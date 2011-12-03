package com.trickl.graph.planar.xml;

import com.trickl.graph.planar.DcelFace;
import com.trickl.graph.planar.DcelHalfEdge;
import com.trickl.graph.planar.DcelVertex;
import com.trickl.graph.planar.DoublyConnectedEdgeList;
import com.trickl.graph.planar.FaceFactory;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.jgrapht.EdgeFactory;

public class XmlDoublyConnectedEdgeListAdapter<V, E, F> extends XmlAdapter<XmlDoublyConnectedEdgeList<V, E, F>, DoublyConnectedEdgeList<V, E, F>> {

   /**
    * This function is called before the IDResolver finishes resolving. At this point,
    * only idrefs that reference ids earlier in the document are resolved. The other
    * idrefs will be resolved after this method.
    * @param xmlDcel
    * @return
    * @throws Exception
    */
   @Override
   public DoublyConnectedEdgeList<V, E, F> unmarshal(XmlDoublyConnectedEdgeList<V, E, F> xmlDcel) throws Exception {
      EdgeFactory<V, E> edgeFactory = xmlDcel.getEdgeFactory();
      FaceFactory<V, F> faceFactory = xmlDcel.getFaceFactory();
      DoublyConnectedEdgeList<V, E, F> dcel = new DoublyConnectedEdgeList<V, E, F>(edgeFactory, faceFactory);
      
      for (DcelVertex<V, E, F> vertex : xmlDcel.getDcelVertices()) {
         dcel.getVertexMap().put(vertex.getVertex(), vertex);
      }
      
      for (DcelHalfEdge<V, E, F> halfEdge : xmlDcel.getDcelHalfEdges()) {
         if (!dcel.getEdgeMap().containsKey(halfEdge.getEdge())) {
            dcel.getEdgeMap().put(halfEdge.getEdge(), halfEdge);
         }
      }
      
      dcel.getFaceMap().clear();
      for (DcelFace<V, E, F> face : xmlDcel.getDcelFaces()) {
         dcel.getFaceMap().put(face.getFace(), face);
      }

      dcel.setBoundaryFace(xmlDcel.getDcelBoundaryFace().getFace());

      return dcel;
   }

   @Override
   public XmlDoublyConnectedEdgeList<V, E, F> marshal(DoublyConnectedEdgeList<V, E, F> dcel) throws Exception {
      XmlDoublyConnectedEdgeList<V, E, F> xmlDcel = new XmlDoublyConnectedEdgeList<V, E, F>();
      xmlDcel.getFaces().addAll(dcel.getFaceMap().keySet());
      xmlDcel.getEdges().addAll(dcel.getEdgeMap().keySet());
      xmlDcel.getVertices().addAll(dcel.getVertexMap().keySet());
      xmlDcel.getDcelFaces().addAll(dcel.getFaceMap().values());
      for (DcelHalfEdge<V, E, F> dcelHalfEdge : dcel.getEdgeMap().values())
      {
         xmlDcel.getDcelHalfEdges().add(dcelHalfEdge);
         xmlDcel.getDcelHalfEdges().add(dcelHalfEdge.getTwin());
      }
      xmlDcel.getDcelVertices().addAll(dcel.getVertexMap().values());
      xmlDcel.setDcelBoundaryFace(dcel.getFaceMap().get(dcel.getBoundaryFace()));
      xmlDcel.setEdgeFactory(dcel.getEdgeFactory());
      xmlDcel.setFaceFactory(dcel.getFaceFactory());
      return xmlDcel;
   }
 }
