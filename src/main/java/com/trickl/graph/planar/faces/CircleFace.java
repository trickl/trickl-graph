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

import java.io.Serializable;
import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "circle-face")
@XmlRootElement(name = "circle-face")
public class CircleFace extends IdCoordinateFace
        implements Serializable {

   private BigDecimal radius;

   private CircleFace() {
      super(null);
   }

   public CircleFace(Integer id) {
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
      return radius != null ? radius.doubleValue() : Double.NaN;
   }

   public void setRadius(double radius) {
      this.radius = !Double.isNaN(radius) ? new BigDecimal(radius, mathContext) : null;
   }
}
