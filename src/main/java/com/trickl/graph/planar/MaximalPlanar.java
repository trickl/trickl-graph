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

import com.trickl.graph.edges.UndirectedEdge;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import org.jgrapht.EdgeFactory;

public class MaximalPlanar<V, E> {

   private static class TriangulationVisitor<V, E> implements PlanarFaceTraversalVisitor<V, E> {

      private class Detail<V> {

         int marked;
         int degreeSize;
      }
            
      private Map<V, Detail<V>> vertexDetails;      
      private LinkedList<V> verticesOnFace;
      private int timestamp = 0;
      private EdgeFactory<V, E> edgeFactory;
      private PlanarGraph<V, E> graph;
      private boolean addEdges;
      private boolean checkInteriorOnly;
      private Set<UndirectedEdge<V>> missingEdges;

      TriangulationVisitor(PlanarGraph<V, E> graph, EdgeFactory<V, E> edgeFactory, boolean addEdges, boolean checkInteriorOnly) {
         this.graph = graph;
         this.edgeFactory = edgeFactory;
         this.addEdges = addEdges;
         this.checkInteriorOnly = checkInteriorOnly;
         this.vertexDetails = new Hashtable<V, Detail<V>>();
         this.verticesOnFace = new LinkedList<V>();
         this.missingEdges = new HashSet<UndirectedEdge<V>>();

         for (V vertex : graph.vertexSet())
         {
            Detail<V> detail = new Detail<V>();
            detail.degreeSize = graph.edgesOf(vertex).size();
            vertexDetails.put(vertex, detail);
         }
      }

      @Override
      public void nextVertex(V vertex) {
         if (!verticesOnFace.isEmpty() &&
                (verticesOnFace.getFirst().equals(vertex) ||
                 verticesOnFace.getLast().equals(vertex))) return;

         verticesOnFace.add(vertex);
      }

      @Override
      public void endFace(V source, V target) {
         ++timestamp;

         if (verticesOnFace.size() <= 3) {
            // At most three vertices on this face - don't need to triangulate
            verticesOnFace.clear();
            return;
         }

         if (checkInteriorOnly && graph.isBoundary(source, target)) {
            // Do not triangulate the outer face
            return;
         }

         // Find vertex on face of minimum degree
         int minDegree = graph.vertexSet().size();
         V minDegreeVertex = null;
         for (V vertex : verticesOnFace) {
            Detail<V> detail = vertexDetails.get(vertex);
            if (detail.degreeSize < minDegree) {
               minDegreeVertex = vertex;
               minDegree = detail.degreeSize;
            }
         }

         // Put this vertex first in the list
         Collections.rotate(verticesOnFace, -verticesOnFace.indexOf(minDegreeVertex));

         // Mark all of the min degree vertex's neighbours
         for (E edge : graph.edgesOf(minDegreeVertex)) {
            V vertex = graph.getEdgeSource(edge).equals(minDegreeVertex) ?
                          graph.getEdgeTarget(edge) :
                          graph.getEdgeSource(edge);

            vertexDetails.get(vertex).marked = timestamp;
         }

         // The iterator manipulations on the next two lines are safe because
         // verticesOnFace.size() > 3 (from the first test in this function)
         V markedNeighbour = null;
         for (V vertex : verticesOnFace.subList(2, verticesOnFace.size() - 1)) {
            if (vertexDetails.get(vertex).marked == timestamp) {
               markedNeighbour = vertex;
            }
         }

         if (markedNeighbour == null) {
            addEdgeRange(verticesOnFace.get(0),
                         verticesOnFace.get(verticesOnFace.size() - 1),
                         verticesOnFace.subList(2, verticesOnFace.size() - 1));
         }
         else {
            int markedNeighbourIndex = verticesOnFace.indexOf(markedNeighbour);
            addEdgeRange(verticesOnFace.get(1),
                         verticesOnFace.get(0),
                         verticesOnFace.subList(markedNeighbourIndex + 1, verticesOnFace.size()));
            addEdgeRange(verticesOnFace.get(markedNeighbourIndex + 1),
                         verticesOnFace.get(markedNeighbourIndex),
                         verticesOnFace.subList(2, markedNeighbourIndex));
         }

         // Reset for the next face
         verticesOnFace.clear();
      }

      @Override
      public void beginTraversal() {
      }

      @Override
      public void nextEdge(V source, V target) {
      }

      @Override
      public void beginFace(V source, V target) {
      }

      @Override
      public void endTraversal() {
      }


      public Set<UndirectedEdge<V>> getMissingEdges() {
         return missingEdges;
      }

      private void addEdgeRange(V source, V before, List<V> targets) {                           
         for (V target : targets) {
             if (addEdges) {
               E edge = edgeFactory.createEdge(source, target);
               graph.addEdge(source, target, before, null, edge);
             }
             
             missingEdges.add(new UndirectedEdge<V>(source, target));
             vertexDetails.get(source).degreeSize++;
             vertexDetails.get(target).degreeSize++;
         }
      }
   };

   public MaximalPlanar() {            
   }

   public Set<UndirectedEdge<V>>  getMaximalPlanarEdgeDeficit(PlanarGraph<V, E> graph) {
      return traverse(graph, graph.getEdgeFactory(), false, false);
   }

   public Set<UndirectedEdge<V>> getInteriorTriangulatedEdgeDeficit(PlanarGraph<V, E> graph) {
      return traverse(graph, graph.getEdgeFactory(), false, true);
   }


   public boolean isMaximalPlanar(PlanarGraph<V, E> graph) {
      return getMaximalPlanarEdgeDeficit(graph).isEmpty();
   }

   public boolean isInteriorTriangulated(PlanarGraph<V, E> graph) {
      return getInteriorTriangulatedEdgeDeficit(graph).isEmpty();
   }

   public Set<UndirectedEdge<V>> makeMaximalPlanar(PlanarGraph<V, E> graph) {
      return makeMaximalPlanar(graph, graph.getEdgeFactory());
   }

   public Set<UndirectedEdge<V>> makeMaximalPlanar(PlanarGraph<V, E> graph, EdgeFactory<V, E> edgeFactory) {
      return traverse(graph, edgeFactory, true, false);
   }

   private Set<UndirectedEdge<V>> traverse(PlanarGraph<V, E> graph, EdgeFactory<V, E> edgeFactory, boolean addEdges, boolean checkInteriorOnly) {
      BreadthFirstPlanarFaceTraversal<V, E> planarFaceTraversal = new BreadthFirstPlanarFaceTraversal<V, E>(graph);
      TriangulationVisitor<V, E> triangulationVisitor = new TriangulationVisitor(graph, edgeFactory, addEdges, checkInteriorOnly);
      planarFaceTraversal.traverse(triangulationVisitor);
      return triangulationVisitor.getMissingEdges();
   }
} 
