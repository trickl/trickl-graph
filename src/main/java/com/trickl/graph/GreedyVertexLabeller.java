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
package com.trickl.graph;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.jgrapht.Graph;
import org.jgrapht.UndirectedGraph;

/**
 * A wrapper around the ChromaticNumber greedy algorithm for vertex colouring
 * @author tgee
 */
public class GreedyVertexLabeller<V, E> implements Labeller<V> {
   
   private Graph<V, E> graph;
   private Map<Integer, Set<V>> labelVertices = new HashMap<Integer, Set<V>>();
   private Map<V, Integer> vertexLabels = new HashMap<V, Integer>();
   
   public GreedyVertexLabeller(UndirectedGraph<V, E> graph) {
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
   
   private void generateLabels(Graph<V, E> graph) {
      throw new UnsupportedOperationException("Requires JGraph 0.8.3");
      /*
      this.graph = graph;            
      labelVertices = ChromaticNumber.findGreedyColoredGroups(graph);      
      vertexLabels.clear();
      for (int label : labelVertices.keySet()) {
         for (V vertex : labelVertices.get(label)) {
            vertexLabels.put(vertex, label);
         }
      }
      * 
      */
   }
}
