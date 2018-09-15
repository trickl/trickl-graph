/*
 * This file is part of the Trickl Open Source Libraries.
 *
 * Trickl Open Source Libraries - http://open.trickl.com/
 *
 * Copyright (C) 2011 Tim Gee.
 *
 * Trickl Open Source Libraries are free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Trickl Open Source Libraries are distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this project.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.trickl.graph.planar.generate;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.RandomEngine;
import com.trickl.graph.CopyEdgeFactory;
import com.trickl.graph.CopyVertexFactory;
import com.trickl.graph.edges.DirectedEdge;
import com.trickl.graph.edges.UndirectedIdEdge;
import com.trickl.graph.edges.UndirectedIdEdgeFactory;
import com.trickl.graph.planar.BreadthFirstPlanarFaceTraversal;
import com.trickl.graph.planar.DoublyConnectedEdgeList;
import com.trickl.graph.planar.PlanarCopyGraphVisitor;
import com.trickl.graph.planar.PlanarGraph;
import com.trickl.graph.planar.PlanarGraphs;
import com.trickl.graph.planar.PlanarLayout;
import com.trickl.graph.vertices.IdVertex;
import com.trickl.random.RandomEngineShuffler;
import com.trickl.random.Shuffler;
import com.vividsolutions.jts.algorithm.Angle;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineSegment;
import java.util.*;
import org.jgrapht.VertexFactory;

/**
* A delaunay generator loosely based off a description published  by Dani Lischinski of Cornell University 
* This is a randomised incremental algorithm that uses a directed search for point location O(n^3/2) 
* Faster algorithms rely on a more efficient search O(log(n)) that uses a tree structure, this algorithm sacrifices 
* that speed for simplicity
* @author tgee
*/
public class DelaunayGraphGenerator<V, E> implements PlanarGraphGenerator<V, E, V>, PlanarLayout<V> {

   private RandomEngine randomEngine = new MersenneTwister();
   private final List<V> vertices;
   private final Map<Integer, Coordinate> indexToCoordinate;
   private final Map<V, Integer> vertexToIndex;
   private final Map<Coordinate, Integer> coordinateToIndex;
   private DirectedEdge<Integer> lastSearchFace;
   
   public DelaunayGraphGenerator(Set<V> vertices, PlanarLayout<V> layout) {            
      this.indexToCoordinate = new HashMap<>();
      this.coordinateToIndex = new HashMap<>();
      this.vertexToIndex = new HashMap<>();
      this.vertices = new ArrayList<>(vertices);      
      
      for (int index = 0; index < vertices.size(); index++) {
         V vertex = this.vertices.get(index);
         Coordinate coordinate = layout.getCoordinate(vertex);
                     
         vertexToIndex.put(vertex, index);
         indexToCoordinate.put(index, coordinate);
         coordinateToIndex.put(coordinate, index);
      }
   }

   public DelaunayGraphGenerator(Collection<Coordinate> sites, VertexFactory<V> vertexFactory) {
      this.indexToCoordinate = new HashMap<>();
      this.coordinateToIndex = new HashMap<>();
      this.vertexToIndex = new HashMap<>();      
      this.vertices = new ArrayList<>(sites.size());
      
      ArrayList<Coordinate> siteList = new ArrayList<>(sites);
            
      for (int index = 0; index < sites.size(); index++) {      
         V vertex = vertexFactory.createVertex();
         Coordinate coordinate = siteList.get(index);
                  
         vertices.add(vertex);         
         vertexToIndex.put(vertex, index);
         indexToCoordinate.put(index, coordinate);
         coordinateToIndex.put(coordinate, index);
      }
   }

   @Override
   public void generateGraph(PlanarGraph<V, E> graph, VertexFactory<V> vertexFactory,
           java.util.Map<java.lang.String, V> resultMap) {
       
      PlanarGraph<Integer,UndirectedIdEdge<Integer>> source = 
          new DoublyConnectedEdgeList<>(
          new UndirectedIdEdgeFactory<>(), Object.class);
          
      if (coordinateToIndex.isEmpty()) {
         return;
      }
      
      List<Integer> shuffledVertices = new ArrayList<>(coordinateToIndex.values());
      Shuffler shuffler = new RandomEngineShuffler(randomEngine);
      shuffler.shuffle(shuffledVertices);

      // First contain all points in a boundary
      createBounds(source);

      for (Integer vertex : shuffledVertices) {
         Coordinate site = indexToCoordinate.get(vertex);
         addSite(source, site, vertex);
      }

      // Finally remove the boundary
      removeBounds(source);
      
      PlanarCopyGraphVisitor<Integer, UndirectedIdEdge<Integer>, V, E> copyGraphVisitor 
          = new PlanarCopyGraphVisitor(source, graph, 
          new CopyVertexFactory<V, Integer>() {
          @Override
          public V createVertex(Integer index) {
              return vertices.get(index);
          }
      },        
                  new CopyEdgeFactory<V, E, UndirectedIdEdge>() {
          @Override
          public E createEdge(V sourceVertex, V targetVertex, UndirectedIdEdge edge) {
              return graph.getEdgeFactory().createEdge(sourceVertex, targetVertex);
          }
      });
      
      BreadthFirstPlanarFaceTraversal<Integer, UndirectedIdEdge<Integer>> planarFaceTraversal = new BreadthFirstPlanarFaceTraversal<>(source);
      planarFaceTraversal.traverse(copyGraphVisitor);
   }

   @Override
   public Coordinate getCoordinate(V vertex) {
      return indexToCoordinate.get(vertexToIndex.get(vertex));
   }

   public V getVertex(Coordinate coordinate) {
      return vertices.get(coordinateToIndex.get(coordinate));
   }

   private void createBounds(PlanarGraph<Integer, UndirectedIdEdge<Integer>> graph) {
      Envelope bounds = new Envelope();
      for (Integer vertex : coordinateToIndex.values()) {
         Coordinate site = indexToCoordinate.get(vertex);
         bounds.expandToInclude(site);
      }

      // Ensure the bounding triangle always has non-zero width and height
      double triangleHalfWidth = 1.51 * (bounds.getWidth() == 0
              ? (bounds.getHeight() == 0 ? bounds.getHeight() : 1)
              : bounds.getWidth());
      double triangleHalfHeight = 1.01 * (bounds.getHeight() == 0
              ? (bounds.getWidth() == 0 ? bounds.getWidth() : 1)
              : bounds.getHeight());

      int a = vertices.size();
      graph.addVertex(a);
      indexToCoordinate.put(a, new Coordinate(bounds.centre().x - triangleHalfWidth, bounds.getMaxY() - triangleHalfHeight));

      int b = a + 1;
      graph.addVertex(b);
      indexToCoordinate.put(b, new Coordinate(bounds.centre().x + triangleHalfWidth, bounds.getMaxY() - triangleHalfHeight));

      int c = b + 1;
      graph.addVertex(b);
      indexToCoordinate.put(c, new Coordinate(bounds.centre().x, bounds.getMaxY() + triangleHalfHeight));

      // The boundary face is defined anticlockwise, which is the convention for this algorithm
      graph.addEdge(a, b);
      graph.addEdge(b, c, a, null);
      graph.addEdge(c, a, b, null);
   }

   private void removeBounds(PlanarGraph<Integer, UndirectedIdEdge<Integer>> graph) {      
      for (Integer vertex : PlanarGraphs.getBoundaryVertices(graph)) {
         graph.removeVertex(vertex);
         indexToCoordinate.remove(vertex);
      }
   }

   private void addSite(PlanarGraph<Integer, UndirectedIdEdge<Integer>> graph, Coordinate p, Integer vertex) {
      
      // More efficient to set the start edge to the last found edge, effectively randomizing it
      DirectedEdge<Integer> start = lastSearchFace;
      if (start == null) {
         start = graph.getBoundary().getTwin();
      } 
       
      // Locate the edge that this site is next to
      DirectedEdge<Integer> face = locateInsideBoundary(graph, p, start);      
      lastSearchFace = face;

      Integer first = face.getSource();
      Integer second = face.getTarget();
      Integer third = graph.getNextVertex(face.getSource(), face.getTarget());

      // Degenerate cases
      if (p.equals(indexToCoordinate.get(first))
              || p.equals(indexToCoordinate.get(second))
              || p.equals(indexToCoordinate.get(third))) {
         // Point is already in the structure
         return;
      }

      // Connect the new point to the DCEL
      graph.addVertex(vertex);
      indexToCoordinate.put(vertex, p);

      insertVertexInsideFace(graph, face, vertex);

      // Flip edges as required to maintain the Delaunay property
      enforceDelaunayCondition(graph, first, second);
      enforceDelaunayCondition(graph, second, third);
      enforceDelaunayCondition(graph, third, first);
   }

   // See https://www.cl.cam.ac.uk/techreports/UCAM-CL-TR-728.pdf
   private DirectedEdge<Integer> locateInsideBoundary(PlanarGraph<Integer, UndirectedIdEdge<Integer>> graph, Coordinate x, DirectedEdge<Integer> start) {
      // Validate that the supplied point is inside the boundary, otherwise we may not terminate
      for (UndirectedIdEdge<Integer> boundaryEdge : PlanarGraphs.getBoundaryEdges(graph)) {
          if (isRightOf(x, new DirectedEdge(graph.getEdgeSource(boundaryEdge), graph.getEdgeTarget(boundaryEdge)))) {
             throw new IllegalArgumentException("Search Coordinate must be inside or on boundary.");
          }
      }
       
      // Loop until p is left of every edge in the triangle
      int iterations = 0;
      int maxIterations = graph.edgeSet().size();
      DirectedEdge<Integer> edge = start;
            
      if (isRightOf(x, edge)) {
         edge = edge.getTwin();
      } 
      
      while (true) {        
         if (iterations++ > maxIterations) {
             // Should not happen, if it does the boundary conditions and tolerances may be inconsistent.
             throw new RuntimeException("Search overflow, geometric conditions not met.");
         }
         
         Integer origVertex = edge.getSource();
         Integer destVertex = edge.getTarget();
         Coordinate origCoord = indexToCoordinate.get(origVertex);
         Coordinate destCoord = indexToCoordinate.get(destVertex);
                                 
         // Keep p to the left of our edge walk
         if (x.equals2D(origCoord) || x.equals2D(destCoord)) {
            break;
         }
         else {
             int whichOp = 0;
             
            Integer nextVertex = graph.getPrevVertex(origVertex, destVertex);
            DirectedEdge<Integer> next = new DirectedEdge<>(origVertex, nextVertex);
            if (!isRightOf(x, next)) {
              whichOp += 1;
            }
            
            DirectedEdge<Integer> prev = new DirectedEdge<>(nextVertex, destVertex);
            if (!isRightOf(x, prev)) {
                whichOp += 2;
            }
            
            if (whichOp == 0) break;
            else if (whichOp == 1) edge = next;
            else if (whichOp == 2) edge = prev;
            else {
                if (distance(x, next) < distance(x, prev)) {
                    edge = next;
                }
                else {
                    edge = prev;
                }
            }             
         }
      }

      return edge;
   }
   
   private boolean isRightOf(Coordinate x, DirectedEdge<Integer> edge) {
      Coordinate source = indexToCoordinate.get(edge.getSource());
      Coordinate target = indexToCoordinate.get(edge.getTarget());
      
      // Note we flip source and target as orientationIndex == 1, when isLeftOf(...) is true.
      LineSegment line = new LineSegment(target, source);
      return line.orientationIndex(x) == 1;
   }
   
   private double distance(Coordinate x, DirectedEdge<Integer> edge) {       
      Coordinate source = indexToCoordinate.get(edge.getSource());
      Coordinate target = indexToCoordinate.get(edge.getTarget());
      
      LineSegment line = new LineSegment(source, target);
      return line.distance(x);
   }

   private void insertVertexInsideFace(PlanarGraph<Integer, UndirectedIdEdge<Integer>> graph, DirectedEdge<Integer> face, Integer vertex) {            
      Integer current = face.getSource();
      Integer next = face.getTarget();
      
      if (graph.isBoundary(current, next)) {
          throw new IllegalArgumentException("Cannot insert vertex into the boundary face");
      }

      do {
         Integer nextNext = graph.getNextVertex(current, next);
         graph.addEdge(next, vertex, current, null);

         current = next;
         next = nextNext;
      } while (!current.equals(face.getSource()));
   }

   private void enforceDelaunayCondition(PlanarGraph<Integer, UndirectedIdEdge<Integer>> graph, Integer source, Integer target) {
      // Check that the delaunay condition holds      
      Integer targetNext = graph.getNextVertex(source, target);
      Integer sourceNext = graph.getNextVertex(target, source);

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
   private boolean isWithinCircumcircle(PlanarGraph<Integer, UndirectedIdEdge<Integer>> graph, Integer first,
           Integer second,
           Integer third,
           Integer fourth) {
      // Treat the boundary as if infinitely far away
      Coordinate p = indexToCoordinate.get(fourth);
      if (PlanarGraphs.isVertexBoundary(graph, first)) {
         return isRightOf(p, new DirectedEdge(third, second));
      } else if (PlanarGraphs.isVertexBoundary(graph, second)) {
         return isRightOf(p, new DirectedEdge(first, third));
      } else if (PlanarGraphs.isVertexBoundary(graph, third)) {
         return isRightOf(p, new DirectedEdge(second, first));
      } else if (PlanarGraphs.isVertexBoundary(graph, fourth)) {
         return false;
      }

      Coordinate a = indexToCoordinate.get(first);
      Coordinate b = indexToCoordinate.get(second);
      Coordinate c = indexToCoordinate.get(third);

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

   private void flipTransform(PlanarGraph<Integer, UndirectedIdEdge<Integer>> graph, Integer source, Integer target) {
      Integer targetNext = graph.getNextVertex(source, target);
      Integer sourceNext = graph.getNextVertex(target, source);

      // Take care to keep lastSearchFace valid
      if ((lastSearchFace.getSource().equals(source)
              && lastSearchFace.getTarget().equals(target))
              || (lastSearchFace.getSource().equals(target)
              && lastSearchFace.getTarget().equals(source))) {

         lastSearchFace = new DirectedEdge<Integer>(target, targetNext);
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
