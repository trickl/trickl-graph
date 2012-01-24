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

import com.trickl.graph.Labeller;
import java.util.*;
import org.jgrapht.Graphs;

/**
 * A simple six-color planar graph vertex labeller detailed in: TWO LINEAR-TIME
 * ALGORITHMS FOR FIVE-COLORING A PLANAR GRAPH by David Matula, Yossi Shiloach,
 * Robert Tarjan O(n) time complexity
 *
 * @author tgee
 */
public class SixColorVertexLabeller<V, E> implements Labeller<V> {
   
   private Map<Integer, Set<V>> labelVertices = new HashMap<Integer, Set<V>>();
   private Map<V, Integer> vertexLabels = new HashMap<V, Integer>();
   
   public SixColorVertexLabeller(PlanarGraph<V, E> graph) {
      generateLabels(graph);
   }

   @Override
   public int getLabelCount() {      
      return labelVertices.size();
   }

   @Override
   public int getLabel(V vertex) {      
      return vertexLabels.get(vertex);
   }

   @Override
   public Set<V> getMembers(int label) {      
      return labelVertices.get(label);
   }

   private void generateLabels(PlanarGraph<V, E> graph) {      
      // Step 1. [Establish degree lists.] For each j where 0- j - n - 1, form a doubly
      // linked list of all vertices of G of degree j.
      Set<V> vertices = graph.vertexSet();
      Map<V, List<V>> adjacencyList = new HashMap<V, List<V>>();
      Set[] degreeLists = new Set[vertices.size()];
      for (V vertex : vertices) {
         // Setup the degree list structure
         int degree = graph.edgesOf(vertex).size();
         if (degreeLists[degree] == null) {
            for (int j = degree; j >= 0 && degreeLists[j] == null; j--) {
               degreeLists[j] = new HashSet<V>();
            }
         }
         degreeLists[degree].add(vertex);

         // Setup the adjacency list structure
         adjacencyList.put(vertex, new LinkedList<V>());
         for (E edge : graph.edgesOf(vertex)) {
            V adjacent = Graphs.getOppositeVertex(graph, edge, vertex);
            adjacencyList.get(vertex).add(adjacent);
         }
      }

      // Step 2. [Label vertices smallest degree last.] For i = n, n - 1, n*- 1,. . . , 1
      // designate the first vertex of the non-vacuous j degree list of smallest j
      // as vertex v(i). Delete v(i) from the j degree list. For each vertex v’ that
      // was adjacent to v(i) in G and remains in some degree list, say j', delete
      // v’ from the j' degree list and insert v’ in the j' - 1 degree list.
      Stack<V> colorOrder = new Stack<V>();
      for (int i = 0; i < vertices.size(); ++i) {
         // Find non-vacuous degree list of smallest degree
         int j = 0;
         while (j < vertices.size() - i && degreeLists[j].isEmpty()) {
            ++j;
         }

         Set<V> degreeList = degreeLists[j];
         V vertex = degreeList.iterator().next();
         // Remove the first vertex and update structures
         degreeList.remove(vertex);
         Set<V> adjacencies = new HashSet<V>(adjacencyList.get(vertex));
         for (V adjacent : adjacencies) {
            int adjacentDegree = adjacencyList.get(adjacent).size();
            degreeLists[adjacentDegree].remove(adjacent);
            degreeLists[adjacentDegree - 1].add(adjacent);
            adjacencyList.get(vertex).remove(adjacent);
            adjacencyList.get(adjacent).remove(vertex);
         }

         // Label from last to first
         colorOrder.push(vertex);
      }

      // Step 3. [Color vertices.] For i = 1,2,. . . , n, assign vertex v(i) the smallest color
      // value (which must be some integer between one and six) not occuring on
      // the vertices adjacent to v(i) that have already been colored.
      for (V vertex : colorOrder) {
         // Use a mask to 'turn off' adjacent colors
         int labelMask = 63; // 0b111111
         for (E edge : graph.edgesOf(vertex)) {
            V adjacent = Graphs.getOppositeVertex(graph, edge, vertex);
            if (vertexLabels.containsKey(adjacent)) {
               // Switch off the adjacent label 
               labelMask &= 63 - (1 << vertexLabels.get(adjacent));
            }
         }
         int label = 0;
         for (; (labelMask & 1) == 0; ++label) {
            labelMask = labelMask >> 1;
         }

         // Update the label structures
         vertexLabels.put(vertex, label);

         if (!labelVertices.containsKey(label)) {
            labelVertices.put(label, new HashSet<V>());
         }
         labelVertices.get(label).add(vertex);
      }
   }
}