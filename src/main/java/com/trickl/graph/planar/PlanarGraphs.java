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
package com.trickl.graph.planar;

import com.trickl.graph.CopyEdgeFactory;
import com.trickl.graph.CopyVertexFactory;
import com.trickl.graph.EdgeVisitor;
import com.trickl.graph.edges.DirectedEdge;
import com.vividsolutions.jts.algorithm.Angle;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LinearRing;
import java.util.*;
import org.jgrapht.Graphs;
import org.jgrapht.VertexFactory;

public final class PlanarGraphs {

   private PlanarGraphs() {
   }

   static public <V, E> List<V> getBoundaryVertices(PlanarGraph<V, E> graph) {
      DirectedEdge<V> boundary = graph.getBoundary();
      if (boundary == null) return new ArrayList<V>();
      return PlanarGraphs.getVerticesOnFace(graph, boundary.getSource(), boundary.getTarget());
   }

   static public <V, E> void boundaryHops(PlanarGraph<V, E> graph, Map<V, Integer> hops) {
      Set<E> boundary = getBoundaryEdges(graph);
      for (E edge : boundary) {
         hops.put(graph.getEdgeSource(edge), 0);
         hops.put(graph.getEdgeTarget(edge), 0);
      }

      Queue<V> vertexQueue = new LinkedList<V>();
      vertexQueue.addAll(hops.keySet());

      while (!vertexQueue.isEmpty()) {
         V vertex = vertexQueue.poll();
         for (V neighbour : getConnectedVertices(graph, vertex)) {
            if (!hops.containsKey(neighbour)) {
               hops.put(neighbour, hops.get(vertex) + 1);
               vertexQueue.add(neighbour);
            }
         }
      }
   }

   static public <V1, E1, V2, E2> PlanarLayout<V2> delaunayToVoronoi(PlanarGraph<V1, E1> delaunay,
                                               PlanarLayout<V1> delaunayLocations,
                                               PlanarGraph<V2, E2> voronoi,
                                               LinearRing boundary,
                                               VertexFactory<V2> vertexFactory) {
      DelaunayVoronoiVisitor<V1, E1, V2, E2> delaunayVoronoiVisitor = new DelaunayVoronoiVisitor(
              delaunay, delaunayLocations, voronoi, boundary, vertexFactory);
      PlanarFaceTraversal<V1, E1> planarFaceTraversal = new CanonicalPlanarFaceTraversal<V1, E1>(delaunay);
      planarFaceTraversal.traverse(delaunayVoronoiVisitor);
      return delaunayVoronoiVisitor;
   }

   static public <V1, E1, V2, E2> void dualGraph(PlanarGraph<V1, E1> graph,
                                       PlanarGraph<V2, E2> dualGraph,
                                       VertexFactory<V2> vertexFactory) {
      DualGraphVisitor<V1, E1, V2, E2> dualGraphVisitor = new DualGraphVisitor(graph, dualGraph, vertexFactory);
      PlanarFaceTraversal<V1, E1> planarFaceTraversal = new CanonicalPlanarFaceTraversal<V1, E1>(graph);
      planarFaceTraversal.traverse(dualGraphVisitor);
   }

   static public <V, E> void split(PlanarGraph<V, E> graph, E edge, V vertex) {
      V target = graph.getEdgeTarget(edge);
      V source = graph.getEdgeSource(edge);
      V before = graph.getPrevVertex(source, target);
      V after  = graph.getNextVertex(source, target);
      graph.removeEdge(edge);
      graph.addEdge(source, vertex, before, null);
      graph.addEdge(vertex, target, source, after);
   }

   static public <V1, E1, V2, E2> Map<V1, V2> aggregate(PlanarGraph<V1, E1> source,
                                            PlanarGraph<V2, E2> target,
                                            Map<V1, Integer> aggregateGroups,
                                            CopyVertexFactory<V2, V1> vertexFactory,
                                            CopyEdgeFactory<V2, E2, E1> edgeFactory) {
      PlanarCopyGraphVisitor<V1, E1, V2, E2> copyGraphVisitor = new PlanarCopyGraphVisitor(
              source, target, vertexFactory, edgeFactory);
      copyGraphVisitor.setAggregationGroups(aggregateGroups);
      BreadthFirstPlanarFaceTraversal<V1, E1> planarFaceTraversal = new BreadthFirstPlanarFaceTraversal<V1, E1>(source);
      planarFaceTraversal.traverse(copyGraphVisitor);
      return copyGraphVisitor.getVertexMap();
   }

   static public <V1, E1, V2, E2> Map<V1, V2> aggregate(PlanarGraph<V1, E1> source,
                                            PlanarGraph<V2, E2> target,
                                            Map<V1, Integer> aggregateGroups) {
      return aggregate(source, target, aggregateGroups, null, null);
   }

   static public <V1, E1, V2, E2> Map<V1, V2> copy(PlanarGraph<V1, E1> source,
                                            PlanarGraph<V2, E2> target,
                                            CopyVertexFactory<V2, V1> vertexFactory,
                                            CopyEdgeFactory<V2, E2, E1> edgeFactory) {
      return aggregate(source, target, null, vertexFactory, edgeFactory);
   }

   static public <V1, E1, V2, E2> Map<V1, V2> subgraph(PlanarGraph<V1, E1> graph, PlanarGraph<V2, E2> subgraph,
                                            Set<V1> vertices,
                                            V1 boundarySource,
                                            V1 boundaryTarget,
                                            CopyVertexFactory<V2, V1> vertexFactory,
                                            CopyEdgeFactory<V2, E2, E1> edgeFactory) {
      PlanarGraph<V1, E1> subgraphProxy = new PlanarSubGraph<V1, E1>(graph, vertices, boundarySource, boundaryTarget);
      return copy(subgraphProxy, subgraph, vertexFactory, edgeFactory);
   }

   static public <V1, E1, V2, E2> Map<V1, V2> subgraph(PlanarGraph<V1, E1> graph, PlanarGraph<V2, E2> subgraph, Set<V1> vertices, V1 boundarySource, V1 boundaryTarget) {
      return subgraph(graph, subgraph, vertices, boundarySource, boundaryTarget, null, null);
   }

   static public <V, E> Set<E> getEdgesInsideBoundary(PlanarGraph<V, E> graph, List<V> boundary) {

      // Search for edges within the boundary
      Stack<DirectedEdge<V>> edgeStack = new Stack<DirectedEdge<V>>();
      List<E> boundaryEdges = new LinkedList<E>();

      for (int i = 0; i < boundary.size(); ++i) {
         V boundaryCurrent = boundary.get(i);
         V boundaryNext = boundary.get((i + 1) % boundary.size());

         if (!graph.containsEdge(boundaryCurrent, boundaryNext)) {
            throw new NoSuchElementException("Boundary does not define an edge cycle in the graph.");
         }

         boundaryEdges.add(graph.getEdge(boundaryCurrent, boundaryNext));
         edgeStack.add(new DirectedEdge(boundaryCurrent, boundaryNext));
      }

      Set<E> edges = new HashSet<E>(boundaryEdges);

      while (!edgeStack.isEmpty()) {
         DirectedEdge<V> edge = edgeStack.pop();
         V nextVertex = graph.getNextVertex(edge.getSource(), edge.getTarget());
         E nextEdge = graph.getEdge(nextVertex, edge.getTarget());
         if (!edges.contains(nextEdge)) {
            edgeStack.add(new DirectedEdge<V>(nextVertex, edge.getTarget()));
            edges.add(nextEdge);
         }
      }

      // Don't include boundary edges
      edges.removeAll(boundaryEdges);

      return edges;
   }

   /**
    * Boundary should have the same chirality as inner faces (so if an internal face
    * is described counter-clockwise, the boundary is also counter-clockwise).
    * @param <V>
    * @param <E>
    * @param graph
    * @param boundary
    * @param removeEdgeVisitor
    */
   static public <V, E> void removeEdgesInsideBoundary(PlanarGraph<V, E> graph, List<V> boundary, EdgeVisitor<E> removeEdgeVisitor) {

      for (E edge : getEdgesInsideBoundary(graph, boundary)) {
         if (removeEdgeVisitor != null) {
            removeEdgeVisitor.onEdge(edge);
         }

         graph.removeEdge(edge);
      }
   }

   static public <V, E> void triangulateFace(PlanarGraph<V, E> graph, V source, V target, EdgeVisitor<E> addEdgeVisitor) {

      V sourceNext = target;
      V current = graph.getNextVertex(source, sourceNext);
      V next = graph.getNextVertex(sourceNext, current);

      while (!next.equals(source)) {
         if (graph.containsEdge(source, current)) {            
            E edge = graph.addEdge(next, sourceNext, current, null);
            if (addEdgeVisitor != null) {
               addEdgeVisitor.onEdge(edge);
            }
         } else {
            E edge = graph.addEdge(current, source, sourceNext, null);
            if (addEdgeVisitor != null) {
               addEdgeVisitor.onEdge(edge);
            }

            sourceNext = current;
         }

         current = next;
         next = graph.getNextVertex(sourceNext, current);
      } 
   }

   static public <V, E> List<V> getConnectedVertices(PlanarGraph<V, E> graph, V vertex) {
      return getConnectedVertices(graph, vertex, null);
   }

   static public <V, E> List<V> getConnectedVertices(PlanarGraph<V, E> graph, V vertex, V startVertex) {
      return getConnectedVertices(graph, vertex, startVertex, startVertex);
   }

   /**
    * @param <V>
    * @param <E>
    * @param graph
    * @param vertex
    * @param startVertex The first vertex, included in output.
    * @param endVertex The last vertex , included in output.
    * @return A list of all the vertices connected to the supplied vertex. Given in inward face order.
    * TODO: Handle self-loops, which are not returned at the moment. They probably should be? (self-connected)
    * , perhaps should be returned first regardless of start and end?...
    */
   static public <V, E> List<V> getConnectedVertices(PlanarGraph<V, E> graph, V vertex, V startVertex, V endVertex) {
      if (graph == null || vertex == null) {
         throw new NullPointerException();
      }

      Set<E> edges = graph.edgesOf(vertex);
      LinkedList<V> frontVertices = new LinkedList<V>();
      LinkedList<V> backVertices = new LinkedList<V>();

      // Note edge discovery order will be in outward face order, so
      // we need to work backwards
      boolean afterEnd = (endVertex == null);
      boolean beforeStart = true;
      for (E edge : edges) {
         V target = Graphs.getOppositeVertex(graph, edge, vertex);

         LinkedList<V> vertices = beforeStart ? frontVertices : backVertices;
         if (beforeStart || afterEnd) {
            vertices.addFirst(target);
         }

         if (beforeStart && (startVertex == null || target.equals(startVertex))) {
            beforeStart = false;
            afterEnd = false;
         }
         if (!afterEnd && (endVertex == null || target.equals(endVertex))) {
            afterEnd = true;
            if (beforeStart) {
               // Still waiting to encounter the start vertex
               frontVertices.clear();
            }
         }
      }

      frontVertices.addAll(backVertices);

      return frontVertices;
   }

   static public <V, E> List<V> getVerticesOnFace(PlanarGraph<V, E> graph, V startVertex, V targetVertex) {
      return getVerticesOnFace(graph, startVertex, targetVertex, null);
   }

   static public <V, E> List<V> getVerticesOnFace(PlanarGraph<V, E> graph, V startVertex, V targetVertex, V endVertex) {
      if (graph == null || startVertex == null || targetVertex == null) {
         throw new NullPointerException();
      }
      
      List<V> vertices = new LinkedList<V>();
      V boundaryCurrent = startVertex;
      V boundaryNext = targetVertex;
      if (endVertex == null) endVertex = startVertex;
      do {
         vertices.add(boundaryCurrent);

         V boundaryNextNext = graph.getNextVertex(boundaryCurrent, boundaryNext);
         boundaryCurrent = boundaryNext;
         boundaryNext = boundaryNextNext;
      }
      while (!boundaryCurrent.equals(endVertex));
      return vertices;
   }

   static public <V, E> boolean isEdgeBoundary(PlanarGraph<V, E> graph, E edge) {
      V source = graph.getEdgeSource(edge);
      V target = graph.getEdgeTarget(edge);

      return graph.isBoundary(source, target) ||
             (!com.trickl.graph.Graphs.isEdgeDirected(graph, edge) && graph.isBoundary(target, source));
   }

   static public <V, E> boolean isVertexBoundary(PlanarGraph<V, E> graph, V vertex) {
      for (E edge : graph.edgesOf(vertex)) {
         if (isEdgeBoundary(graph, edge)) {
            return true;
         }
      }
      return false;
   }

   static public <V, E> Set<E> getBoundaryEdges(PlanarGraph<V, E> graph) {
      Set<E> edges = new LinkedHashSet<E>();
      V priorVertex = null;
      V firstVertex = null;
      for (V vertex : getBoundaryVertices(graph)) {
         if (priorVertex != null) {
            edges.add(graph.getEdge(priorVertex, vertex));
         }
         else {
            firstVertex = vertex;
         }

         priorVertex = vertex;
      }
      if (priorVertex != null) {
         edges.add(graph.getEdge(priorVertex, firstVertex));
      }
      return edges;
   }

   static public <V, E> Set<E> getBoundaryEdges(PlanarGraph<V, E> graph, V vertex) {
      Set<E> edges = new HashSet<E>();
      for (E edge : graph.edgesOf(vertex)) {
         if (isEdgeBoundary(graph, edge)) {
            edges.add(edge);
         }
      }
      return edges;
   }

   static public <V, E> V getNextVertexOnBoundary(PlanarGraph<V, E> graph, V vertex) {
      for (E edge : graph.edgesOf(vertex)) {
         V target = Graphs.getOppositeVertex(graph, edge, vertex);
         if (graph.isBoundary(vertex, target)) {
            return target;
         }
      }
      throw new NoSuchElementException("Vertex not on boundary.");
   }

   static public <V, E> V getPrevVertexOnBoundary(PlanarGraph<V, E> graph, V vertex) {
      for (E edge : graph.edgesOf(vertex)) {
         V target = Graphs.getOppositeVertex(graph, edge, vertex);
         if (graph.isBoundary(target, vertex)) {
            return target;
         }
      }
      throw new NoSuchElementException("Vertex not on boundary.");
   }

   static public <V, E> List<V> getInnerBoundary(PlanarGraph<V, E> graph, List<V> outerBoundary) {
      List<V> innterBoundary = new LinkedList<V>();
      Collections.reverse(outerBoundary);
      for (int i = 0; i < outerBoundary.size(); ++i)
      {
         V prevOuterVertex = outerBoundary.get(i);
         V outerVertex = outerBoundary.get((i + 1) % outerBoundary.size());
         V nextOuterVertex = outerBoundary.get((i + 2) % outerBoundary.size());
         V nextInnerVertex = innterBoundary.isEmpty()
                             ? prevOuterVertex
                             : innterBoundary.get(innterBoundary.size() - 1);
         List<V> perimeter = PlanarGraphs.getConnectedVertices(graph, outerVertex, nextInnerVertex, nextOuterVertex);
         if (!perimeter.isEmpty()) {
            innterBoundary.addAll(perimeter.subList(1, perimeter.size()));
         }
      }
      return innterBoundary.isEmpty() ? innterBoundary : innterBoundary.subList(1, innterBoundary.size());
   }

   static public <V, E> boolean isBoundaryConvex(PlanarGraph<V, E> graph, PlanarLayout<V> layout) {
      boolean isConvex = true;
      List<V> boundaryVertices = PlanarGraphs.getBoundaryVertices(graph);
      Coordinate[] boundaryCoordinates = new Coordinate[boundaryVertices.size() + 1];
      for (int i = 0; i < boundaryVertices.size(); ++i) {
         boundaryCoordinates[i] = layout.getCoordinate(boundaryVertices.get(i));
      }

      int priorOrientation = Angle.NONE;
      for (int i = 0; i < boundaryVertices.size(); ++i) {
         Coordinate currentCoord = boundaryCoordinates[i];
         Coordinate nextCoord    = boundaryCoordinates[(i + 1) % boundaryVertices.size()];
         Coordinate prevCoord    = boundaryCoordinates[(i + boundaryVertices.size() - 1) % boundaryVertices.size()];
         int orientation = Angle.getTurn(Angle.angle(prevCoord, currentCoord), Angle.angle(currentCoord, nextCoord));
         if (priorOrientation != Angle.NONE)
         {
            if (orientation != priorOrientation)
            {
               // The boundary should be defined in a consistent orientation
               return false;
            }
            priorOrientation = orientation;
         }
      }

      return isConvex;
   }
}
