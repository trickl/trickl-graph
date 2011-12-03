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
