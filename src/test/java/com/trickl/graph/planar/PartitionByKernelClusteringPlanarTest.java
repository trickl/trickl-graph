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

import com.trickl.graph.PartitionByKernelClustering;
import com.trickl.graph.edges.UndirectedIdEdge;
import com.trickl.graph.edges.UndirectedIdEdgeFactory;
import com.trickl.graph.generate.PlanarCircleGraphGenerator;
import com.trickl.graph.vertices.IdVertex;
import com.trickl.graph.vertices.IdVertexFactory;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

public class PartitionByKernelClusteringPlanarTest {

   public PartitionByKernelClusteringPlanarTest() {
   }

   @Test
   public void testCircularGraphClusters() {
      System.out.println("testCircularGraphClusters");
      int totalClusters = 4;
      int vertices = 19;
      PlanarGraph<IdVertex, UndirectedIdEdge<IdVertex>> graph = new DoublyConnectedEdgeList<IdVertex, UndirectedIdEdge<IdVertex>, Object>(new UndirectedIdEdgeFactory<IdVertex>(), Object.class);

      PlanarCircleGraphGenerator<IdVertex, UndirectedIdEdge<IdVertex>> generator = new PlanarCircleGraphGenerator<IdVertex, UndirectedIdEdge<IdVertex>>(vertices);
      IdVertexFactory vertexFactory = new IdVertexFactory();
      generator.generateGraph(graph, vertexFactory, null);      

      PartitionByKernelClustering<IdVertex, UndirectedIdEdge<IdVertex>> partitioner =
              new PartitionByKernelClustering<IdVertex, UndirectedIdEdge<IdVertex>>();
      partitioner.partition(graph, totalClusters);
      Map<IdVertex, Integer> partitions = partitioner.getPartition();

      // Check the four corners of the graph belong to different partitions      
      Assert.assertNotSame(partitions.get(vertexFactory.get(8)), partitions.get(vertexFactory.get(11)));
      Assert.assertNotSame(partitions.get(vertexFactory.get(8)), partitions.get(vertexFactory.get(15)));
      Assert.assertNotSame(partitions.get(vertexFactory.get(8)), partitions.get(vertexFactory.get(18)));
      Assert.assertNotSame(partitions.get(vertexFactory.get(11)), partitions.get(vertexFactory.get(15)));
      Assert.assertNotSame(partitions.get(vertexFactory.get(11)), partitions.get(vertexFactory.get(18)));
      Assert.assertNotSame(partitions.get(vertexFactory.get(15)), partitions.get(vertexFactory.get(18)));

   }

   @Test
   public void testLargeGraphFewClusters() {
      System.out.println("testLargeGraphFewClusters");
      int totalClusters = 10;
      int vertices = 500;
      PlanarGraph<IdVertex, UndirectedIdEdge<IdVertex>> graph = new DoublyConnectedEdgeList<IdVertex, UndirectedIdEdge<IdVertex>, Object>(new UndirectedIdEdgeFactory<IdVertex>(), Object.class);

      PlanarCircleGraphGenerator<IdVertex, UndirectedIdEdge<IdVertex>> generator = new PlanarCircleGraphGenerator<IdVertex, UndirectedIdEdge<IdVertex>>(vertices);
      IdVertexFactory vertexFactory = new IdVertexFactory();
      generator.generateGraph(graph, vertexFactory, null);

      PartitionByKernelClustering<IdVertex, UndirectedIdEdge<IdVertex>> partitioner =
              new PartitionByKernelClustering<IdVertex, UndirectedIdEdge<IdVertex>>();
      partitioner.partition(graph, totalClusters);
   }
   
   @Test
   public void testLargeGraphManyClusters() {
      System.out.println("testLargeGraphManyClusters");
      int totalClusters = 400;
      int vertices = 500;
      PlanarGraph<IdVertex, UndirectedIdEdge<IdVertex>> graph = new DoublyConnectedEdgeList<IdVertex, UndirectedIdEdge<IdVertex>, Object>(new UndirectedIdEdgeFactory<IdVertex>(), Object.class);

      PlanarCircleGraphGenerator<IdVertex, UndirectedIdEdge<IdVertex>> generator = new PlanarCircleGraphGenerator<IdVertex, UndirectedIdEdge<IdVertex>>(vertices);
      IdVertexFactory vertexFactory = new IdVertexFactory();
      generator.generateGraph(graph, vertexFactory, null);

      PartitionByKernelClustering<IdVertex, UndirectedIdEdge<IdVertex>> partitioner =
              new PartitionByKernelClustering<IdVertex, UndirectedIdEdge<IdVertex>>();
      partitioner.partition(graph, totalClusters);
   }
}
