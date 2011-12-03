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
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class BreadthFirstPlanarFaceTraversal<V, E> implements PlanarFaceTraversal<V, E> {

   protected PlanarGraph<V, E> graph;

   public BreadthFirstPlanarFaceTraversal(PlanarGraph<V, E> graph) {
      this.graph = graph;
   }

   @Override
   public void traverse(PlanarFaceTraversalVisitor<V, E> visitor) {
      visitor.beginTraversal();

      // Copy the embedding so the graph can be modified during traversal
      Map<DirectedEdge<V>, DirectedEdge<V>> embedding = new Hashtable<DirectedEdge<V>, DirectedEdge<V>>();
      for (V vertex : graph.vertexSet()) {

         V priorItr = null;
         V firstItr = null;
         for (V itr : PlanarGraphs.getConnectedVertices(graph, vertex)) {

            if (priorItr != null) {               
               embedding.put(new DirectedEdge<V>(itr, vertex), new DirectedEdge<V>(vertex, priorItr));              
            } else {
               firstItr = itr;
            }
            priorItr = itr;
         }
         if (priorItr != null) {
            embedding.put(new DirectedEdge<V>(firstItr, vertex), new DirectedEdge<V>(vertex, priorItr));
         }
      }

      // Iterate over all the internal edges in the graph, deleting from
      // the embedding as we go
      Queue<DirectedEdge<V>> edgeQueue = new LinkedList<DirectedEdge<V>>();
      while (!embedding.isEmpty()) {
         
         // Start traversal with first internal edge         
         for (DirectedEdge<V> edge : embedding.keySet()) {
            if (!graph.isBoundary(edge.getSource(), edge.getTarget())) {
               edgeQueue.add(edge);
               break;
            }
         }

         // Only boundary edges are left
         if (edgeQueue.isEmpty()) {
            edgeQueue.add(embedding.keySet().iterator().next());
         }        

         while (!edgeQueue.isEmpty()) {

            DirectedEdge<V> edge = edgeQueue.poll();

            if (embedding.containsKey(edge)) {
               // Traverse around this face
               visitor.beginFace(edge.getSource(), edge.getTarget());
               visitor.nextVertex(edge.getSource());

               DirectedEdge<V> current = edge;
               do {                  
                  if (!current.getTarget().equals(edge.getSource())) {
                     visitor.nextVertex(current.getTarget());
                  }
                  visitor.nextEdge(current.getSource(), current.getTarget());

                  DirectedEdge<V> opposing = new DirectedEdge(current.getTarget(), current.getSource());
                  if (embedding.containsKey(opposing)
                          && !graph.isBoundary(opposing.getSource(), opposing.getTarget())) {
                     edgeQueue.add(opposing);
                  }

                  DirectedEdge<V> prev = current;
                  current = embedding.get(current);
                  embedding.remove(prev);
               } while (current != null && !current.equals(edge));

               visitor.endFace(edge.getSource(), edge.getTarget());
            }
         }
      }

      visitor.endTraversal();
   }
}
