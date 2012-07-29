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
package com.trickl.graph.planar.generate;

import com.trickl.graph.edges.IntegerEdgeFactory;
import com.trickl.graph.ext.JComponentWindow;
import com.trickl.graph.planar.DoublyConnectedEdgeList;
import com.trickl.graph.planar.JGraphAdaptor;
import static com.trickl.graph.planar.PlanarAssert.assertEmbeddingEquals;
import com.trickl.graph.planar.PlanarGraph;
import com.trickl.graph.planar.PlanarGraphs;
import com.trickl.graph.vertices.IntegerVertexFactory;
import javax.swing.JScrollPane;
import org.jgraph.JGraph;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class PlanarCircleGraphGeneratorTest {

   @Test
   public void generateSmallLayout() throws Exception {

      int vertices = 7;

      PlanarGraph<Integer, Integer> graph = new DoublyConnectedEdgeList<Integer, Integer, Object>(new IntegerEdgeFactory(), Object.class);

      PlanarCircleGraphGenerator<Integer, Integer> generator = new PlanarCircleGraphGenerator<Integer, Integer>(vertices, 0.25);
      generator.generateGraph(graph, new IntegerVertexFactory(), null);

      assertEquals(6, PlanarGraphs.getBoundaryVertices(graph).size());
      assertEmbeddingEquals(graph, 0, "6,5,4,3,2,1");

      if (Boolean.parseBoolean(System.getProperty("visualTests"))) {
         JGraph jGraph = JGraphAdaptor.getDisplayGraph(graph, generator);
         JComponentWindow window = new JComponentWindow(new JScrollPane(jGraph));
         window.showAndWait();
      }
   }

   @Test
   public void generateLargeLayout() throws Exception {

      int vertices = 99;

      PlanarGraph<Integer, Integer> graph = new DoublyConnectedEdgeList<Integer, Integer, Object>(new IntegerEdgeFactory(), Object.class);

      PlanarCircleGraphGenerator<Integer, Integer> generator = new PlanarCircleGraphGenerator<Integer, Integer>(vertices, 0.25);
      generator.generateGraph(graph, new IntegerVertexFactory(), null);

      assertEquals(36, PlanarGraphs.getBoundaryVertices(graph).size());
      assertEmbeddingEquals(graph, 0, "6,5,4,3,2,1");
      assertEmbeddingEquals(graph, 40, "68,50,26,27,51,69");
      assertEmbeddingEquals(graph, 90, "84,83,60");
      assertEmbeddingEquals(graph, 95, "70,71");

      if (Boolean.parseBoolean(System.getProperty("visualTests"))) {
         JGraph jGraph = JGraphAdaptor.getDisplayGraph(graph, generator);
         JComponentWindow window = new JComponentWindow(new JScrollPane(jGraph));
         window.showAndWait();
      }
   }
}
