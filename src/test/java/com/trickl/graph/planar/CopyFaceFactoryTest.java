package com.trickl.graph.planar;

import com.trickl.graph.edges.UndirectedIdEdge;
import com.trickl.graph.edges.UndirectedIdEdgeFactory;
import com.trickl.graph.planar.CopyFaceFactory;
import com.trickl.graph.planar.DoublyConnectedEdgeList;
import com.trickl.graph.planar.PlanarFaceGraph;
import com.trickl.graph.planar.PlanarGraphs;
import com.trickl.graph.generate.PlanarCircleGraphGenerator;
import com.trickl.graph.planar.faces.IdFace;
import com.trickl.graph.planar.faces.IdFaceFactory;
import com.trickl.graph.vertices.IdVertex;
import com.trickl.graph.vertices.IdVertexFactory;
import org.junit.*;

public class CopyFaceFactoryTest {

   @Test
   public void testCopyStructureOnly() throws Exception {
      // Create a graph with face ids >= 100
      IdFaceFactory<IdVertex> faceFactory = new IdFaceFactory<IdVertex>();
      faceFactory.setNextId(100);

      PlanarFaceGraph<IdVertex, UndirectedIdEdge<IdVertex>, IdFace> graph =
              new DoublyConnectedEdgeList<IdVertex, UndirectedIdEdge<IdVertex>, IdFace>(new UndirectedIdEdgeFactory<IdVertex>(), faceFactory);

      IdVertexFactory vertexFactory = new IdVertexFactory();
      PlanarCircleGraphGenerator generator =
              new PlanarCircleGraphGenerator<IdVertex, UndirectedIdEdge<IdVertex>>(7);

      generator.generateGraph(graph, vertexFactory, null);

      // Copy the graph
      PlanarFaceGraph<IdVertex, UndirectedIdEdge<IdVertex>, IdFace> copyGraph =
              new DoublyConnectedEdgeList<IdVertex, UndirectedIdEdge<IdVertex>, IdFace>(new UndirectedIdEdgeFactory<IdVertex>(), new CopyFaceFactory(graph));
      PlanarGraphs.copy(graph, copyGraph, null, null);

      Assert.assertEquals(7, copyGraph.faceSet().size());

      // Check that the new graph has the same faces as the old graph
      // TODO: Think of a better way of validating the copied faces
      int minId = Integer.MAX_VALUE;
      int maxId = Integer.MIN_VALUE;
      for (IdFace copyFace : graph.faceSet()) {
         minId = Math.min(minId, copyFace.getId());
         maxId = Math.max(maxId, copyFace.getId());
      }

      Assert.assertEquals(100, minId);
      Assert.assertEquals(106, maxId);
   }
}
