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

import com.trickl.graph.edges.DirectedEdge;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jgrapht.Graphs;

public class CanonicalPlanarFaceTraversal<V, E> implements PlanarFaceTraversal<V, E> {

   protected PlanarGraph<V, E> graph;

   public CanonicalPlanarFaceTraversal(PlanarGraph<V, E> graph) {
      this.graph = graph;
   }

   @Override
   public void traverse(PlanarFaceTraversalVisitor<V, E> visitor) {
      visitor.beginTraversal();

      // Copy the embedding so the graph can be modified during traversal
      Map<V, List<V>> embedding = new Hashtable<V, List<V>>();
      for (V vertex : graph.vertexSet()) {
         List<V> targets = new LinkedList<V>();
         Set<E> edges = graph.edgesOf(vertex);
         for (E edge : edges) {
            V target = Graphs.getOppositeVertex(graph, edge, vertex);
            targets.add(target);
         }

         embedding.put(vertex, targets);
      }

      // Need a triconnected graph to determine the canonical order
      PlanarGraph<V, E> maximalGraphCopy = new DoublyConnectedEdgeList<V, E, Object>(graph, Object.class);
      MaximalPlanar<V, E> maximalPlanar = new MaximalPlanar<V, E>();
      maximalPlanar.makeMaximalPlanar(maximalGraphCopy);

      // Get the canonical ordering
      PlanarCanonicalOrdering<V, E> planarCanonicalOrder = new LeftistPlanarCanonicalOrdering<V, E>();

      DirectedEdge<V> boundary = maximalGraphCopy.getBoundary();
      List<V> canonicalOrder = planarCanonicalOrder.getOrder(maximalGraphCopy, boundary.getSource());

      boundary = graph.getBoundary();
      Set<V> processedVertices = new HashSet<V>();
      for (V source : canonicalOrder) {
         processedVertices.add(source);
         
         // Process all internal faces connecting to an item of
         // lower canonical order
         for (V target : embedding.get(source)) {
            V next = graph.getNextVertex(source, target);
            if (processedVertices.contains(target) 
               && processedVertices.contains(next)
               && !graph.isBoundary(source, target)) {
               traverseFace(visitor, source, target);
            }
         }
      }
      
      // Finally process the boundary face
      traverseFace(visitor, boundary.getSource(), boundary.getTarget());

      visitor.endTraversal();
   }

   private void traverseFace(PlanarFaceTraversalVisitor<V, E> visitor, V source, V target) {
      visitor.beginFace(source, target);

      V prevVertex = null;
      V firstVertex = null;
      for (V vertex : PlanarGraphs.getVerticesOnFace(graph, source, target)) {
         if (prevVertex == null) {
            firstVertex = vertex;
         } else {
            visitor.nextEdge(prevVertex, vertex);
         }
         visitor.nextVertex(vertex);

         prevVertex = vertex;
      }
      visitor.nextEdge(prevVertex, firstVertex);

      visitor.endFace(source, target);
   }
}
