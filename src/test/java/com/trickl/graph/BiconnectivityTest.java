package com.trickl.graph;

import com.trickl.graph.Biconnectivity;
import org.jgrapht.Graph;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.DefaultEdge;

import java.util.List;

import org.junit.Test;
import static org.junit.Assert.*;

public class BiconnectivityTest {

   @Test
   public void emptyGraph() throws Exception {
      Graph<Integer, DefaultEdge> graph = new SimpleGraph<Integer, DefaultEdge>(DefaultEdge.class);

      Biconnectivity<Integer, DefaultEdge> connectivity = new Biconnectivity<Integer, DefaultEdge>(graph);
      assertEquals(0, connectivity.getComponents());
   }
   
   @Test
   public void minimalConnectedGraph() throws Exception {
      Graph<Integer, DefaultEdge> graph = new SimpleGraph<Integer, DefaultEdge>(DefaultEdge.class);
      for (int i = 0; i < 5; ++i) graph.addVertex(i);
      graph.addEdge(0, 1);
      graph.addEdge(1, 2);
      graph.addEdge(2, 0);
      graph.addEdge(1, 3);
      graph.addEdge(2, 3);
      graph.addEdge(3, 4);
      graph.addEdge(4, 2);

      Biconnectivity<Integer, DefaultEdge> connectivity = new Biconnectivity<Integer, DefaultEdge>(graph);
      assertTrue(connectivity.isBiconnected());
      assertEquals(1, connectivity.getComponents());
   }

   @Test
   public void minimalTwoComponentGraph() throws Exception {
      Graph<Integer, DefaultEdge> graph = new SimpleGraph<Integer, DefaultEdge>(DefaultEdge.class);
      for (int i = 0; i < 5; ++i) graph.addVertex(i);
      // First Part
      graph.addEdge(0, 1);
      graph.addEdge(1, 2);
      graph.addEdge(2, 0);

      // Second Part
      graph.addEdge(2, 3);
      graph.addEdge(3, 4);
      graph.addEdge(4, 2);

      Biconnectivity<Integer, DefaultEdge> connectivity = new Biconnectivity<Integer, DefaultEdge>(graph);
      assertFalse(connectivity.isBiconnected());
      assertEquals(2, connectivity.getComponents());

      List<Integer> articulationPoints = connectivity.getArticulationPoints();
      assertEquals(1, articulationPoints.size());
      assertEquals(2, (int) articulationPoints.get(0));
   }
}
