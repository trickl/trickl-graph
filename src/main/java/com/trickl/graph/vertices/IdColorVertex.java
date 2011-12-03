package com.trickl.graph.vertices;

import java.awt.Color;
import java.io.Serializable;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="coordinate-vertex")
@XmlRootElement(name="coordinate-vertex")
public class IdColorVertex extends IdVertex implements Serializable {

   protected Color color;

   private IdColorVertex() {
      super(null);
   }

   public IdColorVertex(Integer id) {
      super(id);
   }

   public IdColorVertex(Integer id, Color color) {
      super(id);
      this.color = color;
   }

   @XmlAttribute(name="color")
   protected String getColorString() {
      return color.toString();
   }

   @XmlTransient
   public Color getColor() {
      return color;
   }

   protected void setColorString(String x) {
      this.color = Color.decode(x);
   }

   public void setColor(Color color) {
      this.color = color;
   }
}
