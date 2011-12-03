package com.trickl.graph.vertices;

import com.trickl.graph.AbstractIdFactory;
import java.io.Serializable;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.jgrapht.VertexFactory;

@XmlType(name = "id-vertex-factory")
@XmlRootElement(name = "id-vertex-factory")
public class IdVertexFactory extends AbstractIdFactory<IdVertex>
        implements VertexFactory<IdVertex>,
        Serializable {

   @Override
   public IdVertex createVertex() {
      IdVertex vertex = new IdVertex(nextId++);
      vertices.put(vertex.getId(), vertex);
      return vertex;
   }
}
