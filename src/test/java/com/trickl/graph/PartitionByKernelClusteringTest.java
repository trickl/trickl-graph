/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.trickl.graph;

import cern.colt.matrix.DoubleMatrix1D;
import com.trickl.graph.PartitionByKernelClustering;
import com.trickl.cluster.KernelKMeans;
import java.util.Map;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.junit.Assert;
import org.junit.Test;

public class PartitionByKernelClusteringTest {

   public PartitionByKernelClusteringTest() {
   }

   @Test
   public void testMinimalGraphCluster() {
      System.out.println("getMinimalGraphCluster");
      int totalClusters = 2;
      Graph<Integer, DefaultEdge> graph = new SimpleGraph<Integer, DefaultEdge>(DefaultEdge.class);
      for (int i = 0; i < 7; ++i) {
         graph.addVertex(i);
      }

      // Create a simple, connected graph
      graph.addEdge(0, 1);
      graph.addEdge(1, 2);
      graph.addEdge(2, 0);
      graph.addEdge(1, 3);
      graph.addEdge(2, 3);
      graph.addEdge(3, 4);
      graph.addEdge(4, 2);
      graph.addEdge(3, 5);
      graph.addEdge(5, 4);
      graph.addEdge(5, 6);
      graph.addEdge(6, 4);
      
      PartitionByKernelClustering<Integer, DefaultEdge> partitionAlgorithm =
              new PartitionByKernelClustering<Integer, DefaultEdge>();
      partitionAlgorithm.setKernelClusterAlgorithm(new KernelKMeans());
      partitionAlgorithm.partition(graph, totalClusters);

      Map<Integer, Integer> partitions = partitionAlgorithm.getPartition();
   
      // Membership of vertex 3 is ambiguous, but the rest should be divided
      // into the following clusters
      Assert.assertEquals(partitions.get(0), partitions.get(1));
      Assert.assertEquals(partitions.get(0), partitions.get(2));
      Assert.assertNotSame(partitions.get(4), partitions.get(0));
      Assert.assertEquals(partitions.get(4), partitions.get(5));
      Assert.assertEquals(partitions.get(4), partitions.get(6));
   }
}
