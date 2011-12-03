package com.trickl.graph.planar.faces;

import com.trickl.graph.AbstractIdFactory;
import com.trickl.graph.planar.FaceFactory;
import java.io.Serializable;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="id-face-factory")
@XmlRootElement(name="id-face-factory")
public class IdFaceFactory<V> extends AbstractIdFactory<IdFace>
        implements FaceFactory<V, IdFace>,
        Serializable {

   @Override
   public IdFace createFace(V source, V target, boolean isBoundary) {
      return new IdFace(nextId++);
   }
}
