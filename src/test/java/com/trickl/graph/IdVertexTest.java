package com.trickl.graph;

import com.trickl.graph.vertices.IdVertex;
import com.trickl.graph.xml.bind.DefaultNamespace;
import com.trickl.graph.xml.bind.WellKnownNamespace;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

public class IdVertexTest {

   @XmlRootElement(name = "root")
   public static class Root<V> {

      protected List<V> vertices =
              new LinkedList<V>();

      @XmlElementWrapper(name = "vertices")
      @XmlElement(name = "vertex")
      public List<V> getVertices() {
         return vertices;
      }

      public void setVertices(List<V> vertices) {
         this.vertices = vertices;
      }
   }

   @Test
   public void transformToXML() throws Exception {
      IdVertex vertex = new IdVertex(12345);
      StringWriter writer = new StringWriter();
      JAXBContext context = JAXBContext.newInstance(IdVertex.class);
      Marshaller marshaller = context.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      marshaller.setProperty(DefaultNamespace.PROPERTY_NAME,
              new DefaultNamespace(WellKnownNamespace.TRICKL.getURI(),
                                   WellKnownNamespace.XML_SCHEMA_INSTANCE.getURI()));

      marshaller.marshal(vertex, writer);

      //System.out.println(writer.toString());

      URL controlFile = this.getClass().getResource("id-vertex.xml");
      InputStreamReader controlReader = new InputStreamReader(controlFile.openStream());
      StringReader reader = new StringReader(writer.toString());
      assertXMLEqual("Generated XML not as expected", controlReader, reader);
   }

   @Test
   public void transformListToXML() throws Exception {
      Root<IdVertex> root = new Root<IdVertex>();
      root.vertices.add(new IdVertex(1));
      root.vertices.add(new IdVertex(2));
      root.vertices.add(new IdVertex(3));
      StringWriter writer = new StringWriter();
      JAXBContext context = JAXBContext.newInstance(Root.class, IdVertex.class);
      Marshaller marshaller = context.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);      
      marshaller.setProperty(DefaultNamespace.PROPERTY_NAME,
              new DefaultNamespace(WellKnownNamespace.TRICKL.getURI(),
                                   WellKnownNamespace.XML_SCHEMA_INSTANCE.getURI()));
      marshaller.marshal(root, writer);

      //System.out.println(writer.toString());

      URL controlFile = this.getClass().getResource("id-vertex-list.xml");
      InputStreamReader controlReader = new InputStreamReader(controlFile.openStream());
      StringReader reader = new StringReader(writer.toString());
      assertXMLEqual("Generated XML not as expected", controlReader, reader);
   }

   @Test
   public void transformFromXML() throws Exception {
      URL controlFile = this.getClass().getResource("id-vertex.xml");
      InputStreamReader reader = new InputStreamReader(controlFile.openStream());

      JAXBContext context = JAXBContext.newInstance(IdVertex.class);
      Unmarshaller unmarshaller = context.createUnmarshaller();
      IdVertex vertex = (IdVertex) unmarshaller.unmarshal(reader);
      assertNotNull(vertex);
      assertEquals(12345, (int) vertex.getId());
   }

   @Test
   public void transformListFromXML() throws Exception {
      URL controlFile = this.getClass().getResource("id-vertex-list.xml");
      InputStreamReader reader = new InputStreamReader(controlFile.openStream());

      JAXBContext context = JAXBContext.newInstance(Root.class,
              IdVertex.class);
      Unmarshaller unmarshaller = context.createUnmarshaller();
      Root<IdVertex> root = (Root<IdVertex>) unmarshaller.unmarshal(reader);
      assertNotNull(root);
      assertEquals(3, root.vertices.size());
      assertEquals(1, (int) root.vertices.get(0).getId());
      assertEquals(2, (int) root.vertices.get(1).getId());
      assertEquals(3, (int) root.vertices.get(2).getId());
   }
}
