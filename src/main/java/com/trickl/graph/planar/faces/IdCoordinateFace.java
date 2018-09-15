/*
 * This file is part of the Trickl Open Source Libraries.
 *
 * Trickl Open Source Libraries - http://open.trickl.com/
 *
 * Copyright (C) 2011 Tim Gee.
 *
 * Trickl Open Source Libraries are free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Trickl Open Source Libraries are distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this project.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.trickl.graph.planar.faces;

import com.vividsolutions.jts.geom.Coordinate;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="coordinate-face")
@XmlRootElement(name="coordinate-face")
public class IdCoordinateFace extends IdFace implements Serializable {

   protected BigDecimal x;

   protected BigDecimal y;

   protected BigDecimal z;

   protected MathContext mathContext = MathContext.UNLIMITED;

   private IdCoordinateFace() {
      super(null);
   }

   public IdCoordinateFace(Integer id) {
      super(id);
   }

   public IdCoordinateFace(Integer id, Coordinate coordinate) {
      super(id);
      setCoordinate(coordinate);
   }

   @XmlAttribute
   public BigDecimal getX() {
      return x;
   }

   @XmlAttribute
   public BigDecimal getY() {
      return y;
   }

   @XmlAttribute
   public BigDecimal getZ() {
      return z;
   }

   @XmlTransient
   public Coordinate getCoordinate() {
      return new Coordinate(x == null ? Double.NaN : x.doubleValue(),
              y == null ? Double.NaN : y.doubleValue(),
              z == null ? Double.NaN : z.doubleValue());
   }

   public void setX(BigDecimal x) {
      this.x = x;
   }

   public void setY(BigDecimal y) {
      this.y = y;
   }

   public void setZ(BigDecimal z) {
      this.z = z;
   }

   public final void setCoordinate(Coordinate coordinate) {
      if (!Double.isNaN(coordinate.x)) {
         this.x = new BigDecimal(coordinate.x, mathContext);
      }
      if (!Double.isNaN(coordinate.y)) {
         this.y = new BigDecimal(coordinate.y, mathContext);
      }
      if (!Double.isNaN(coordinate.z)) {
         this.z = new BigDecimal(coordinate.z, mathContext);
      }
   }

   @XmlTransient
   public MathContext getMathContext() {
      return mathContext;
   }

   public void setMathContext(MathContext mathContext) {
      this.mathContext = mathContext;
      
      // Reapply to the coordinate
      setCoordinate(getCoordinate());
   }
}
