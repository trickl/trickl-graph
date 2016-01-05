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
import com.trickl.graph.edges.UndirectedIdEdge;
import com.trickl.graph.edges.UndirectedIdEdgeFactory;
import com.trickl.graph.planar.faces.IdFace;
import com.trickl.graph.planar.faces.IdFaceFactory;
import com.trickl.graph.planar.generate.PlanarCircleGraphGenerator;
import com.trickl.graph.planar.xml.XmlDcelDocument;
import com.trickl.graph.vertices.CircleVertex;
import com.trickl.graph.vertices.IdCoordinateVertex;
import com.trickl.graph.vertices.IntegerVertexFactory;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class PlanarCanonicalOrderTest {

   public PlanarCanonicalOrderTest() {
   }

   @Test   
   public void testMinimal() throws InterruptedException, InvocationTargetException {
      IntegerVertexFactory vertexFactory = new IntegerVertexFactory();
      PlanarGraph<Integer, Integer> graph
              = new DoublyConnectedEdgeList<Integer, Integer, Object>(new IntegerEdgeFactory(), Object.class);
      
      for (int i = 0; i < 5; ++i) graph.addVertex(vertexFactory.createVertex());

      // Note the graph needs to be maximal planar
      graph.addEdge(0, 1);
      graph.addEdge(1, 2);
      graph.addEdge(2, 0);
      graph.addEdge(1, 3);
      graph.addEdge(3, 2);
      graph.addEdge(3, 4);
      graph.addEdge(4, 2);
      graph.addEdge(0, 3, 1, null);
      graph.addEdge(4, 0, 2, null);

      MaximalPlanarCanonicalOrdering<Integer, Integer> planarCanonicalOrder
              = new MaximalPlanarCanonicalOrdering<Integer, Integer>();

      List<Integer> ordering = planarCanonicalOrder.getOrder(graph, 0);

      assertList(ordering, "0,3,4,2,1");
   }

   @Test
   public void testSmall() throws InterruptedException, InvocationTargetException {
      int vertices = 7;
      IntegerVertexFactory vertexFactory = new IntegerVertexFactory();
      PlanarGraph<Integer, Integer> graph = new DoublyConnectedEdgeList<Integer, Integer, Object>(new IntegerEdgeFactory(), Object.class);

      PlanarCircleGraphGenerator generator = new PlanarCircleGraphGenerator<Integer, Integer>(vertices, 0.5);      
      generator.generateGraph(graph, vertexFactory, null);

      MaximalPlanar<Integer, Integer> maximalPlanar = new MaximalPlanar<Integer, Integer>();
      maximalPlanar.makeMaximalPlanar(graph);
            
      MaximalPlanarCanonicalOrdering<Integer, Integer> planarCanonicalOrder = new MaximalPlanarCanonicalOrdering<Integer, Integer>();
      
      List<Integer> ordering = planarCanonicalOrder.getOrder(graph, graph.getBoundary().getSource());

      assertList(ordering, "6,1,2,5,4,3,0");
   }

   @Test
   public void testLarge() throws Exception {
      
      PlanarGraph<Integer, Integer> graph = loadGraphFromFile("delaunay-graph-1000.xml");
      MaximalPlanar<Integer, Integer> maximalPlanar = new MaximalPlanar<Integer, Integer>();
      maximalPlanar.makeMaximalPlanar(graph);

      MaximalPlanarCanonicalOrdering<Integer, Integer> planarCanonicalOrder = new MaximalPlanarCanonicalOrdering<Integer, Integer>();

      List<Integer> boundary = new LinkedList<Integer>(PlanarGraphs.getBoundaryVertices(graph));
               List<Integer> ordering = planarCanonicalOrder.getOrder(graph, boundary.get(0));

      assertEquals(1000, ordering.size());
   }

   private PlanarGraph<Integer, Integer> loadGraphFromFile(String file) throws IOException, JAXBException {
      URL controlFile = this.getClass().getResource(file);
      InputStreamReader reader = new InputStreamReader(controlFile.openStream());

      JAXBContext context = JAXBContext.newInstance(XmlDcelDocument.class,
                                                    Integer.class,
                                                    IdCoordinateVertex.class,
                                                    CircleVertex.class,
                                                    UndirectedIdEdge.class,
                                                    UndirectedIdEdgeFactory.class,
                                                    IdFace.class,
                                                    IdFaceFactory.class);
      Unmarshaller unmarshaller = context.createUnmarshaller();
      XmlDcelDocument<Integer, Integer, IdFace> document =
              (XmlDcelDocument<Integer, Integer, IdFace>) unmarshaller.unmarshal(reader);
      return document.getDoublyConnectedEdgeList();
   }

   static private void assertList(List<Integer> list, String str) {

      StringBuilder idString = new StringBuilder();
      for (int i = 0; i < list.size(); ++i)
      {
         if (i > 0) idString.append(',');
         idString.append(list.get(i).toString());
      }
      assertEquals(str, idString.toString());
   }
}
