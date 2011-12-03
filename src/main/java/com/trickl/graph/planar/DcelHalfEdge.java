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
import java.util.Iterator;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="dcel-half-edge")
public class DcelHalfEdge<V, E, F> implements Serializable {

   private final static int ITERATION_LIMIT = 10000;   

   private DcelHalfEdge<V, E, F> twin;
   private DcelVertex<V, E, F> origin;
   private DcelHalfEdge<V, E, F> next;
   private DcelFace<V, E, F> face;
   private E edge;

   public DcelHalfEdge() {
      this(null);
   }

   public DcelHalfEdge(E edge) {
      this.edge = edge;
   }

   @XmlID
   @XmlAttribute(name="id")
   public String getIdString() {
      return "dcel-"
              + (origin.getVertex() == null ? "null" : origin.getVertex().toString()) + "-"
              + (next.origin.getVertex() == null ? "null" : next.origin.getVertex().toString());
   }

   @XmlIDREF
   @XmlAttribute(name="twin-half-edge-id")
   public DcelHalfEdge<V, E, F> getTwin() {
      return twin;
   }
   
   @XmlIDREF
   @XmlAttribute(name="origin-vertex-id")
   public DcelVertex<V, E, F> getOrigin() {
      return origin;
   }

   @XmlIDREF
   @XmlAttribute(name="next-half-edge-id")
   public DcelHalfEdge<V, E, F> getNext() {
      return next;
   }
   
   @XmlIDREF
   @XmlAttribute(name="face-id")
   public DcelFace<V, E, F> getFace() {
      return face;
   }
   
   @XmlIDREF
   @XmlAttribute(name="data-id")
   public E getEdge() {
      return edge;
   }

   public void setEdge(E edge) {
      this.edge = edge;
   }

   public void setTwin(DcelHalfEdge<V, E, F> twin) {
      this.twin = twin;
      if (twin != null && twin.twin == null) {
         twin.twin = this;
      }
   }

   public void setNext(DcelHalfEdge<V, E, F> next) {
      this.next = next;
   }

   public void setFace(DcelFace<V, E, F> face) {
      this.face = face;
      if (face != null && face.getAdjacent() == null) {
         face.setAdjacent(this);
      }
   }

   public void setOrigin(DcelVertex<V, E, F> origin) {
      this.origin = origin;
      if (origin != null && origin.getLeaving() == null) {
         origin.setLeaving(this);
      }
   }

   public void invalidate() {
      setTwin(null);
      setNext(null);
      setFace(null);
      setOrigin(null);
   }

   public DcelHalfEdge<V, E, F> getPrev() {
      for (DcelHalfEdge<V, E, F> halfEdge : origin.outHalfEdges()) {
         if (halfEdge.twin.next.equals(this)) {
            return halfEdge.twin;
         }
      }
      return null;
   }

   public DcelHalfEdge<V, E, F> getPrevInternal() {
      for (DcelHalfEdge<V, E, F> halfEdge : prevEdges()) {
         if (!halfEdge.isEdgeBoundary()) {
            return halfEdge;
         }
      }
      return null;
   }

   public DcelHalfEdge<V, E, F> getNextInternal() {
      for (DcelHalfEdge<V, E, F> halfEdge : edges()) {
         if (!halfEdge.isEdgeBoundary()) {
            return halfEdge;
         }
      }
      return null;
   }

   public Iterable<DcelHalfEdge<V, E, F>> edges() {
      return edges(this, true);
   }

   public Iterable<DcelHalfEdge<V, E, F>> prevEdges() {
      return edges(this, false);
   }

   public Iterable<DcelHalfEdge<V, E, F>> edges(final DcelHalfEdge<V, E, F> endEdge, final boolean forward) {
      final DcelHalfEdge<V, E, F> startEdge = this;
      return new Iterable<DcelHalfEdge<V, E, F>>() {

         @Override
         public Iterator<DcelHalfEdge<V, E, F>> iterator() {
            return new Iterator<DcelHalfEdge<V, E, F>>() {

               DcelHalfEdge<V, E, F> next = startEdge;
               int iteration = 0;

               @Override
               public boolean hasNext() {
                  return (next != null);
               }

               @Override
               public DcelHalfEdge<V, E, F> next() {
                  DcelHalfEdge<V, E, F> current = next;
                  if (forward) {
                     next = current.next;
                  } else {
                     next = current.getPrev();
                  }

                  if (next == endEdge) {
                     next = null;
                  }

                  if (++iteration > ITERATION_LIMIT) {
                     throw new IndexOutOfBoundsException(
                             "Iteration beyond limit, suggests corrupt structure.");
                  }

                  return current;
               }

               @Override
               public void remove() {
                  throw new UnsupportedOperationException("Not supported yet.");
               }
            };
         }
      };
   }

   public boolean isBoundary() {
      return face.isBoundary();
   }

   public boolean isEdgeBoundary() {
      return isBoundary() || twin.isBoundary();
   }
  
   public void remove() {
      // Check if this edge separates two distinct faces
      if (face != twin.face) {
         // Remove the redundant face, merging the faces
         twin.face.setBoundary(face.isBoundary()
                               || twin.face.isBoundary());

         face.invalidate();

         for (DcelHalfEdge<V, E, F> itr : edges()) {
            // Repoint edges to the conjoined face
            itr.setFace(twin.face);
            twin.face.setAdjacent(itr);
         }        
      } else if (face.getAdjacent() == this) {
         // Hanging edge, ensure the face no longer references the removed edge
         face.setAdjacent(next.next);
      }

      // Reassign faces and vertices if necessary so they don't reference the deleted edge
      if (twin.face.getAdjacent() == twin) {
         // Note we set to next->next in case next = twin.
         twin.face.setAdjacent(twin.next.next);
      }

      if (twin.origin.getLeaving() == twin) {
         twin.origin.setLeaving(next);
      }
      if (origin.getLeaving() == this) {
         origin.setLeaving(twin.next);
      }

      // Handle disconnected vertices and faces
      if (twin.origin.getLeaving() == twin) {
         twin.origin.invalidate();
      }
      if (origin.getLeaving() == this) {
         origin.invalidate();
      }
      
      if (face.getAdjacent() == this) {
         face.invalidate();
      }
      if (twin.face.getAdjacent() == twin) {
         twin.face.invalidate();
      }

      // Reassign previous edges
      DcelHalfEdge<V, E, F> prev = getPrev();
      if (prev != null) prev.setNext(twin.next);
      DcelHalfEdge<V, E, F> twinPrev = twin.getPrev();
      if (twinPrev != null) twinPrev.setNext(next);

      // Finally invalidate these edges
      twin.invalidate();
      invalidate();
   }

   @Override
   public String toString() {
      return "E(" + ((edge == null) ? "NULL" : edge.toString()) + ") T("
              + ((twin == null) ? "NULL" : twin.getIdString()) + ") N("
              + ((next == null) ? "NULL" : next.getIdString()) + ") O("
              + ((origin == null) ? "NULL" : origin.getIdString()) + ") F("
              + ((face == null) ? "NULL" : face.getIdString()) + ")";
   }

   @Override
   public boolean equals(Object rhs) {
      if (!(rhs instanceof DcelHalfEdge)) {
         return false;
      }
      {
         return ((DcelHalfEdge) rhs).origin.equals(origin)
                 && ((DcelHalfEdge) rhs).next.origin.equals(next.origin);
      }
   }

   @Override
   public int hashCode() {
      int hash = 5;
      hash = 73 * hash + (this.next != null && this.next.origin != null
              ? this.next.origin.hashCode() : 0);
      hash = 73 * hash + (this.origin != null ? this.origin.hashCode() : 0);
      return hash;
   }
}
