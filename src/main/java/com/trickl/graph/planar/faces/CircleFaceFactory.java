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

import com.trickl.graph.AbstractIdFactory;
import com.trickl.graph.planar.FaceFactory;
import java.io.Serializable;
import java.math.MathContext;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "circle-vertex-factory")
@XmlRootElement(name = "circle-vertex-factory")
public class CircleFaceFactory<V> extends AbstractIdFactory<CircleFace>
        implements FaceFactory<V, CircleFace>, Serializable {


   protected MathContext mathContext = MathContext.UNLIMITED;

   public CircleFaceFactory() {
   }

   public CircleFaceFactory(MathContext mathContext) {
      this.mathContext = mathContext;
   }

   @Override
   public CircleFace createFace(V source, V target, boolean isBoundary) {
      return new CircleFace(nextId++);
   }

   @XmlTransient
   public MathContext getMathContext() {
      return mathContext;
   }

   public void setMathContext(MathContext mathContext) {
      this.mathContext = mathContext;
   }
}

