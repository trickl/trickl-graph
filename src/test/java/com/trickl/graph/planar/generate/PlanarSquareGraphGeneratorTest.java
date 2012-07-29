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

import com.trickl.graph.edges.UndirectedIdEdge;
import com.trickl.graph.edges.UndirectedIdEdgeFactory;
import com.trickl.graph.ext.JComponentWindow;
import com.trickl.graph.planar.DoublyConnectedEdgeList;
import com.trickl.graph.planar.JGraphAdaptor;
import static com.trickl.graph.planar.PlanarAssert.assertEmbeddingEquals;
import com.trickl.graph.planar.PlanarGraph;
import com.trickl.graph.planar.PlanarGraphs;
import com.trickl.graph.vertices.IdVertex;
import com.trickl.graph.vertices.IdVertexFactory;
import javax.swing.JScrollPane;
import org.jgraph.JGraph;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class PlanarSquareGraphGeneratorTest {

    @Test
   public void generateLayout() throws Exception {

      int vertices = 99;

      PlanarGraph<IdVertex, UndirectedIdEdge<IdVertex>> graph = new DoublyConnectedEdgeList<IdVertex, UndirectedIdEdge<IdVertex>, Object>(new UndirectedIdEdgeFactory<IdVertex>(), Object.class);

      PlanarSquareGraphGenerator<IdVertex, UndirectedIdEdge<IdVertex>> generator = new PlanarSquareGraphGenerator<IdVertex, UndirectedIdEdge<IdVertex>>(vertices, 0.25);
      generator.generateGraph(graph, new IdVertexFactory(), null);

      assertEquals(36, PlanarGraphs.getBoundaryVertices(graph).size());
      for (IdVertex vertex : graph.vertexSet())
      {
         // Test the embedding
         if (vertex.getId().equals(11)) {
            assertEmbeddingEquals(graph, vertex, "21,10,1,12");
         }
         else if (vertex.getId().equals(98)) {
            assertEmbeddingEquals(graph, vertex, "97,88");
         }
         else if (vertex.getId().equals(49)) {
            assertEmbeddingEquals(graph, vertex, "59,48,39");
         }
         else if (vertex.getId().equals(90)) {
            assertEmbeddingEquals(graph, vertex, "91,80");
         }
      }
      
      if (Boolean.parseBoolean(System.getProperty("visualTests"))) {
        JGraph jGraph = JGraphAdaptor.getDisplayGraph(graph, generator);
        JComponentWindow window = new JComponentWindow(new JScrollPane(jGraph));              
        window.showAndWait();
      }
   }
}
