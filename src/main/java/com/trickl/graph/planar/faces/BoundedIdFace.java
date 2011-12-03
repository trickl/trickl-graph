package com.trickl.graph.planar.faces;

import com.vividsolutions.jts.geom.Envelope;
import java.math.BigDecimal;
import java.math.MathContext;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

public class BoundedIdFace extends IdFace {

   private BigDecimal minX;
   private BigDecimal minY;
   private BigDecimal maxX;
   private BigDecimal maxY;
   private MathContext mathContext = MathContext.UNLIMITED;

   public BoundedIdFace() {
      super();
   }

   public BoundedIdFace(Integer id) {
      super(id);
   }

   @XmlAttribute(name="min-x")
   public BigDecimal getMinX() {
      return minX;
   }

   @XmlAttribute(name="min-y")
   public BigDecimal getMinY() {
      return minY;
   }

   @XmlAttribute(name="max-x")
   public BigDecimal getMaxX() {
      return maxX;
   }

   @XmlAttribute(name="max-y")
   public BigDecimal getMaxY() {
      return maxY;
   }

   public void setMinX(BigDecimal minX) {
      this.minX = minX;
   }

   public void setMinY(BigDecimal minY) {
      this.minY = minY;
   }

   public void setMaxX(BigDecimal maxX) {
      this.maxX = maxX;
   }

   public void setMaxY(BigDecimal maxY) {
      this.maxY = maxY;
   }

   @XmlTransient
   public Envelope getBounds() {
      return new Envelope(
              minX == null ? Double.NaN : minX.doubleValue(),
              maxX == null ? Double.NaN : maxX.doubleValue(),
              maxY == null ? Double.NaN : maxY.doubleValue(),
              maxY == null ? Double.NaN : maxY.doubleValue());
   }

   public void setBounds(Envelope bounds) {
      if (!Double.isNaN(bounds.getMinX())) {
         this.minX = new BigDecimal(bounds.getMinX(), mathContext);
      }
      if (!Double.isNaN(bounds.getMaxX())) {
         this.maxX = new BigDecimal(bounds.getMaxX(), mathContext);
      }
      if (!Double.isNaN(bounds.getMinY())) {
         this.minY = new BigDecimal(bounds.getMinY(), mathContext);
      }
      if (!Double.isNaN(bounds.getMaxY())) {
         this.maxY = new BigDecimal(bounds.getMaxY(), mathContext);
      }
   }

   public MathContext getMathContext() {
      return mathContext;
   }

   public void setMathContext(MathContext mathContext) {
      this.mathContext = mathContext;
   }
}
