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

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jgrapht.Graph;

/* See http://en.wikipedia.org/wiki/Laplacian_matrix */
public class LaplacianGenerator<V, E> {

   private Graph<V, E> graph;
   private List<V> vertices;
   private Map<V, Integer> vertexIndexMap;
   private DoubleMatrix2D laplacian;
   private boolean useOutDegree;

   public LaplacianGenerator(Graph<V, E> graph) {
      this.graph = graph;
   }

   public LaplacianGenerator(Graph<V, E> graph, boolean useOutDegree) {
      this.graph = graph;
      this.useOutDegree = useOutDegree;
   }

   public DoubleMatrix2D getLaplacian() {
      if (laplacian == null) {
         int n = graph.vertexSet().size();
         laplacian = new SparseDoubleMatrix2D(n, n);

         vertices = new ArrayList<V>(n);
         vertexIndexMap = new HashMap<V, Integer>();
         int i = 0;
         for (V vertex : graph.vertexSet()) {
            vertices.add(vertex);
            vertexIndexMap.put(vertex, i);
            ++i;
         }
         
         for (V vertex : graph.vertexSet()) {
            double degree = 0;
            for (E edge : graph.edgesOf(vertex)) {
               // Use outdegree
               V source = graph.getEdgeSource(edge);
               V target = graph.getEdgeTarget(edge);
               boolean outEdge = source.equals(vertex);
               double edgeWeight = graph.getEdgeWeight(edge);

               if ((useOutDegree && outEdge)
                       || (!useOutDegree && !outEdge)) {
                  degree += edgeWeight;
                  laplacian.setQuick(vertexIndexMap.get(source),
                                     vertexIndexMap.get(target),
                    -edgeWeight);
               } else {
                  degree += edgeWeight;
                  laplacian.setQuick(vertexIndexMap.get(target),
                                     vertexIndexMap.get(source),
                    -edgeWeight);
               }              
            }

            laplacian.setQuick(vertexIndexMap.get(vertex),
                               vertexIndexMap.get(vertex), degree);
         }        
      }

      return laplacian;
   }

   public Integer getIndex(V vertex) {
      return vertexIndexMap.get(vertex);
   }

   public V getVertex(int index) {
      return vertices.get(index);
   }
}
