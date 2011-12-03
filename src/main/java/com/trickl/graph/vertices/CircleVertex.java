package com.trickl.graph.vertices;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "circle-vertex")
@XmlRootElement(name = "circle-vertex")
public class CircleVertex extends IdCoordinateVertex
        implements Serializable {

   private BigDecimal radius;

   private CircleVertex() {
      super(null);
   }

   public CircleVertex(Integer id) {
      super(id);
   }

   @XmlAttribute(name="radius")
   public BigDecimal getRoundedRadius() {
      return radius;
   }

   public void setRoundedRadius(BigDecimal radius) {
      this.radius = radius;
   }

   @XmlTransient
   public double getRadius() {
      return radius.doubleValue();
   }

   public void setRadius(double radius) {
      this.radius = new BigDecimal(radius, mathContext);
   }
}
