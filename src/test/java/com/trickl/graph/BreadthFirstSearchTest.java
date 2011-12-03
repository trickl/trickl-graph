package com.trickl.graph;

import com.trickl.graph.BreadthFirstSearch;
import com.trickl.graph.SpanningSearchVisitor;
import java.util.ArrayList;
import java.util.List;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import org.junit.Test;
import static org.junit.Assert.*;

public class BreadthFirstSearchTest {

   public static class VertexOrder<V, E> implements SpanningSearchVisitor<V, E> {

      protected List<V> order;

      public VertexOrder() {
         order = new ArrayList<V>();
      }

      @Override
      public void startVertex(V u) {
      }

      @Override
      public void discoverVertex(V u) {         
      }

      @Override
      public void initializeVertex(V u) {
      }

      @Override
      public void examineEdge(V source, V target) {
      }

      @Override
      public void treeEdge(V source, V target) {
      }

      @Override
      public void backEdge(V source, V target) {
      }

      @Override
      public void forwardOrCrossEdge(V source, V target) {
      }

      @Override
      public void finishVertex(V u) {
         order.add(u);
      }

      public List<V> getOrder()
      {
         return order;
      }
   };

   @Test
   public void emptyGraph() throws Exception {
      Graph<Integer, DefaultEdge> graph = new SimpleDirectedGraph<Integer, DefaultEdge>(DefaultEdge.class);
      BreadthFirstSearch<Integer, DefaultEdge> search = new BreadthFirstSearch<Integer, DefaultEdge>(graph);
      VertexOrder<Integer, DefaultEdge> indexer = new VertexOrder<Integer, DefaultEdge>();
      search.traverse(indexer);
   }

   // http://en.wikipedia.org/wiki/File:Tree_edges.svg
   @Test
   public void minimalGraph() throws Exception {
      Graph<Integer, DefaultEdge> graph = new SimpleDirectedGraph<Integer, DefaultEdge>(DefaultEdge.class);
      for (int i = 1; i <= 8; ++i) graph.addVertex(i);
      // Define the edges in clockwise order
      graph.addEdge(1, 2);
      graph.addEdge(1, 5);
      graph.addEdge(1, 8);
      graph.addEdge(2, 3);
      graph.addEdge(3, 4);
      graph.addEdge(4, 2);            
      graph.addEdge(5, 6);      
      graph.addEdge(6, 7);
      graph.addEdge(6, 3);
      graph.addEdge(6, 8);
      BreadthFirstSearch<Integer, DefaultEdge> search = new BreadthFirstSearch<Integer, DefaultEdge>(graph);
      VertexOrder<Integer, DefaultEdge> indexer = new VertexOrder<Integer, DefaultEdge>();
      search.traverse(indexer);

      assertEquals(8, indexer.getOrder().size());
      assertEquals(1, (int) indexer.getOrder().get(0));
      assertEquals(2, (int) indexer.getOrder().get(1));
      assertEquals(5, (int) indexer.getOrder().get(2));
      assertEquals(8, (int) indexer.getOrder().get(3));
      assertEquals(3, (int) indexer.getOrder().get(4));
      assertEquals(6, (int) indexer.getOrder().get(5));
      assertEquals(4, (int) indexer.getOrder().get(6));
      assertEquals(7, (int) indexer.getOrder().get(7));
   }
}
