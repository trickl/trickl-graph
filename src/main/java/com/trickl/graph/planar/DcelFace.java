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
package com.trickl.graph.planar;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="dcel-face")
public class DcelFace<V, E, F> implements Serializable {

   private DcelHalfEdge<V, E, F> adjacent;
   private boolean isBoundary = false;
   private F face;

   public DcelFace() {
      this(null, false);
   }

   public DcelFace(F face) {
      this(face, false);
   }

   public DcelFace(F face, boolean isBoundary) {
      this.face = face;
      this.isBoundary = isBoundary;
   }

   @XmlID
   @XmlAttribute(name="id")
   public String getIdString() {
      return "dcel-f-" + (face == null ? "null" : face.toString());
   }

   @XmlIDREF
   @XmlAttribute(name="adjacent-edge-id")
   public DcelHalfEdge<V, E, F> getAdjacent() {
      return adjacent;
   }
      
   @XmlAttribute(name="is-boundary")
   public boolean isBoundary() {
      return isBoundary;
   }
   
   @XmlIDREF
   @XmlAttribute(name="data-id")
   public F getFace() {
      return face;
   }

   public void setFace(F face) {
      this.face = face;
   }

   public void setAdjacent(DcelHalfEdge<V, E, F> adjacent) {
      this.adjacent = adjacent;
      if (adjacent != null && adjacent.getFace() == null) {
         adjacent.setFace(this);
      }
   }

   public void setBoundary(boolean isBoundary) {
      this.isBoundary = isBoundary;
   }

   DcelHalfEdge<V, E, F> getHalfEdge(final V vertex) {

      for (DcelHalfEdge<V, E, F> halfEdge : adjacent.edges()) {
         if (halfEdge.getOrigin().getVertex().equals(vertex)) {
            return halfEdge;
         }
      }
      return null;
   }

   public Set<V> getVertices() {
      final Set<V> vertices = new LinkedHashSet<V>(getEdgeCount());

      for (DcelHalfEdge<V, E, F> halfEdge : adjacent.edges()) {
         V vertex = halfEdge.getOrigin().getVertex();
         if (vertex != null) {
            vertices.add(vertex);
         }
      }

      return vertices;
   }

   public Set<E> getEdges() {
      final Set<E> edges = new LinkedHashSet<E>(getEdgeCount());

      for (DcelHalfEdge<V, E, F> halfEdge : adjacent.edges()) {
         E edge = halfEdge.getEdge();
         if (edge != null) {
            edges.add(edge);
         }
      }

      return edges;
   }

   public int getEdgeCount() {
      int count = 0;
      for (DcelHalfEdge<V, E, F> halfEdge : adjacent.edges()) {
         ++count;
      }

      return count;
   }

   public void invalidate() {
      setAdjacent(null);
   }

   @Override
   public String toString() {
      return "F(" + ((face == null) ? "NULL" : face.toString()) + ") E("
              + ((adjacent == null ? "NULL" : adjacent.getIdString())) + ") B("
              + Boolean.toString(isBoundary) + ")";
   }

   @Override
   public boolean equals(Object rhs) {
      if (!(rhs instanceof DcelFace)) {
         return false;
      }
      {
         return ((DcelFace) rhs).getAdjacent().equals(adjacent);
      }
   }

   @Override
   public int hashCode() {
      int hash = 7;
      hash = 23 * hash + (this.adjacent != null ? this.adjacent.hashCode() : 0);
      return hash;
   }
}
