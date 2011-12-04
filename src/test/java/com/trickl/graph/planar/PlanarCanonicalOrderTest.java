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

import com.trickl.graph.vertices.IdCoordinateVertex;
import com.trickl.graph.planar.faces.IdFaceFactory;
import com.trickl.graph.planar.faces.IdFace;
import com.trickl.graph.planar.xml.XmlDcelDocument;
import com.trickl.graph.vertices.IdVertex;
import com.trickl.graph.vertices.IdVertexFactory;
import com.trickl.graph.edges.UndirectedIdEdge;
import com.trickl.graph.edges.UndirectedIdEdgeFactory;
import com.trickl.graph.planar.DoublyConnectedEdgeList;
import com.trickl.graph.planar.MaximalPlanar;
import com.trickl.graph.planar.MaximalPlanarCanonicalOrdering;
import com.trickl.graph.planar.PlanarGraph;
import com.trickl.graph.planar.PlanarGraphs;
import com.trickl.graph.generate.PlanarCircleGraphGenerator;
import com.trickl.graph.vertices.CircleVertex;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.junit.Test;
import static org.junit.Assert.*;

public class PlanarCanonicalOrderTest {

   public PlanarCanonicalOrderTest() {
   }

   @Test   
   public void testMinimal() throws InterruptedException, InvocationTargetException {
      IdVertexFactory vertexFactory = new IdVertexFactory();
      PlanarGraph<IdVertex, UndirectedIdEdge<IdVertex>> graph
              = new DoublyConnectedEdgeList<IdVertex, UndirectedIdEdge<IdVertex>, Object>(new UndirectedIdEdgeFactory<IdVertex>(), Object.class);
      
      for (int i = 0; i < 5; ++i) graph.addVertex(vertexFactory.createVertex());

      // Note the graph needs to be maximal planar
      graph.addEdge(vertexFactory.get(0), vertexFactory.get(1));
      graph.addEdge(vertexFactory.get(1), vertexFactory.get(2));
      graph.addEdge(vertexFactory.get(2), vertexFactory.get(0));
      graph.addEdge(vertexFactory.get(1), vertexFactory.get(3));
      graph.addEdge(vertexFactory.get(3), vertexFactory.get(2));
      graph.addEdge(vertexFactory.get(3), vertexFactory.get(4));
      graph.addEdge(vertexFactory.get(4), vertexFactory.get(2));
      graph.addEdge(vertexFactory.get(0), vertexFactory.get(3), vertexFactory.get(1), null);
      graph.addEdge(vertexFactory.get(4), vertexFactory.get(0), vertexFactory.get(2), null);

      MaximalPlanarCanonicalOrdering<IdVertex, UndirectedIdEdge<IdVertex>> planarCanonicalOrder
              = new MaximalPlanarCanonicalOrdering<IdVertex, UndirectedIdEdge<IdVertex>>();

      List<IdVertex> ordering = planarCanonicalOrder.getOrder(graph, vertexFactory.get(0));

      assertList(ordering, "0,3,4,2,1");
   }

   @Test
   public void testSmall() throws InterruptedException, InvocationTargetException {
      int vertices = 7;
      IdVertexFactory vertexFactory = new IdVertexFactory();
      PlanarGraph<IdVertex, UndirectedIdEdge<IdVertex>> graph = new DoublyConnectedEdgeList<IdVertex, UndirectedIdEdge<IdVertex>, Object>(new UndirectedIdEdgeFactory<IdVertex>(), Object.class);

      PlanarCircleGraphGenerator generator = new PlanarCircleGraphGenerator<IdVertex, UndirectedIdEdge<IdVertex>>(vertices, 100);
      generator.generateGraph(graph, vertexFactory, null);

      MaximalPlanar<IdVertex, UndirectedIdEdge<IdVertex>> maximalPlanar = new MaximalPlanar<IdVertex, UndirectedIdEdge<IdVertex>>();
      maximalPlanar.makeMaximalPlanar(graph);
            
      MaximalPlanarCanonicalOrdering<IdVertex, UndirectedIdEdge<IdVertex>> planarCanonicalOrder = new MaximalPlanarCanonicalOrdering<IdVertex, UndirectedIdEdge<IdVertex>>();
      
      List<IdVertex> ordering = planarCanonicalOrder.getOrder(graph, graph.getBoundary().getSource());

      assertList(ordering, "4,5,6,3,2,1,0");
   }

   @Test
   public void testLarge() throws Exception {
      
      PlanarGraph<IdVertex, UndirectedIdEdge<IdVertex>> graph = loadGraphFromFile("delaunay-graph-1000.xml");
      MaximalPlanar<IdVertex, UndirectedIdEdge<IdVertex>> maximalPlanar = new MaximalPlanar<IdVertex, UndirectedIdEdge<IdVertex>>();
      maximalPlanar.makeMaximalPlanar(graph);

      MaximalPlanarCanonicalOrdering<IdVertex, UndirectedIdEdge<IdVertex>> planarCanonicalOrder = new MaximalPlanarCanonicalOrdering<IdVertex, UndirectedIdEdge<IdVertex>>();

      List<IdVertex> boundary = new LinkedList<IdVertex>(PlanarGraphs.getBoundaryVertices(graph));
               List<IdVertex> ordering = planarCanonicalOrder.getOrder(graph, boundary.get(0));

      assertEquals(1000, ordering.size());
   }

   private PlanarGraph<IdVertex, UndirectedIdEdge<IdVertex>> loadGraphFromFile(String file) throws IOException, JAXBException {
      URL controlFile = this.getClass().getResource(file);
      InputStreamReader reader = new InputStreamReader(controlFile.openStream());

      JAXBContext context = JAXBContext.newInstance(XmlDcelDocument.class,
                                                    IdVertex.class,
                                                    IdCoordinateVertex.class,
                                                    CircleVertex.class,
                                                    UndirectedIdEdge.class,
                                                    UndirectedIdEdgeFactory.class,
                                                    IdFace.class,
                                                    IdFaceFactory.class);
      Unmarshaller unmarshaller = context.createUnmarshaller();
      XmlDcelDocument<IdVertex, UndirectedIdEdge<IdVertex>, IdFace> document =
              (XmlDcelDocument<IdVertex, UndirectedIdEdge<IdVertex>, IdFace>) unmarshaller.unmarshal(reader);
      return document.getDoublyConnectedEdgeList();
   }

   static private void assertList(List<IdVertex> list, String str) {

      StringBuffer idString = new StringBuffer();
      for (int i = 0; i < list.size(); ++i)
      {
         if (i > 0) idString.append(',');
         idString.append(list.get(i).toString());
      }
      assertEquals(str, idString.toString());
   }
}
