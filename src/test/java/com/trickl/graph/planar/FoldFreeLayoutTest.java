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
package com.trickl.graph.planar;

import com.trickl.graph.vertices.IdVertex;
import com.trickl.graph.vertices.IdVertexFactory;
import com.trickl.graph.edges.UndirectedIdEdge;
import com.trickl.graph.edges.UndirectedIdEdgeFactory;
import com.trickl.graph.generate.PlanarSquareGraphGenerator;
import com.vividsolutions.jts.geom.Coordinate;
import java.lang.reflect.InvocationTargetException;
import org.junit.Ignore;
import org.junit.Test;

public class FoldFreeLayoutTest {

   public FoldFreeLayoutTest() {
   }

  
   @Test
   @Ignore("Visual test with undefined results")
   public void testLayout() throws InterruptedException, InvocationTargetException {
      /* TODO: Define a test with predictable results

      int vertices = 121;

      PlanarGraph<IdVertex, UndirectedIdEdge<IdVertex>> graph = new DoublyConnectedEdgeList<IdVertex, UndirectedIdEdge<IdVertex>, Object>(new UndirectedIdEdgeFactory<IdVertex>(), Object.class);

      PlanarSquareGraphGenerator generator = new PlanarSquareGraphGenerator<IdVertex, UndirectedIdEdge<IdVertex>>(vertices);
      generator.generateGraph(graph, new IdVertexFactory(), null);
      
      FoldFreeLayout<IdVertex, UndirectedIdEdge<IdVertex>> foldFreeLayout = new FoldFreeLayout(graph);
      foldFreeLayout.setScale(0.8);
      foldFreeLayout.setCentre(new Coordinate(8, 8));

      // Display the results
      GraphWindow<IdVertex, UndirectedIdEdge<IdVertex>> window =
              new GraphWindow<IdVertex, UndirectedIdEdge<IdVertex>>(graph);

      int graphWidth = 400;
      int minVertexPixelWidth = 5;
      int margin = 15;
      int vertexPixelWidth = Math.max(
              (int) (graphWidth / Math.sqrt(vertices)) - margin,
              minVertexPixelWidth);
      int vertexPixelHeight = vertexPixelWidth;

      for (IdVertex vertex : graph.vertexSet())
      {
         Coordinate position = foldFreeLayout.getCoordinate(vertex);
         window.setVertexPosition(vertex, (int) (position.x * (vertexPixelWidth + margin)),
                                          (int) (position.y * (vertexPixelWidth + margin)));
         window.setVertexSize(vertex, vertexPixelWidth, vertexPixelHeight);
      }

      window.showAndWait();
    * *
    */
   }    
}
