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

/**
 * A simple edge class that stores both vertices at either ends of the edge
 * and is equal to any other edge with the same two vertices.
 * @author tgee
 * @param <V> 
 */
public class UndirectedEdge<V> extends DirectedEdge<V> 
        implements Serializable {

   private UndirectedEdge() {
      this(null, null);
   }

   public UndirectedEdge(V source, V target) {
      super(source, target);
   }

   @Override
   public int hashCode() {
      int hash = 5;

      hash = 73 * hash + (this.source != null ? this.source.hashCode() : 0)
                       + (this.target != null ? this.target.hashCode() : 0);
      return hash;
   }

   @Override
   public boolean equals(Object obj) {
      if (super.equals(obj)) return true;

      if (obj == null) {
         return false;
      }
      if (getClass() != obj.getClass()) {
         return false;
      }

      // Check the other direction
      final UndirectedEdge<V> other = (UndirectedEdge<V>) obj;
      if (this.source != other.target && (this.source == null || !this.source.equals(other.target))) {
         return false;
      }
      if (this.target != other.source && (this.target == null || !this.target.equals(other.source))) {
         return false;
      }

      return true;
   }
}
