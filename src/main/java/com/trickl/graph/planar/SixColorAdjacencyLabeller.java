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
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A simple six-color planar graph vertex labeller detailed in: TWO LINEAR-TIME
 * ALGORITHMS FOR FIVE-COLORING A PLANAR GRAPH by David Matula, Yossi Shiloach,
 * Robert Tarjan O(n) time complexity
 *
 * @author tgee
 */
public class SixColorAdjacencyLabeller<T, A> implements Labeller<T> {
   
   private final Map<Integer, Set<T>> labelItems = new LinkedHashMap<>();
   private final Map<T, Integer> itemLabels = new LinkedHashMap<>();
   
   public SixColorAdjacencyLabeller(Set<T> items, Function<T, Set<A>> getAdjacencies, BiFunction<A, T, T> getAdjacent) {
      generateLabels(items, getAdjacencies, getAdjacent);
   }

   @Override
   public int getLabelCount() {      
      return labelItems.size();
   }

   @Override
   public int getLabel(T vertex) {      
      return itemLabels.get(vertex);
   }

   @Override
   public Set<T> getMembers(int label) {      
      return labelItems.get(label);
   }

   private void generateLabels(Set<T> items, Function<T, Set<A>> getAdjacencies, BiFunction<A, T, T> getAdjacent) {      
      // Step 1. [Establish degree lists.] For each j where 0- j - n - 1, form a doubly
      // linked list of all vertices of G of degree j.      
      Map<T, List<T>> adjacencyList = new HashMap<T, List<T>>();
            
      Set[] degreeLists = new Set[items.size()];
      for (T item : items) {
         // Setup the degree list structure
         int degree = getAdjacencies.apply(item).size();
         if (degreeLists[degree] == null) {
            for (int j = degree; j >= 0 && degreeLists[j] == null; j--) {
               degreeLists[j] = new HashSet<T>();
            }
         }
         degreeLists[degree].add(item);

         // Setup the adjacency list structure
         adjacencyList.put(item, new LinkedList<T>());
         for (A edge : getAdjacencies.apply(item)) {
            T adjacent = getAdjacent.apply(edge, item);
            adjacencyList.get(item).add(adjacent);
         }
      }

      // Step 2. [Label vertices smallest degree last.] For i = n, n - 1, n*- 1,. . . , 1
      // designate the first vertex of the non-vacuous j degree list of smallest j
      // as vertex v(i). Delete v(i) from the j degree list. For each vertex v’ that
      // was adjacent to v(i) in G and remains in some degree list, say j', delete
      // v’ from the j' degree list and insert v’ in the j' - 1 degree list.
      Stack<T> colorOrder = new Stack<T>();
      for (int i = 0; i < items.size(); ++i) {
         // Find non-vacuous degree list of smallest degree
         int j = 0;
         while (j < items.size() - i && degreeLists[j].isEmpty()) {
            ++j;
         }

         Set<T> degreeList = degreeLists[j];
         T vertex = degreeList.iterator().next();
         // Remove the first vertex and update structures
         degreeList.remove(vertex);
         Set<T> adjacencies = new HashSet<T>(adjacencyList.get(vertex));
         for (T adjacent : adjacencies) {
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
      for (T vertex : colorOrder) {
         // Use a mask to 'turn off' adjacent colors
         int labelMask = 63; // 0b111111
         for (A edge : getAdjacencies.apply(vertex)) {
            T adjacent = getAdjacent.apply( edge, vertex);
            if (itemLabels.containsKey(adjacent)) {
               // Switch off the adjacent label 
               labelMask &= 63 - (1 << itemLabels.get(adjacent));
            }
         }
         int label = 0;
         for (; (labelMask & 1) == 0; ++label) {
            labelMask = labelMask >> 1;
         }

         // Update the label structures
         itemLabels.put(vertex, label);

         if (!labelItems.containsKey(label)) {
            labelItems.put(label, new HashSet<T>());
         }
         labelItems.get(label).add(vertex);
      }
   }
}