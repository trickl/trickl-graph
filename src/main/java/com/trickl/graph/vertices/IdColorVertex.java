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
