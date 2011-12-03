package com.trickl.graph.planar;

import com.trickl.graph.vertices.IdVertex;
import com.trickl.graph.vertices.IdVertexFactory;
import com.trickl.graph.edges.UndirectedIdEdge;
import com.trickl.graph.edges.UndirectedIdEdgeFactory;
import com.trickl.graph.planar.DoublyConnectedEdgeList;
import com.trickl.graph.planar.LeftistPlanarCanonicalOrdering;
import com.trickl.graph.planar.PlanarCanonicalOrdering;
import com.trickl.graph.planar.PlanarGraph;
import java.util.List;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class LeftistPlanarCanonicalOrderingTest {

   public LeftistPlanarCanonicalOrderingTest() {
   }

   @BeforeClass
   public static void setUpClass() throws Exception {
   }

   @AfterClass
   public static void tearDownClass() throws Exception {
   }

   @Test
   public void testPaperExample() {
      IdVertexFactory vertexFactory = new IdVertexFactory();
      PlanarGraph<IdVertex, UndirectedIdEdge<IdVertex>> graph = new DoublyConnectedEdgeList<IdVertex, UndirectedIdEdge<IdVertex>, Object>(new UndirectedIdEdgeFactory<IdVertex>(), Object.class);

      for (int i = 0; i < 16; ++i) {
         graph.addVertex(vertexFactory.createVertex());
      }

      // From Figure 1b) in the paper.
      graph.addEdge(vertexFactory.get(1), vertexFactory.get(2));
      graph.addEdge(vertexFactory.get(2), vertexFactory.get(5), vertexFactory.get(1), null);
      graph.addEdge(vertexFactory.get(5), vertexFactory.get(4), vertexFactory.get(2), null);
      graph.addEdge(vertexFactory.get(4), vertexFactory.get(3), vertexFactory.get(5), null);
      graph.addEdge(vertexFactory.get(3), vertexFactory.get(1), vertexFactory.get(4), null);
      graph.addEdge(vertexFactory.get(3), vertexFactory.get(6), vertexFactory.get(1), null);
      graph.addEdge(vertexFactory.get(6), vertexFactory.get(1), vertexFactory.get(3), null);
      graph.addEdge(vertexFactory.get(2), vertexFactory.get(13), vertexFactory.get(5), null);
      graph.addEdge(vertexFactory.get(13), vertexFactory.get(12), vertexFactory.get(2), null);
      graph.addEdge(vertexFactory.get(12), vertexFactory.get(5), vertexFactory.get(13), null);
      graph.addEdge(vertexFactory.get(13), vertexFactory.get(11), vertexFactory.get(12), null);
      graph.addEdge(vertexFactory.get(11), vertexFactory.get(12), vertexFactory.get(13), null);
      graph.addEdge(vertexFactory.get(11), vertexFactory.get(9), vertexFactory.get(12), null);
      graph.addEdge(vertexFactory.get(9), vertexFactory.get(8), vertexFactory.get(11), null);
      graph.addEdge(vertexFactory.get(8), vertexFactory.get(5), vertexFactory.get(9), null);
      graph.addEdge(vertexFactory.get(8), vertexFactory.get(7), vertexFactory.get(5), null);
      graph.addEdge(vertexFactory.get(7), vertexFactory.get(4), vertexFactory.get(8), null);
      graph.addEdge(vertexFactory.get(9), vertexFactory.get(7), vertexFactory.get(8), null);
      graph.addEdge(vertexFactory.get(2), vertexFactory.get(14), vertexFactory.get(13), null);
      graph.addEdge(vertexFactory.get(14), vertexFactory.get(13), vertexFactory.get(2), null);
      graph.addEdge(vertexFactory.get(14), vertexFactory.get(10), vertexFactory.get(13), null);
      graph.addEdge(vertexFactory.get(10), vertexFactory.get(9), vertexFactory.get(14), null);
      graph.addEdge(vertexFactory.get(10), vertexFactory.get(7), vertexFactory.get(9), null);
      graph.addEdge(vertexFactory.get(14), vertexFactory.get(15), vertexFactory.get(10), null);
      graph.addEdge(vertexFactory.get(15), vertexFactory.get(10), vertexFactory.get(14), null);
      graph.addEdge(vertexFactory.get(15), vertexFactory.get(6), vertexFactory.get(10), null);
      graph.addEdge(vertexFactory.get(15), vertexFactory.get(1), vertexFactory.get(6), null);

      PlanarCanonicalOrdering<IdVertex, UndirectedIdEdge<IdVertex>> planarCanonicalOrder = new LeftistPlanarCanonicalOrdering<IdVertex, UndirectedIdEdge<IdVertex>>();

      List<IdVertex> ordering = planarCanonicalOrder.getOrder(graph, vertexFactory.get(1));

      assertList(ordering, "1,2,3,4,5,6,7,8,9,10,11,12,13,14,15");
   }

   static private void assertList(List<IdVertex> list, String str) {
      StringBuffer idString = new StringBuffer();
      for (int i = 0; i < list.size(); ++i) {
         if (i > 0) {
            idString.append(',');
         }
         idString.append(list.get(i).toString());
      }
      assertEquals(str, idString.toString());
   }
}
