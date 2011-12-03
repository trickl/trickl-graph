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
import com.trickl.graph.planar.ChrobakPayneLayout;
import com.trickl.graph.planar.DoublyConnectedEdgeList;
import com.trickl.graph.planar.PlanarGraph;
import com.trickl.graph.generate.PlanarCircleGraphGenerator;
import java.lang.reflect.InvocationTargetException;
import org.junit.Ignore;
import org.junit.Test;

public class ChropakPayneLayoutTest {

   public ChropakPayneLayoutTest() {
   }

   // TODO: Migrate test to toolbox
   @Test
   @Ignore("Visual test with undefined results")
   public void testLayout() throws InterruptedException, InvocationTargetException {
      int vertices = 7;
      IdVertexFactory vertexFactory = new IdVertexFactory();

      PlanarGraph<IdVertex, UndirectedIdEdge<IdVertex>> graph = new DoublyConnectedEdgeList<IdVertex, UndirectedIdEdge<IdVertex>, Object>(new UndirectedIdEdgeFactory<IdVertex>(), Object.class);

      PlanarCircleGraphGenerator generator = new PlanarCircleGraphGenerator<IdVertex, UndirectedIdEdge<IdVertex>>(vertices, 100);
      generator.generateGraph(graph, vertexFactory, null);

      ChrobakPayneLayout<IdVertex, UndirectedIdEdge<IdVertex>> chrobakPayneLayout
              = new ChrobakPayneLayout<IdVertex, UndirectedIdEdge<IdVertex>>(graph, 100);

      // Display the results
      // TODO Show layout using drawing pad and planar face traversal
      /*
      DrawingPad pad = new DrawingPad(720, 600, 20, 20, "Unit test - Chrobak Payne Layout");
      pad.getViewport().setRect(new Rectangle.Double(-1200, -1200, 2400, 2400));
      pad.getViewport().setView(new JPlanarGraphView(graph, chrobakPayneLayout, Color.black));
      pad.showAndWait();
       *
       */

      // Display the results
      /*
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
         Coordinate position = chrobakPayneLayout.getCoordinate(vertex);
         window.setVertexPosition(vertex, (int) (position.x * (vertexPixelWidth + margin)),
                                          (int) (position.y * (vertexPixelWidth + margin)));
         window.setVertexSize(vertex, vertexPixelWidth, vertexPixelHeight);
      }

      window.showAndWait();
       *
       */
   }


   @Test
   public void testLayoutSmall() throws InterruptedException, InvocationTargetException {

      PlanarGraph<IdVertex, UndirectedIdEdge<IdVertex>> graph = new DoublyConnectedEdgeList<IdVertex, UndirectedIdEdge<IdVertex>, Object>(new UndirectedIdEdgeFactory<IdVertex>(), Object.class);
      IdVertexFactory vertexFactory = new IdVertexFactory();
      for (int i = 0; i < 20; i++) {
         graph.addVertex(vertexFactory.createVertex());
      }
      addTriangularFace(graph, vertexFactory.get(19), vertexFactory.get(18), vertexFactory.get(17));
      addTriangularFace(graph, vertexFactory.get(17), vertexFactory.get(18), vertexFactory.get(6));
      addTriangularFace(graph, vertexFactory.get(6), vertexFactory.get(18), vertexFactory.get(12));
      addTriangularFace(graph, vertexFactory.get(6), vertexFactory.get(12), vertexFactory.get(5));
      addTriangularFace(graph, vertexFactory.get(5), vertexFactory.get(12), vertexFactory.get(17));
      addTriangularFace(graph, vertexFactory.get(5), vertexFactory.get(17), vertexFactory.get(11));
      addTriangularFace(graph, vertexFactory.get(5), vertexFactory.get(11), vertexFactory.get(16));
      addTriangularFace(graph, vertexFactory.get(7), vertexFactory.get(6), vertexFactory.get(1));
      addTriangularFace(graph, vertexFactory.get(1), vertexFactory.get(6), vertexFactory.get(0));
      addTriangularFace(graph, vertexFactory.get(0), vertexFactory.get(6), vertexFactory.get(5));
      addTriangularFace(graph, vertexFactory.get(0), vertexFactory.get(5), vertexFactory.get(4));
      addTriangularFace(graph, vertexFactory.get(4), vertexFactory.get(5), vertexFactory.get(16));
      addTriangularFace(graph, vertexFactory.get(4), vertexFactory.get(16), vertexFactory.get(10));
      addTriangularFace(graph, vertexFactory.get(1), vertexFactory.get(0), vertexFactory.get(2));
      addTriangularFace(graph, vertexFactory.get(2), vertexFactory.get(0), vertexFactory.get(3));
      addTriangularFace(graph, vertexFactory.get(3), vertexFactory.get(0), vertexFactory.get(4));
      addTriangularFace(graph, vertexFactory.get(3), vertexFactory.get(4), vertexFactory.get(10));
      addTriangularFace(graph, vertexFactory.get(3), vertexFactory.get(10), vertexFactory.get(15));
      addTriangularFace(graph, vertexFactory.get(1), vertexFactory.get(2), vertexFactory.get(13));
      addTriangularFace(graph, vertexFactory.get(13), vertexFactory.get(2), vertexFactory.get(8));
      addTriangularFace(graph, vertexFactory.get(2), vertexFactory.get(3), vertexFactory.get(14));
      addTriangularFace(graph, vertexFactory.get(14), vertexFactory.get(3), vertexFactory.get(9));
      addTriangularFace(graph, vertexFactory.get(9), vertexFactory.get(3), vertexFactory.get(15));

      ChrobakPayneLayout<IdVertex, UndirectedIdEdge<IdVertex>> chrobakPayneLayout
              = new ChrobakPayneLayout<IdVertex, UndirectedIdEdge<IdVertex>>(graph, 30);

      // Display the results
      // TODO Show layout using drawing pad and planar face traversal
      /* TODO Migrate test to toolbox
      DrawingPad pad = new DrawingPad(720, 600, 20, 20, "Test Drawing Pad - Circle Graph");
      pad.getViewport().setRect(new Rectangle.Double(-100, -100, 1200, 1200));
      pad.getViewport().setView(new JPlanarGraphView(graph, chrobakPayneLayout, Color.black));
      PlanarGraphLabelProvider<IdVertex, UndirectedIdEdge<IdVertex>> delaunayLabels = new PlanarGraphLabelProvider<IdVertex, UndirectedIdEdge<IdVertex>>(graph, chrobakPayneLayout, 40, 40);
      for (JComponent component : delaunayLabels.getVertexLabels(pad.getViewport())) {
         pad.getLabelPane().add(component);
      }
      pad.showAndWait();
       *
       */
   }


   private <V, E> void addTriangularFace(PlanarGraph<V, E> graph, V firstVertex, V secondVertex, V thirdVertex) {
      graph.addEdge(firstVertex, secondVertex);
      graph.addEdge(secondVertex, thirdVertex, firstVertex, null);
      graph.addEdge(thirdVertex, firstVertex, secondVertex, secondVertex);
   }
}
