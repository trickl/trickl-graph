package com.trickl.graph.vertices;

import com.trickl.graph.AbstractIdFactory;
import java.io.Serializable;
import java.math.MathContext;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jgrapht.VertexFactory;

@XmlType(name = "circle-vertex-factory")
@XmlRootElement(name = "circle-vertex-factory")
public class CircleVertexFactory extends AbstractIdFactory<CircleVertex>
        implements VertexFactory<CircleVertex>, Serializable {


   protected MathContext mathContext = MathContext.UNLIMITED;

   public CircleVertexFactory() {
   }

   public CircleVertexFactory(MathContext mathContext) {
      this.mathContext = mathContext;
   }

   @Override
   public CircleVertex createVertex() {
      CircleVertex vertex = new CircleVertex(vertices.size());
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

