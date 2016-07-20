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

import com.trickl.graph.planar.FaceFactory;
import java.io.Serializable;
import java.util.function.Predicate;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="throwable-face-factory")
@XmlRootElement(name="throwable-face-factory")
public class ThrowableFaceFactory<V, F> implements FaceFactory<V, F>,
        Serializable {
    
    @XmlTransient
    private Predicate<F> predicate = (F face) -> { return false; };
    
    private final FaceFactory<V, F> faceFactory;
    
    public ThrowableFaceFactory() {
        faceFactory = null;
    }
    
    public ThrowableFaceFactory(FaceFactory<V, F> faceFactory, Predicate<F> predicate) {
       this.faceFactory = faceFactory;
       this.predicate = predicate;
    }

   @Override
   public F createFace(V source, V target, boolean isBoundary) {            
      F face = faceFactory.createFace(source, target, isBoundary);
      if (predicate.test(face)) {
          throw new RuntimeException("Predicate evaluated to true");
      }
      return face;
   }
}
