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

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Map;
import java.util.Hashtable;
import org.jgrapht.Graphs;

public class MaximalPlanarCanonicalOrdering<V, E> implements PlanarCanonicalOrdering<V, E>{

   private enum State {

      PROCESSED(0),
      UNPROCESSED(1),
      ONE_NEIGHBOR_PROCESSED(2),
      READY(3);

      private final int value;

      private State(int value) {
         this.value = value;
      }

      public int getValue() {
         return value;
      }
   };

   @Override
   public List<V> getOrder(PlanarGraph<V, E> graph,
                           V first) {
      if (graph == null || first == null) {
         throw new NullPointerException();
      }
      V second = PlanarGraphs.getPrevVertexOnBoundary(graph, first);

      List<V> ordering = new ArrayList<V>(graph.vertexSet().size());

      Queue<V> readyQueue = new LinkedList<V>();
      Map<V, V> processedNeighbors = new Hashtable<V, V>(graph.vertexSet().size());
      Map<V, Integer> status = new Hashtable<V, Integer>();
      for (V vertex : graph.vertexSet()) {
         status.put(vertex, State.UNPROCESSED.getValue());
      }

      readyQueue.add(first);
      status.put(first, State.READY.getValue());
      readyQueue.add(second);
      status.put(second, State.READY.getValue());

      while (!readyQueue.isEmpty()) {
         V u = readyQueue.poll();

         if (status.get(u) != State.READY.getValue() && !u.equals(second)) {
            continue;
         }

         List<E> embeddingList = new ArrayList<E>(graph.edgesOf(u));

         int priorEdgeItr = embeddingList.size() - 1;

         // Skip self loops
         while (graph.getEdgeSource(embeddingList.get(priorEdgeItr)).
                 equals(graph.getEdgeTarget(embeddingList.get(priorEdgeItr)))) {
            --priorEdgeItr;
         }

         for (int edgeItr = 0, edgeEndItr = embeddingList.size(); edgeItr != edgeEndItr; ++edgeItr) {
            E edge = embeddingList.get(edgeItr);
            int nextEdgeItr = (edgeItr + 1) % embeddingList.size();

            V v = graph.getEdgeSource(edge).equals(u) ? graph.getEdgeTarget(edge) : graph.getEdgeSource(edge);
            V priorVertex = Graphs.getOppositeVertex(graph, embeddingList.get(priorEdgeItr), u);
            V nextVertex = Graphs.getOppositeVertex(graph, embeddingList.get(nextEdgeItr), u);
            
            // Need priorVertex, u, v, and nextVertex to all be
            // distinct. This is possible, since the input graph is
            // triangulated. It'll be true all the time in a simple
            // graph, but loops and parallel edges cause some complications.
            if (priorVertex.equals(v) || priorVertex.equals(u)) {
               priorEdgeItr = edgeItr;
               continue;
            }

            // Skip any self-loops
            if (u.equals(v)) {
               continue;
            }

            // Move next_edge_itr (and next_vertex) forwards
            // past any loops or parallel edges
            while (nextVertex.equals(v) || nextVertex.equals(u)) {
               nextEdgeItr = (nextEdgeItr + 1) % embeddingList.size();
               nextVertex = Graphs.getOppositeVertex(graph, embeddingList.get(nextEdgeItr), u);
            }


            if (status.get(v) == State.UNPROCESSED.getValue()) {
               status.put(v, State.ONE_NEIGHBOR_PROCESSED.getValue());
               processedNeighbors.put(v, u);
            } else if (status.get(v) == State.ONE_NEIGHBOR_PROCESSED.getValue()) {
               V x = processedNeighbors.get(v);
               // Are edges (v,u) and (v,x) adjacent in the planar
               // embedding? if so, set status[v] = ready. otherwise, set
               // status[v] = ready pending.
               if ((nextVertex.equals(x)
               && !(first.equals(u) && second.equals(x)))
                || (priorVertex.equals(x)
               && !(first.equals(x) && second.equals(u)))) {
                  status.put(v, State.READY.getValue());
               } else {
                  status.put(v, State.READY.getValue() + 1);
               }
            } else if (status.get(v) > State.ONE_NEIGHBOR_PROCESSED.getValue()) {
               // check the two edges before and after (v,u) in the planar
               // embedding, and update status[v] accordingly
               boolean processedPrior = false;
               if (status.get(priorVertex) == State.PROCESSED.getValue()) {
                  processedPrior = true;
               }

               boolean processedNext = false;
               if (status.get(nextVertex) == State.PROCESSED.getValue()) {
                  processedNext = true;
               }

               if (!processedPrior && !processedNext) {
                  status.put(v, status.get(v) + 1);
               } else if (processedPrior && processedNext) {
                  status.put(v, status.get(v) - 1);
               }
            }

            if (status.get(v) == State.READY.getValue()) {
               readyQueue.add(v);
            }

            priorEdgeItr = edgeItr;

         }

         status.put(u, State.PROCESSED.getValue());
         ordering.add(u);
      }

      return ordering;
   }
}
