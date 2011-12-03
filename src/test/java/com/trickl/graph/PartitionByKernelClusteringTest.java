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
