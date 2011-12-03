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

import com.trickl.graph.DepthFirstSearch;
import com.trickl.graph.SpanningSearchVisitor;
import java.util.ArrayList;
import java.util.List;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import org.junit.Test;
import static org.junit.Assert.*;

public class DepthFirstSearchTest {

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
      DepthFirstSearch<Integer, DefaultEdge> search = new DepthFirstSearch<Integer, DefaultEdge>(graph);
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
      graph.addEdge(6, 3);
      graph.addEdge(6, 7);      
      graph.addEdge(6, 8);
      DepthFirstSearch<Integer, DefaultEdge> search = new DepthFirstSearch<Integer, DefaultEdge>(graph);
      VertexOrder<Integer, DefaultEdge> indexer = new VertexOrder<Integer, DefaultEdge>();
      search.traverse(indexer);

      assertList(indexer.getOrder(), "4,3,2,7,8,6,5,1");
   }

      static private <V> void assertList(List<V> list, String str) {

      StringBuffer idString = new StringBuffer();
      for (int i = 0; i < list.size(); ++i)
      {
         if (i > 0) idString.append(',');
         idString.append(list.get(i).toString());
      }
      assertEquals(str, idString.toString());
   }
}
