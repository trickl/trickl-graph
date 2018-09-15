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
import java.math.MathContext;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlTransient;

@XmlType(name = "coordinate-vertex-factory")
@XmlRootElement(name = "coordinate-vertex-factory")
public class IdCoordinateFaceFactory<V> extends AbstractIdFactory<IdCoordinateFace>
        implements FaceFactory<V, IdCoordinateFace> {

    
   protected MathContext mathContext = MathContext.UNLIMITED;

   public IdCoordinateFaceFactory() {
   }

   public IdCoordinateFaceFactory(MathContext mathContext) {
      this.mathContext = mathContext;
   }

   @Override
   public IdCoordinateFace createFace(V source, V target, boolean isBoundary) {
      return new IdCoordinateFace(nextId++);
   }   

   @XmlTransient
   public MathContext getMathContext() {
      return mathContext;
   }

   public void setMathContext(MathContext mathContext) {
      this.mathContext = mathContext;
   }
}
