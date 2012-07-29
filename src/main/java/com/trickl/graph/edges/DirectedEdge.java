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
package com.trickl.graph.edges;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlIDREF;

/**
 * A simple edge class that stores both vertices at either ends of the edge
 * and is equal to any other edge with the source and target.
 * @author tgee
 * @param <V> 
 */
public class DirectedEdge<V> implements Serializable {
   protected V source;
   protected V target;

   private DirectedEdge() {
      this(null, null);
   }

   public DirectedEdge(V source, V target) {
      this.source = source;
      this.target = target;
   }

   @XmlIDREF
   @XmlAttribute(name="source-vertex-id")
   public V getSource() {
      return source;
   }

   public void setSource(V source) {
      this.source = source;
   }

   @XmlIDREF
   @XmlAttribute(name="target-vertex-id")
   public V getTarget() {
      return target;
   }

   public void setTarget(V target) {
      this.target = target;
   }

   @Override
   public int hashCode() {
      int hash = 5;
      hash = 73 * hash + (this.source != null ? this.source.hashCode() : 0);
      hash = 73 * hash + (this.target != null ? this.target.hashCode() : 0);
      return hash;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }
      if (getClass() != obj.getClass()) {
         return false;
      }
      final DirectedEdge<V> other = (DirectedEdge<V>) obj;

      if (this.source != other.source && (this.source == null || !this.source.equals(other.source))) {
         return false;
      }
      if (this.target != other.target && (this.target == null || !this.target.equals(other.target))) {
         return false;
      }
      return true;
   }

   @Override
   public String toString() {
      return ((source == null) ? "null" : source.toString()) + "-"
           + ((target == null) ? "null" : target.toString());
   }
}
