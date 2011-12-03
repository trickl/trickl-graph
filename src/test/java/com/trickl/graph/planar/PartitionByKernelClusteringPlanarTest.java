/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
