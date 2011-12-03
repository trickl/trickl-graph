package com.trickl.graph.planar;

import com.trickl.graph.vertices.IdVertex;
import com.trickl.graph.vertices.IdVertexFactory;
import com.trickl.graph.edges.UndirectedIdEdge;
import com.trickl.graph.edges.UndirectedIdEdgeFactory;
import com.trickl.graph.planar.DoublyConnectedEdgeList;
import com.trickl.graph.planar.PlanarGraph;
import org.junit.Test;
import static org.junit.Assert.*;

public class DcelVertexTest {

   public DcelVertexTest() {
   }

   /**
    * Test the construction of a graph when joining two separate islands of edges
    */
   @Test
   public void testAddEdgeDisconnectedFaces() {
        IdVertexFactory vertexFactory = new IdVertexFactory();
      PlanarGraph<IdVertex, UndirectedIdEdge<IdVertex>> graph
              = new DoublyConnectedEdgeList<IdVertex,
              UndirectedIdEdge<IdVertex>,
              Object>(new UndirectedIdEdgeFactory<IdVertex>(), Object.class);
      for (int i = 0; i < 6; ++i) graph.addVertex(vertexFactory.createVertex());

      graph.addEdge(vertexFactory.get(0), vertexFactory.get(1));
      graph.addEdge(vertexFactory.get(0), vertexFactory.get(5), vertexFactory.get(1), null);
      graph.addEdge(vertexFactory.get(4), vertexFactory.get(3));
      graph.addEdge(vertexFactory.get(4), vertexFactory.get(5), vertexFactory.get(3), null);
      graph.addEdge(vertexFactory.get(5), vertexFactory.get(1), vertexFactory.get(0), null);
      graph.addEdge(vertexFactory.get(5), vertexFactory.get(2), vertexFactory.get(1), null);
      graph.addEdge(vertexFactory.get(5), vertexFactory.get(3), vertexFactory.get(2), null);
      graph.addEdge(vertexFactory.get(2), vertexFactory.get(1), vertexFactory.get(5), null);
      graph.addEdge(vertexFactory.get(3), vertexFactory.get(2), vertexFactory.get(5), null);

      assertEquals(9, graph.edgeSet().size());
   }

   /**
    * Test the construction of a graph when joining two separate islands of edges
    */
   @Test
   public void testAddEdgeExteriorEdge() {
        IdVertexFactory vertexFactory = new IdVertexFactory();
      PlanarGraph<IdVertex, UndirectedIdEdge<IdVertex>> graph
              = new DoublyConnectedEdgeList<IdVertex,
              UndirectedIdEdge<IdVertex>,
              Object>(new UndirectedIdEdgeFactory<IdVertex>(), Object.class);
      for (int i = 0; i < 6; ++i) graph.addVertex(vertexFactory.createVertex());

      graph.addEdge(vertexFactory.get(0), vertexFactory.get(1));
      graph.addEdge(vertexFactory.get(0), vertexFactory.get(5), vertexFactory.get(1), null);
      graph.addEdge(vertexFactory.get(4), vertexFactory.get(3));
      graph.addEdge(vertexFactory.get(4), vertexFactory.get(5), vertexFactory.get(3), null);
      graph.addEdge(vertexFactory.get(1), vertexFactory.get(2), vertexFactory.get(0), null);

      graph.addEdge(vertexFactory.get(1), vertexFactory.get(5), vertexFactory.get(2), vertexFactory.get(4));
      graph.addEdge(vertexFactory.get(5), vertexFactory.get(3), vertexFactory.get(1), null);
      graph.addEdge(vertexFactory.get(2), vertexFactory.get(5), vertexFactory.get(1), null);
      graph.addEdge(vertexFactory.get(2), vertexFactory.get(3), vertexFactory.get(1), null);

      assertEquals(9, graph.edgeSet().size());
   }
}