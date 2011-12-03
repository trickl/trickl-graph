package com.trickl.graph.planar.xml;

import com.trickl.graph.planar.DoublyConnectedEdgeList;
import com.trickl.graph.planar.FaceFactory;
import com.trickl.graph.planar.PlanarGraph;
import com.trickl.graph.xml.bind.DefaultNamespace;
import com.trickl.graph.xml.bind.WellKnownNamespace;
import java.io.Reader;
import java.io.Writer;
import javax.xml.bind.*;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Hmm, I've forgotten the point of this class.
 * TODO: Can't we just marshal the DoublyConnectedEdgeList directly for a
 * root element? Possible refactoring here.
 * @author tgee
 * @param <V>
 * @param <E>
 * @param <F>
 */
@XmlRootElement(name = "root")
public class XmlDcelDocument<V, E, F> {

   private DoublyConnectedEdgeList<V, E, F> doublyConnectedEdgeList;

   @XmlElementRef
   public DoublyConnectedEdgeList<V, E, F> getDoublyConnectedEdgeList() {
      return doublyConnectedEdgeList;
   }

   public XmlDcelDocument() {
   }

   public XmlDcelDocument(PlanarGraph<V, E> graph, FaceFactory<V, F> faceFactory) {
      this.doublyConnectedEdgeList = new DoublyConnectedEdgeList<V, E, F>(graph, faceFactory);
   }

   public XmlDcelDocument(DoublyConnectedEdgeList<V, E, F> doublyConnectedEdgeList) {
      this.doublyConnectedEdgeList = doublyConnectedEdgeList;
   }

   public void setDoublyConnectedEdgeList(DoublyConnectedEdgeList<V, E, F> doublyConnectedEdgeList) {
      this.doublyConnectedEdgeList = doublyConnectedEdgeList;
   }

   public void read(Reader reader, JAXBContext context) throws JAXBException  {
      Unmarshaller unmarshaller = context.createUnmarshaller();
      XmlDcelDocument<V, E, F> root = (XmlDcelDocument<V, E, F>) unmarshaller.unmarshal(reader);
      setDoublyConnectedEdgeList(root.getDoublyConnectedEdgeList());
   }

   public void write(Writer writer, JAXBContext context) throws PropertyException, JAXBException {
      Marshaller marshaller = context.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      marshaller.setProperty(DefaultNamespace.PROPERTY_NAME,
              new DefaultNamespace(WellKnownNamespace.TRICKL.getURI(),
                                   WellKnownNamespace.XML_SCHEMA_INSTANCE.getURI()));
      marshaller.marshal(this, writer);
   }
}
