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
