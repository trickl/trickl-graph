package com.trickl.graph.generate;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.RandomEngine;
import com.trickl.graph.edges.DirectedEdge;
import com.trickl.graph.planar.PlanarGraph;
import com.trickl.graph.planar.PlanarGraphs;
import com.trickl.graph.planar.PlanarLayout;
import com.trickl.random.RandomEngineShuffler;
import com.trickl.random.Shuffler;
import com.vividsolutions.jts.algorithm.Angle;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import java.util.*;
import org.jgrapht.VertexFactory;

/* A delaunay generator loosely based off a description published  by Dani Lischinski of Cornell University */
/* This is a randomized incremental algorithm that uses a directed search for point location O(n^3/2) */
/* Faster algorithms rely on a more efficient search O(log(n)) that uses a tree structure, this algorithm sacrifices */
/* that speed for simplicity */
public class DelaunayGraphGenerator<V, E> implements PlanarGraphGenerator<V, E, V>, PlanarLayout<V> {

   private RandomEngine randomEngine = new MersenneTwister();
   private Map<Coordinate, V> coordinateToVertex;
   private Map<V, Coordinate> vertexToCoordinate;
   private DirectedEdge<V> lastSearchFace;
   
   public DelaunayGraphGenerator(Set<V> vertices, PlanarLayout<V> layout) {
      
      this.vertexToCoordinate = new HashMap<V, Coordinate>();
      this.coordinateToVertex = new HashMap<Coordinate, V>();
      for (V vertex : vertices) {
         vertexToCoordinate.put(vertex, layout.getCoordinate(vertex));
         coordinateToVertex.put(layout.getCoordinate(vertex), vertex);
      }
   }

   public DelaunayGraphGenerator(List<Coordinate> sites, VertexFactory<V> vertexFactory) {
      this.vertexToCoordinate = new HashMap<V, Coordinate>();
      this.coordinateToVertex = new HashMap<Coordinate, V>();

      for (Coordinate site : sites) {
         V vertex = vertexFactory.createVertex();
         coordinateToVertex.put(site, vertex);
         vertexToCoordinate.put(vertex, site);
      }
   }

   @Override
   public void generateGraph(PlanarGraph<V, E> graph, VertexFactory<V> vertexFactory,
           java.util.Map<java.lang.String, V> resultMap) {
      if (coordinateToVertex.isEmpty()) {
         return;
      }

      List<V> shuffledVertices = new ArrayList<V>(coordinateToVertex.values());
      Shuffler shuffler = new RandomEngineShuffler(randomEngine);
      shuffler.shuffle(shuffledVertices);

      // First contain all points in a boundary
      createBounds(graph, vertexFactory);

      for (V vertex : shuffledVertices) {
         Coordinate site = vertexToCoordinate.get(vertex);
         addSite(graph, site, vertex);
      }

      // Finally remove the boundary
      removeBounds(graph);
   }

   @Override
   public Coordinate getCoordinate(V vertex) {
      return vertexToCoordinate.get(vertex);
   }

   public V getVertex(Coordinate coordinate) {
      return coordinateToVertex.get(coordinate);
   }

   private void createBounds(PlanarGraph<V, E> graph, VertexFactory<V> vertexFactory) {
      Envelope bounds = new Envelope();
      for (V vertex : coordinateToVertex.values()) {
         Coordinate site = vertexToCoordinate.get(vertex);
         bounds.expandToInclude(site);
      }

      // Ensure the bounding triangle always has non-zero width and height
      double triangleHalfWidth = 1.51 * (bounds.getWidth() == 0
              ? (bounds.getHeight() == 0 ? bounds.getHeight() : 1)
              : bounds.getWidth());
      double triangleHalfHeight = 1.01 * (bounds.getHeight() == 0
              ? (bounds.getWidth() == 0 ? bounds.getWidth() : 1)
              : bounds.getHeight());

      V a = vertexFactory.createVertex();
      graph.addVertex(a);
      vertexToCoordinate.put(a, new Coordinate(bounds.centre().x - triangleHalfWidth, bounds.getMaxY() - triangleHalfHeight));

      V b = vertexFactory.createVertex();
      graph.addVertex(b);
      vertexToCoordinate.put(b, new Coordinate(bounds.centre().x + triangleHalfWidth, bounds.getMaxY() - triangleHalfHeight));

      V c = vertexFactory.createVertex();
      graph.addVertex(b);
      vertexToCoordinate.put(c, new Coordinate(bounds.centre().x, bounds.getMaxY() + triangleHalfHeight));

      // The boundary face is defined anticlockwise, which is the convention for this algorithm
      graph.addEdge(a, b);
      graph.addEdge(b, c, a, null);
      graph.addEdge(c, a, b, null);
   }

   private void removeBounds(PlanarGraph<V, E> graph) {      
      for (V vertex : PlanarGraphs.getBoundaryVertices(graph)) {
         graph.removeVertex(vertex);
         vertexToCoordinate.remove(vertex);
      }
   }

   private void addSite(PlanarGraph<V, E> graph, Coordinate p, V vertex) {
      // Locate the edge that this site is next to
      DirectedEdge<V> face = locate(graph, p);

      V first = face.getSource();
      V second = face.getTarget();
      V third = graph.getNextVertex(face.getSource(), face.getTarget());

      // Degenerate cases
      if (p.equals(vertexToCoordinate.get(first))
              || p.equals(vertexToCoordinate.get(second))
              || p.equals(vertexToCoordinate.get(third))) {
         // Point is already in the structure
         return;
      }

      // Connect the new point to the DCEL
      graph.addVertex(vertex);
      vertexToCoordinate.put(vertex, p);

      insertVertexInsideFace(graph, face, vertex);

      // Flip edges as required to maintain the Delaunay property
      enforceDelaunayCondition(graph, first, second);
      enforceDelaunayCondition(graph, second, third);
      enforceDelaunayCondition(graph, third, first);
   }

   private DirectedEdge<V> locate(PlanarGraph<V, E> graph, Coordinate p) {
      // More efficient to set the start edge to the last found edge, effectively randomizing it
      DirectedEdge<V> face = lastSearchFace;
      if (face == null) {
         E edge = graph.edgeSet().iterator().next();
         face = new DirectedEdge<V>(graph.getEdgeSource(edge),
                 graph.getEdgeTarget(edge));
      }

      // Loop until p is left of every edge in the triangle
      while (true) {
         V first = face.getSource();
         V second = face.getTarget();
         V third = graph.getNextVertex(face.getSource(), face.getTarget());

         if (p.equals(first)
                 || p.equals(second)
                 || p.equals(third)) {
            break;
         } else if (isLeftOf(first, second, p)) {
            face = new DirectedEdge<V>(second, first);
         } else if (isLeftOf(second, third, p)) {
            face = new DirectedEdge<V>(third, second);
         } else if (isLeftOf(third, first, p)) {
            face = new DirectedEdge<V>(first, third);
         } else {
            break;
         }
      }

      lastSearchFace = face;

      return face;
   }

   private boolean isLeftOf(V source, V target, Coordinate p) {
      Coordinate a = vertexToCoordinate.get(source);
      Coordinate b = vertexToCoordinate.get(target);
      return Angle.getTurn(Angle.angle(a, b), Angle.angle(b, p)) == Angle.CLOCKWISE;
   }

   private void insertVertexInsideFace(PlanarGraph<V, E> graph, DirectedEdge<V> face, V vertex) {
      V current = face.getSource();
      V next = face.getTarget();

      do {
         V nextNext = graph.getNextVertex(current, next);
         graph.addEdge(next, vertex, current, null);

         current = next;
         next = nextNext;
      } while (!current.equals(face.getSource()));
   }

   private void enforceDelaunayCondition(PlanarGraph<V, E> graph, V source, V target) {
      // Check that the delaunay condition holds      
      V targetNext = graph.getNextVertex(source, target);
      V sourceNext = graph.getNextVertex(target, source);

      // Boundary vertices are treated as if they are infinitely far away
      // so they never break the circumcircle condition
      if (isWithinCircumcircle(graph, source, target, targetNext, sourceNext)) {
         // Flip edges to enforce empty circumcircle condition
         flipTransform(graph, source, target);

         // Check previous edges
         enforceDelaunayCondition(graph, source, sourceNext);
         enforceDelaunayCondition(graph, sourceNext, target);
      }
   }

   // Check if fourth point is within the circumcircle defined by the first three
   private boolean isWithinCircumcircle(PlanarGraph<V, E> graph, V first,
           V second,
           V third,
           V fourth) {
      // Treat the boundary as if infinitely far away
      Coordinate p = vertexToCoordinate.get(fourth);
      if (PlanarGraphs.isVertexBoundary(graph, first)) {
         return isLeftOf(third, second, p);
      } else if (PlanarGraphs.isVertexBoundary(graph, second)) {
         return isLeftOf(first, third, p);
      } else if (PlanarGraphs.isVertexBoundary(graph, third)) {
         return isLeftOf(second, first, p);
      } else if (PlanarGraphs.isVertexBoundary(graph, fourth)) {
         return false;
      }

      Coordinate a = vertexToCoordinate.get(first);
      Coordinate b = vertexToCoordinate.get(second);
      Coordinate c = vertexToCoordinate.get(third);

      boolean within = (a.x * a.x + a.y * a.y) * getDblOrientedTriangleArea(b, c, p)
              - (b.x * b.x + b.y * b.y) * getDblOrientedTriangleArea(a, c, p)
              + (c.x * c.x + c.y * c.y) * getDblOrientedTriangleArea(a, b, p)
              - (p.x * p.x + p.y * p.y) * getDblOrientedTriangleArea(a, b, c) > 0;

      return within;
   }

   // Returns twice the area of the oriented triangle (a, b, c), i.e., the
   // area is positive if the triangle is oriented counterclockwise.
   double getDblOrientedTriangleArea(Coordinate a, Coordinate b, Coordinate c) {
      return (b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x);
   }

   private void flipTransform(PlanarGraph<V, E> graph, V source, V target) {
      V targetNext = graph.getNextVertex(source, target);
      V sourceNext = graph.getNextVertex(target, source);

      // Take care to keep lastSearchFace valid
      if ((lastSearchFace.getSource().equals(source)
              && lastSearchFace.getTarget().equals(target))
              || (lastSearchFace.getSource().equals(target)
              && lastSearchFace.getTarget().equals(source))) {

         lastSearchFace = new DirectedEdge<V>(target, targetNext);
      }

      graph.removeEdge(source, target);
      graph.addEdge(sourceNext, targetNext, source, source);
   }

   public RandomEngine getRandomEngine() {
      return randomEngine;
   }

   public void setRandomEngine(RandomEngine randomEngine) {
      this.randomEngine = randomEngine;
   }
}
