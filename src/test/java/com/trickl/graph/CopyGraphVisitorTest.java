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

import com.trickl.graph.Graphs;
import org.jgrapht.Graph;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.DefaultEdge;

import java.util.List;
import org.junit.Assert;

import org.junit.Test;
import static org.junit.Assert.*;

public class CopyGraphVisitorTest {

   @Test
   public void emptyGraph() throws Exception {
      Graph<Integer, DefaultEdge> graph = new SimpleGraph<Integer, DefaultEdge>(DefaultEdge.class);
      Graph<Integer, DefaultEdge> copy = new SimpleGraph<Integer, DefaultEdge>(DefaultEdge.class);
      Graphs.copy(graph, copy, null, null);
   }
   
   @Test
   public void simpleGraphCopySameVerticesAndEdges() throws Exception {
      Graph<Integer, DefaultEdge> graph = new SimpleGraph<Integer, DefaultEdge>(DefaultEdge.class);
      for (int i = 0; i < 5; ++i) graph.addVertex(i);
      graph.addEdge(0, 1);
      graph.addEdge(1, 2);
      graph.addEdge(2, 0);
      graph.addEdge(1, 3);
      graph.addEdge(2, 3);
      graph.addEdge(3, 4);
      graph.addEdge(4, 2);

      Graph<Integer, DefaultEdge> copy = new SimpleGraph<Integer, DefaultEdge>(DefaultEdge.class);
      Graphs.copy(graph, copy, null, null);
      Assert.assertEquals(5, copy.vertexSet().size());
      Assert.assertEquals(7, copy.edgeSet().size());
   }
}
