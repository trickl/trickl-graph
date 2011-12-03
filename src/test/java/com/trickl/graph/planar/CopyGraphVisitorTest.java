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

import com.trickl.graph.planar.faces.IdFaceFactory;
import com.trickl.graph.planar.faces.IdFace;
import com.trickl.graph.planar.xml.XmlDcelDocument;
import com.trickl.graph.vertices.CircleVertex;
import com.trickl.graph.edges.UndirectedIdEdge;
import com.trickl.graph.edges.UndirectedIdEdgeFactory;
import com.trickl.graph.planar.ChrobakPayneLayout;
import com.trickl.graph.planar.DoublyConnectedEdgeList;
import com.trickl.graph.planar.FaceFactory;
import com.trickl.graph.planar.MaximalPlanar;
import com.trickl.graph.planar.PlanarGraph;
import com.trickl.graph.planar.PlanarGraphs;
import com.trickl.graph.planar.PlanarLayout;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.jgrapht.EdgeFactory;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author tgee
 */
public class CopyGraphVisitorTest {

    public CopyGraphVisitorTest() {
    }

   @BeforeClass
   public static void setUpClass() throws Exception {
   }

   @AfterClass
   public static void tearDownClass() throws Exception {
   }

   @Test
   @Ignore
   public void testLargeCopy() throws Exception {
      System.out.println("largeCopy");

      PlanarGraph<CircleVertex, UndirectedIdEdge<CircleVertex>> inputGraph =
              loadGraphFromFile("adjacency-graph-10000.xml");

      MaximalPlanar<CircleVertex, UndirectedIdEdge<CircleVertex>> inputMaximalPlanar
                 = new MaximalPlanar<CircleVertex, UndirectedIdEdge<CircleVertex>>();
      Assert.assertTrue("Error occurred before copy. Interior is not triangulated.",
              inputMaximalPlanar.isInteriorTriangulated(inputGraph));

      
      EdgeFactory<CircleVertex, UndirectedIdEdge<CircleVertex>> edgeFactory =
                 new UndirectedIdEdgeFactory<CircleVertex>();
      FaceFactory<CircleVertex, IdFace> faceFactory = new IdFaceFactory<CircleVertex>();
      
      PlanarGraph<CircleVertex, UndirectedIdEdge<CircleVertex>> outputGraph =
              new DoublyConnectedEdgeList<CircleVertex, UndirectedIdEdge<CircleVertex>, IdFace>(edgeFactory, faceFactory);
      PlanarGraphs.copy(inputGraph, outputGraph, null, null);
      int boundaryVertices = PlanarGraphs.getBoundaryVertices(outputGraph).size();
      System.out.println("Graph has " + boundaryVertices + " boundary vertices.");

      PlanarLayout<CircleVertex> outputLayout = new ChrobakPayneLayout<CircleVertex, UndirectedIdEdge<CircleVertex>>(outputGraph, 50);

      MaximalPlanar<CircleVertex, UndirectedIdEdge<CircleVertex>> outputMaximalPlanar
                 = new MaximalPlanar<CircleVertex, UndirectedIdEdge<CircleVertex>>();
      Assert.assertTrue("Error occurred after copy. Interior is not triangulated.",
              outputMaximalPlanar.isInteriorTriangulated(outputGraph));     

      /*
      DrawingPad pad = new DrawingPad(720, 600, 20, 20, "Test Drawing Pad - Circle");
      pad.getViewport().setRect(new Rectangle.Double(-1200, -1200, 2400, 2400));
      Container graphComponents = new Container();
      graphComponents.add(new JPlanarGraph<CircleVertex, UndirectedIdEdge<CircleVertex>>(outputGraph, outputLayout, Color.black));
      pad.getViewport().setView(graphComponents);
      PlanarGraphLabelProvider<CircleVertex, UndirectedIdEdge<CircleVertex>> outputLabels
              = new PlanarGraphLabelProvider<CircleVertex, UndirectedIdEdge<CircleVertex>>(outputGraph, outputLayout, 40, 40);
      for (JComponent component : outputLabels.getVertexLabels(pad.getViewport())) {
         pad.getLabelPane().add(component);
      }
      
      pad.showAndWait();
       * 
       */
   }


   private PlanarGraph<CircleVertex, UndirectedIdEdge<CircleVertex>> loadGraphFromFile(String file) throws IOException, JAXBException {
      URL controlFile = this.getClass().getResource(file);
      InputStreamReader reader = new InputStreamReader(controlFile.openStream());

      JAXBContext context = JAXBContext.newInstance(XmlDcelDocument.class,
              CircleVertex.class,
              UndirectedIdEdge.class,
              UndirectedIdEdgeFactory.class,
              IdFace.class,
              IdFaceFactory.class);
      XmlDcelDocument<CircleVertex, UndirectedIdEdge<CircleVertex>, IdFace> document =
              new XmlDcelDocument<CircleVertex, UndirectedIdEdge<CircleVertex>, IdFace>();
      document.read(reader, context);
      return document.getDoublyConnectedEdgeList();
   }
}