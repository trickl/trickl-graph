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

import cern.colt.function.IntIntDoubleFunction;
import com.trickl.cluster.KernelSvdKMeans;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.jet.math.Functions;
import com.trickl.cluster.ClusterAlgorithm;
import com.trickl.cluster.KernelKMeans;
import com.trickl.cluster.KernelPairwiseNearestNeighbour;
import com.trickl.matrix.ColtSvdAlgorithm;
import com.trickl.matrix.SingularValueDecompositionAlgorithm;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.jgrapht.Graph;

/**
 * See Graph Nodes Clustering based on the Commute-Time Kernel
 * @author tgee
 */
public class PartitionByKernelClustering<V, E> implements PartitionAlgorithm<V, E> {
      
   private ClusterAlgorithm kernelClusterAlgorithm = new KernelPairwiseNearestNeighbour();
   private VertexKernelGenerator<V, E> vertexKernelGenerator = new RatioCutKernelGenerator();
   private Map<V, Integer> partition = new HashMap<V, Integer>();
   
   public PartitionByKernelClustering() {
   }

   @Override
   public void partition(Graph<V, E> graph, int partitions) {
      DoubleMatrix2D kernel = vertexKernelGenerator.getKernel(graph);
      kernelClusterAlgorithm.cluster(kernel, partitions);

      DoubleMatrix2D matrixPartition = kernelClusterAlgorithm.getPartition();
      partition.clear();
      matrixPartition.forEachNonZero(new IntIntDoubleFunction() {

         @Override
         public double apply(int i, int j, double value) {
            if (value > 0) {
               partition.put(vertexKernelGenerator.getVertex(i), j);
            }
            return value;
         }
      });
   }

   @Override
   public Map<V, Integer> getPartition() {
      return partition;
   }

   public ClusterAlgorithm getKernelClusterAlgorithm() {
      return kernelClusterAlgorithm;
   }

   public void setKernelClusterAlgorithm(ClusterAlgorithm kernelClusterAlgorithm) {
      this.kernelClusterAlgorithm = kernelClusterAlgorithm;
   }

   public VertexKernelGenerator getVertexKernelGenerator() {
      return vertexKernelGenerator;
   }

   public void setVertexKernelGenerator(VertexKernelGenerator vertexKernelGenerator) {
      this.vertexKernelGenerator = vertexKernelGenerator;
   }
}
