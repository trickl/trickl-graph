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

import com.trickl.graph.edges.IntegerEdgeFactory;
import com.trickl.graph.ext.JComponentWindow;
import com.trickl.graph.planar.generate.PlanarCircleGraphGenerator;
import com.trickl.graph.vertices.IntegerVertexFactory;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JScrollPane;
import org.jgraph.JGraph;
import org.junit.Test;

public class ChrobakPayneLayoutTest {

   public ChrobakPayneLayoutTest() {
   }

   @Test
   public void testLayout() throws InterruptedException, InvocationTargetException {
      int vertices = 7;
      IntegerVertexFactory vertexFactory = new IntegerVertexFactory();

      PlanarGraph<Integer, Integer> graph = new DoublyConnectedEdgeList<Integer, Integer, Object>(new IntegerEdgeFactory(), Object.class);

      PlanarCircleGraphGenerator generator = new PlanarCircleGraphGenerator<Integer, Integer>(vertices);
      generator.generateGraph(graph, vertexFactory, null);

      ChrobakPayneLayout<Integer, Integer> chrobakPayneLayout
              = new ChrobakPayneLayout<Integer, Integer>(graph, 0.25);

      // Visual Check
      if (Boolean.parseBoolean(System.getProperty("visualTests"))) {
        JGraph jGraph = JGraphAdaptor.getDisplayGraph(graph, chrobakPayneLayout);
        JComponentWindow window = new JComponentWindow(new JScrollPane(jGraph));              
        window.showAndWait();
      }      
   }


   @Test
   public void testLayoutSmall() throws InterruptedException, InvocationTargetException {

      PlanarGraph<Integer, Integer> graph = new DoublyConnectedEdgeList<Integer, Integer, Object>(new IntegerEdgeFactory(), Object.class);
      IntegerVertexFactory vertexFactory = new IntegerVertexFactory();
      for (int i = 0; i < 20; i++) {
         graph.addVertex(vertexFactory.createVertex());
      }
      addTriangularFace(graph, 19, 18, 17);
      addTriangularFace(graph, 17, 18, 6);
      addTriangularFace(graph, 6, 18, 12);
      addTriangularFace(graph, 6, 12, 5);
      addTriangularFace(graph, 5, 12, 17);
      addTriangularFace(graph, 5, 17, 11);
      addTriangularFace(graph, 5, 11, 16);
      addTriangularFace(graph, 7, 6, 1);
      addTriangularFace(graph, 1, 6, 0);
      addTriangularFace(graph, 0, 6, 5);
      addTriangularFace(graph, 0, 5, 4);
      addTriangularFace(graph, 4, 5, 16);
      addTriangularFace(graph, 4, 16, 10);
      addTriangularFace(graph, 1, 0, 2);
      addTriangularFace(graph, 2, 0, 3);
      addTriangularFace(graph, 3, 0, 4);
      addTriangularFace(graph, 3, 4, 10);
      addTriangularFace(graph, 3, 10, 15);
      addTriangularFace(graph, 1, 2, 13);
      addTriangularFace(graph, 13, 2, 8);
      addTriangularFace(graph, 2, 3, 14);
      addTriangularFace(graph, 14, 3, 9);
      addTriangularFace(graph, 9, 3, 15);

      ChrobakPayneLayout<Integer, Integer> chrobakPayneLayout
              = new ChrobakPayneLayout<Integer, Integer>(graph, 0.2);

      // Visual Check
      if (Boolean.parseBoolean(System.getProperty("visualTests"))) {
        JGraph jGraph = JGraphAdaptor.getDisplayGraph(graph, chrobakPayneLayout);
        JComponentWindow window = new JComponentWindow(new JScrollPane(jGraph));              
        window.showAndWait();
      }
   }


   private <V, E> void addTriangularFace(PlanarGraph<V, E> graph, V firstVertex, V secondVertex, V thirdVertex) {
      graph.addEdge(firstVertex, secondVertex);
      graph.addEdge(secondVertex, thirdVertex, firstVertex, null);
      graph.addEdge(thirdVertex, firstVertex, secondVertex, secondVertex);
   }
}
