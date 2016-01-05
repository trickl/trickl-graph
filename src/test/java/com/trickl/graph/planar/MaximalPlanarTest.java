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

import com.trickl.graph.edges.UndirectedIdEdge;
import com.trickl.graph.edges.UndirectedIdEdgeFactory;
import com.trickl.graph.vertices.IdVertex;
import com.trickl.graph.vertices.IdVertexFactory;
import com.trickl.graph.planar.generate.PlanarCircleGraphGenerator;
import static com.trickl.graph.planar.PlanarAssert.*;
import com.trickl.graph.planar.faces.IdFace;
import com.trickl.graph.planar.faces.IdFaceFactory;
import com.vividsolutions.jts.util.Assert;

import java.lang.reflect.InvocationTargetException;
import javax.xml.bind.JAXBException;

import org.junit.Test;

public class MaximalPlanarTest {

   public MaximalPlanarTest() {
   }

   @Test   
   public void testMinimal() throws InterruptedException, InvocationTargetException, JAXBException {
      IdVertexFactory vertexFactory = new IdVertexFactory();
      DoublyConnectedEdgeList<IdVertex,
              UndirectedIdEdge<IdVertex>,
              IdFace> graph
              = new DoublyConnectedEdgeList<IdVertex,
              UndirectedIdEdge<IdVertex>,
              IdFace>(new UndirectedIdEdgeFactory<IdVertex>(), new IdFaceFactory());
      for (int i = 0; i < 5; ++i) graph.addVertex(vertexFactory.createVertex());
      graph.addEdge(vertexFactory.get(0), vertexFactory.get(1));
      graph.addEdge(vertexFactory.get(1), vertexFactory.get(2));
      graph.addEdge(vertexFactory.get(2), vertexFactory.get(0), vertexFactory.get(1), vertexFactory.get(1));
      graph.addEdge(vertexFactory.get(1), vertexFactory.get(3));
      graph.addEdge(vertexFactory.get(3), vertexFactory.get(2), vertexFactory.get(1), vertexFactory.get(1));
      graph.addEdge(vertexFactory.get(3), vertexFactory.get(4));
      graph.addEdge(vertexFactory.get(4), vertexFactory.get(2), vertexFactory.get(3), vertexFactory.get(3));

      MaximalPlanar<IdVertex, UndirectedIdEdge<IdVertex>> maximalPlanar = new MaximalPlanar<IdVertex, UndirectedIdEdge<IdVertex>>();
      
      Assert.isTrue(!maximalPlanar.isMaximalPlanar(graph), "Graph is already maximal planar.");
      maximalPlanar.makeMaximalPlanar(graph);
      Assert.isTrue(maximalPlanar.isMaximalPlanar(graph), "Graph did not become maximal planar");      
      
      assertEmbeddingEquals(graph, vertexFactory.get(0), "4,2,1,3");
      assertEmbeddingEquals(graph, vertexFactory.get(3), "4,0,1,2");
      assertEmbeddingEquals(graph, vertexFactory.get(4), "0,3,2");
   }

   @Test   
   public void testSmall() throws InterruptedException, InvocationTargetException {
      int vertices = 7;
      IdVertexFactory vertexFactory = new IdVertexFactory();

      PlanarGraph<IdVertex, UndirectedIdEdge<IdVertex>> graph = new DoublyConnectedEdgeList<IdVertex, UndirectedIdEdge<IdVertex>, Object>(new UndirectedIdEdgeFactory<IdVertex>(), Object.class);

      PlanarCircleGraphGenerator generator = new PlanarCircleGraphGenerator<IdVertex, UndirectedIdEdge<IdVertex>>(vertices, 100);
      generator.generateGraph(graph, vertexFactory, null);
      
      MaximalPlanar<IdVertex, UndirectedIdEdge<IdVertex>> maximalPlanar = new MaximalPlanar<IdVertex, UndirectedIdEdge<IdVertex>>();
      
      Assert.isTrue(!maximalPlanar.isMaximalPlanar(graph), "Graph is already maximal planar.");
      maximalPlanar.makeMaximalPlanar(graph);
      Assert.isTrue(maximalPlanar.isMaximalPlanar(graph), "Graph did not become maximal planar");
      
      assertEmbeddingEquals(graph, vertexFactory.get(2), "1,0,3,4,5,6");
   }
}
