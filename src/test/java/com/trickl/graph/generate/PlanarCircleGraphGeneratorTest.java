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
package com.trickl.graph.generate;

import com.trickl.graph.generate.PlanarCircleGraphGenerator;
import com.trickl.graph.vertices.IdVertexFactory;
import com.trickl.graph.vertices.IdVertex;
import com.trickl.graph.edges.UndirectedIdEdge;
import com.trickl.graph.edges.UndirectedIdEdgeFactory;
import com.trickl.graph.planar.DoublyConnectedEdgeList;
import com.trickl.graph.planar.PlanarGraph;
import com.trickl.graph.planar.PlanarGraphs;
import java.awt.Color;
import java.awt.Rectangle;
import static com.trickl.graph.planar.PlanarAssert.*;

import org.junit.Ignore;

import org.junit.Test;
import static org.junit.Assert.*;

public class PlanarCircleGraphGeneratorTest {

   /* TODO: Move test into graph toolbox
   @Test
   @Ignore("Visual test, no assertions")
   public void drawLayout() throws Exception {

      int vertices = 19;

      PlanarGraph<IdVertex, UndirectedIdEdge<IdVertex>> graph = new DoublyConnectedEdgeList<IdVertex, UndirectedIdEdge<IdVertex>, Object>(new UndirectedIdEdgeFactory<IdVertex>(), Object.class);

      PlanarCircleGraphGenerator<IdVertex, UndirectedIdEdge<IdVertex>> generator = new PlanarCircleGraphGenerator<IdVertex, UndirectedIdEdge<IdVertex>>(vertices, 100);
      generator.generateGraph(graph, new IdVertexFactory(), null);

      // TODO Show layout using drawing pad and planar face traversal
      DrawingPad pad = new DrawingPad(720, 600, 20, 20, "Test Drawing Pad - Circle Graph");
      pad.getViewport().setRect(new Rectangle.Double(-1200, -1200, 2400, 2400));
      pad.getViewport().setView(new JPlanarGraphView(graph, generator, Color.black));
      pad.showAndWait();
   }
*/

   @Test
   public void generateLayout() throws Exception {

      int vertices = 99;

      PlanarGraph<IdVertex, UndirectedIdEdge<IdVertex>> graph = new DoublyConnectedEdgeList<IdVertex, UndirectedIdEdge<IdVertex>, Object>(new UndirectedIdEdgeFactory<IdVertex>(), Object.class);

      PlanarCircleGraphGenerator<IdVertex, UndirectedIdEdge<IdVertex>> generator = new PlanarCircleGraphGenerator<IdVertex, UndirectedIdEdge<IdVertex>>(vertices);
      generator.generateGraph(graph, new IdVertexFactory(), null);

      assertEquals(36, PlanarGraphs.getBoundaryVertices(graph).size());
      for (IdVertex vertex : graph.vertexSet())
      {
         // Test the embedding
         if (vertex.getId().equals(0)) {
            assertEmbeddingEquals(graph, vertex, "6,5,4,3,2,1");
         }
         else if (vertex.getId().equals(40)) {
            assertEmbeddingEquals(graph, vertex, "68,67,49,25,26,50");
         }
         else if (vertex.getId().equals(90)) {
            assertEmbeddingEquals(graph, vertex, "84,60,73");
         }
         else if (vertex.getId().equals(95)) {
            assertEmbeddingEquals(graph, vertex, "70,69");
         }
      }
   }
}
