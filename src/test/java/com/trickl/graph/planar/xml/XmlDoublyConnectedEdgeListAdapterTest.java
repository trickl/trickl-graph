package com.trickl.graph.planar.xml;

import com.trickl.graph.edges.UndirectedIdEdge;
import com.trickl.graph.edges.UndirectedIdEdgeFactory;
import com.trickl.graph.planar.DoublyConnectedEdgeList;
import com.trickl.graph.planar.faces.IdFace;
import com.trickl.graph.planar.faces.IdFaceFactory;
import com.trickl.graph.planar.xml.XmlDcelDocument;
import com.trickl.graph.vertices.IdVertex;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import javax.xml.bind.JAXBContext;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.ElementNameAndAttributeQualifier;
import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import org.jgrapht.EdgeFactory;
import static org.junit.Assert.*;
import org.junit.Test;

public class XmlDoublyConnectedEdgeListAdapterTest {

   @Test
   public void transformToXML() throws Exception
   {
      XmlDcelDocument<IdVertex, UndirectedIdEdge<IdVertex>, IdFace>
              document = new XmlDcelDocument<IdVertex, UndirectedIdEdge<IdVertex>, IdFace>();
      document.setDoublyConnectedEdgeList(createSquareDcel());
      StringWriter writer = new StringWriter();
      JAXBContext context = JAXBContext.newInstance(XmlDcelDocument.class,
                                                    IdVertex.class,
                                                    UndirectedIdEdge.class,
                                                    UndirectedIdEdgeFactory.class,
                                                    IdFace.class,
                                                    IdFaceFactory.class);
      document.write(writer, context);

      // DEBUG
      //System.out.println(writer.toString());

      URL controlFile = this.getClass().getResource("dcel.square.xml");
      InputStreamReader controlReader = new InputStreamReader(controlFile.openStream());
      StringReader reader = new StringReader(writer.toString());      
      Diff xmlDiff = new Diff(controlReader, reader);
      xmlDiff.overrideElementQualifier(new ElementNameAndAttributeQualifier("id"));
      assertXMLEqual("Generated XML not as expected", xmlDiff, true);
   }

   @Test
   public void transformFromXML() throws Exception
   {      
      JAXBContext context = JAXBContext.newInstance(XmlDcelDocument.class,
                                                    IdVertex.class,
                                                    UndirectedIdEdge.class,
                                                    UndirectedIdEdgeFactory.class,
                                                    IdFace.class,
                                                    IdFaceFactory.class);
      XmlDcelDocument<IdVertex, UndirectedIdEdge<IdVertex>, IdFace> document =
              new XmlDcelDocument<IdVertex, UndirectedIdEdge<IdVertex>, IdFace>();
      URL controlFile = this.getClass().getResource("dcel.square.xml");
      InputStreamReader reader = new InputStreamReader(controlFile.openStream());
      document.read(reader, context);

      assertNotNull(document);
      assertNotNull(document.getDoublyConnectedEdgeList());
      assertEquals(3, document.getDoublyConnectedEdgeList().getFaceMap().size());
      assertEquals(5, document.getDoublyConnectedEdgeList().getEdgeMap().size());
      assertEquals(4, document.getDoublyConnectedEdgeList().getVertexMap().size());
      assertTrue(document.getDoublyConnectedEdgeList().getEdgeFactory() instanceof UndirectedIdEdgeFactory);
      assertTrue(document.getDoublyConnectedEdgeList().getFaceFactory() instanceof IdFaceFactory);
   }

   private DoublyConnectedEdgeList<IdVertex, UndirectedIdEdge<IdVertex>, IdFace> createSquareDcel()
   {
      EdgeFactory<IdVertex, UndirectedIdEdge<IdVertex>> edgeFactory = new UndirectedIdEdgeFactory<IdVertex>();
      DoublyConnectedEdgeList<IdVertex, UndirectedIdEdge<IdVertex>, IdFace> square = new DoublyConnectedEdgeList(edgeFactory, new IdFaceFactory());
      IdVertex a = new IdVertex(0);
      IdVertex b = new IdVertex(1);
      IdVertex c = new IdVertex(2);
      IdVertex d = new IdVertex(3);

      square.addEdge(a, b);
      square.addEdge(b, d, a, null);
      square.addEdge(d, a, b, null);
      square.addEdge(b, c, d, null);
      square.addEdge(c, d, b, null);

      return square;
   }
}
