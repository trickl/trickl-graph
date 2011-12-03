package com.trickl.graph;

import com.trickl.graph.Connectivity;
import org.jgrapht.Graph;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.DefaultEdge;

import org.junit.Test;
import static org.junit.Assert.*;

public class ConnectivityTest {

   @Test
   public void emptyGraph() throws Exception {
      Graph<Integer, DefaultEdge> graph = new SimpleGraph<Integer, DefaultEdge>(DefaultEdge.class);

      Connectivity<Integer, DefaultEdge> connectivity = new Connectivity<Integer, DefaultEdge>(graph);
      assertEquals(0, connectivity.getComponents());
   }

   // http://en.wikipedia.org/wiki/File:Tree_edges.svg
   @Test
   public void minimalConnectedGraph() throws Exception {
      Graph<Integer, DefaultEdge> graph = new SimpleGraph<Integer, DefaultEdge>(DefaultEdge.class);
      for (int i = 0; i < 8; ++i) graph.addVertex(i);
      graph.addEdge(0, 1);
      graph.addEdge(1, 2);
      graph.addEdge(2, 3);
      graph.addEdge(3, 1);
      graph.addEdge(0, 7);
      graph.addEdge(0, 4);
      graph.addEdge(4, 5);
      graph.addEdge(5, 7);
      graph.addEdge(5, 6);
      graph.addEdge(5, 2);

      Connectivity<Integer, DefaultEdge> connectivity = new Connectivity<Integer, DefaultEdge>(graph);
      assertTrue(connectivity.isConnected());
      assertEquals(1, connectivity.getComponents());
   }

   @Test
   public void minimalTwoComponentGraph() throws Exception {
      Graph<Integer, DefaultEdge> graph = new SimpleGraph<Integer, DefaultEdge>(DefaultEdge.class);
      for (int i = 0; i < 8; ++i) graph.addVertex(i);
      // First Part
      graph.addEdge(1, 2);
      graph.addEdge(2, 3);
      graph.addEdge(3, 1);

      // Second Part
      graph.addEdge(0, 7);
      graph.addEdge(0, 4);
      graph.addEdge(4, 5);
      graph.addEdge(5, 7);
      graph.addEdge(5, 6);

      Connectivity<Integer, DefaultEdge> connectivity = new Connectivity<Integer, DefaultEdge>(graph);
      assertFalse(connectivity.isConnected());
      assertEquals(2, connectivity.getComponents());
   }
}
