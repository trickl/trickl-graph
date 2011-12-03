package com.trickl.graph.planar;

import com.trickl.graph.planar.BoyerMyrvoldPlanarity;
import java.lang.reflect.InvocationTargetException;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.Graph;
import org.jgrapht.graph.SimpleGraph;
import org.junit.Ignore;
import org.junit.Test;

public class BoyerMyrboldPlanarityTest {

   public BoyerMyrboldPlanarityTest() {
   }

   @Test
   @Ignore("BoyerMyrvoldPlanaity is not finished, expected to throw errors")
   public void testMinimalConnectedGraph() throws InterruptedException, InvocationTargetException {
   
      Graph<Integer, DefaultEdge> graph = new SimpleGraph<Integer, DefaultEdge>(DefaultEdge.class);
      for (int i = 0; i < 5; ++i) graph.addVertex(i);
      graph.addEdge(0, 1);
      graph.addEdge(1, 2);
      graph.addEdge(2, 0);
      graph.addEdge(1, 3);
      graph.addEdge(2, 3);
      graph.addEdge(3, 4);
      graph.addEdge(4, 2);

      BoyerMyrvoldPlanarity<Integer, DefaultEdge> boyerMyrvoldPlanarity = new BoyerMyrvoldPlanarity<Integer, DefaultEdge>(graph);
      assert(boyerMyrvoldPlanarity.isPlanar());

      // TODO: Validate the embedding
   }
}
