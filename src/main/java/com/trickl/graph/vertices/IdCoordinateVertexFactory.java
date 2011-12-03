package com.trickl.graph.vertices;

import com.trickl.graph.AbstractIdFactory;
import java.math.MathContext;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlTransient;
import org.jgrapht.VertexFactory;

@XmlType(name = "coordinate-vertex-factory")
@XmlRootElement(name = "coordinate-vertex-factory")
public class IdCoordinateVertexFactory extends AbstractIdFactory<IdCoordinateVertex>
        implements VertexFactory<IdCoordinateVertex> {

    
   protected MathContext mathContext = MathContext.UNLIMITED;

   public IdCoordinateVertexFactory() {
   }

   public IdCoordinateVertexFactory(MathContext mathContext) {
      this.mathContext = mathContext;
   }

   @Override
   public IdCoordinateVertex createVertex() {
      IdCoordinateVertex vertex = new IdCoordinateVertex(vertices.size());
      vertex.setMathContext(mathContext);
      vertices.put(vertex.getId(), vertex);
      return vertex;
   }

   @XmlTransient
   public MathContext getMathContext() {
      return mathContext;
   }

   public void setMathContext(MathContext mathContext) {
      this.mathContext = mathContext;
   }
}
