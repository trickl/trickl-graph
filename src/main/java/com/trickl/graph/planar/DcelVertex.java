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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "dcel-vertex")
public class DcelVertex<V, E, F> implements Serializable {

   private final static int ITERATION_LIMIT = 10000;
   private DcelHalfEdge<V, E, F> leaving;
   private V vertex;

   public DcelVertex() {
      this(null);
   }

   public DcelVertex(V vertex) {
      this.vertex = vertex;
   }

   @XmlID
   @XmlAttribute(name="id")
   public String getIdString() {
      return "dcel-" + (vertex == null ? "null" : vertex.toString());
   }
   
   protected void setId(String id) {       
   }

   @XmlIDREF
   @XmlAttribute(name = "leaving-half-edge-id")
   public DcelHalfEdge<V, E, F> getLeaving() {
      return leaving;
   }

   @XmlIDREF
   @XmlAttribute(name = "data-id")
   public V getVertex() {
      return vertex;
   }


   public void setVertex(V vertex) {
      this.vertex = vertex;
   }

   public void setLeaving(DcelHalfEdge<V, E, F> leaving) {
      this.leaving = leaving;
      if (leaving != null && leaving.getOrigin() == null) {
         leaving.setOrigin(this);
      }
   }

   public void invalidate() {
      setLeaving(null);
   }

   @XmlTransient
   public DcelHalfEdge<V, E, F> getNextBoundaryEdge() {
      for (DcelHalfEdge<V, E, F> halfEdge : outHalfEdges()) {
         if (halfEdge.isEdgeBoundary()) {
            return halfEdge;
         }
      }
      return null;
   }

   @XmlTransient
   public DcelHalfEdge<V, E, F> getPrevBoundaryEdge() {
      for (DcelHalfEdge<V, E, F> halfEdge : outHalfEdges()) {
         if (halfEdge.isEdgeBoundary()) {
            return halfEdge.getPrev();
         }
      }
      return null;
   }

   @XmlTransient
   public boolean isBoundary() {
      for (DcelHalfEdge<V, E, F> halfEdge : outHalfEdges()) {
         if (halfEdge.isBoundary()) {
            return true;
         }
      }
      return false;
   }

   @XmlTransient
   public int getEdgeCount() {
      int count = 0;
      for (DcelHalfEdge<V, E, F> halfEdge : outHalfEdges()) {
         ++count;
      }

      return count;
   }

   public DcelHalfEdge<V, E, F> getHalfEdge(final V target) {

      for (DcelHalfEdge<V, E, F> halfEdge : outHalfEdges()) {
         if (halfEdge.getNext().getOrigin().getVertex().equals(target)) {
            return halfEdge;
         }
      }
      return null;
   }

   @XmlTransient
   public Set<E> getEdges() {
      final Set<E> edges = new LinkedHashSet<E>(getEdgeCount());

      for (DcelHalfEdge<V, E, F> halfEdge : outHalfEdges()) {
         E edge = halfEdge.getEdge();
         if (edge != null) {
            edges.add(edge);
         }
      }

      return edges;
   }

   @XmlTransient
   public List<V> getConnectedVertices() {
      return getConnectedVertices(getLeaving());
   }

   public List<V> getConnectedVertices(DcelHalfEdge<V, E, F> startEdge) {
      return getConnectedVertices(startEdge, startEdge);
   }

   public List<V> getConnectedVertices(DcelHalfEdge<V, E, F> startEdge, DcelHalfEdge<V, E, F> endEdge) {
      final List<V> vertices = new ArrayList<V>(getEdgeCount());

      for (DcelHalfEdge<V, E, F> halfEdge : outHalfEdges(startEdge, endEdge)) {
         V edgeVertex = halfEdge.getTwin().getOrigin().getVertex();
         if (edgeVertex != null) {
            vertices.add(edgeVertex);
         }
      }

      return vertices;
   }
   
   Iterable<DcelHalfEdge<V, E, F>> outHalfEdges() {
      return outHalfEdges(getLeaving());
   }

   Iterable<DcelHalfEdge<V, E, F>> outHalfEdges(final DcelHalfEdge<V, E, F> startEdge) {
      return outHalfEdges(startEdge, startEdge);
   }

   Iterable<DcelHalfEdge<V, E, F>> outHalfEdges(final DcelHalfEdge<V, E, F> startEdge, final DcelHalfEdge<V, E, F> endEdge) {
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
                  next = current.getTwin().getNext();

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

   // beforeEdge -> newEdge -> afterEdge
   public DcelHalfEdge<V, E, F> addEdge(DcelVertex<V, E, F> target,
           DcelHalfEdge<V, E, F> beforeEdge,
           DcelHalfEdge<V, E, F> afterEdge,
           DcelFace<V, E, F> boundaryFace,
           FaceFactory<V, F> faceFactory,
           E e) {     
 
      if (leaving != null
          && beforeEdge == null) {
         // Source has edges, set the before edge
         beforeEdge = leaving.getTwin();
      }
      
      if (target.leaving != null
          && afterEdge == null) {
         // Target has edges, set the after edge
         if (beforeEdge == null) {
            afterEdge = target.leaving;
         }
         else {
            for (DcelHalfEdge<V, E, F> halfEdge : target.outHalfEdges()) {
               if (halfEdge.getFace().equals(beforeEdge.getFace())) {
                  afterEdge = halfEdge;
                  break;
               }
            }

            if (afterEdge == null) {
               // Must maintain planarity
                throw new NoSuchElementException("Target, source and before must share a face.");
            }
         }
      }

      // Check if a closed face is formed by the addition of this edge
      // i.e. is after edge already connected to before edge?
      F createdFace = null;
      if (afterEdge != null) {
         for (DcelHalfEdge<V, E, F> halfEdge : afterEdge.edges())
         {
            if (halfEdge.equals(beforeEdge)) {
               createdFace = faceFactory.createFace(this.getVertex(),
                    target.getVertex(),
                    false);
            }
         }
      }

      /* Everything seems okay, we should be able to create this edge */
      DcelHalfEdge<V, E, F> createdEdge = new DcelHalfEdge<V, E, F>(e);
      DcelHalfEdge<V, E, F> createdTwin = new DcelHalfEdge<V, E, F>(e);
      createdEdge.setTwin(createdTwin);

      // Set the edge next pointers
      if (beforeEdge == null) {
         createdTwin.setNext(createdEdge);
      } else {
         createdTwin.setNext(beforeEdge.getNext());
         beforeEdge.setNext(createdEdge);
      }

      if (afterEdge == null) {
         createdEdge.setNext(createdTwin);
      } else {
         createdEdge.setNext(afterEdge);
         afterEdge.getPrev().setNext(createdTwin);
      }

      // Set the edge faces
      if (beforeEdge == null && afterEdge == null) {
         createdEdge.setFace(boundaryFace);
         createdTwin.setFace(boundaryFace);     
         boundaryFace.setAdjacent(createdTwin);     
      } else if (beforeEdge == null) {
         createdEdge.setFace(afterEdge.getFace());
         createdTwin.setFace(afterEdge.getFace());      
         afterEdge.getFace().setAdjacent(createdTwin);      
      } else if (afterEdge == null) {
         createdEdge.setFace(beforeEdge.getFace());
         createdTwin.setFace(beforeEdge.getFace());      
         beforeEdge.getFace().setAdjacent(createdTwin);      
      } else {         
         createdTwin.setFace(createdEdge.getNext().getFace());       
         createdEdge.getNext().getFace().setAdjacent(createdTwin);       
         if (createdFace != null) {
            // By convention, the before -> edge -> after all belong to the new face
            // if one is formed
            final DcelFace<V, E, F> dcelFace = new DcelFace<V, E, F>(createdFace);

            for (DcelHalfEdge<V, E, F> halfEdge : createdEdge.edges()) {
               halfEdge.setFace(dcelFace);
               dcelFace.setAdjacent(halfEdge);
            }
         }
         else {
            createdEdge.setFace(createdTwin.getNext().getFace());           
            createdTwin.getNext().getFace().setAdjacent(createdEdge);         
         }
      }

      // Note that setting the origin will set the vertex
      // to have it's leaving edge point to this edge, the most recently created edge.
      // This is the behaviour we want when for when we don't specify the
      // before edge.
      createdEdge.setOrigin(this);
      setLeaving(createdEdge);
      createdTwin.setOrigin(target);
      target.setLeaving(createdTwin);

      return createdEdge;
   }

   public void remove() {

      Set<DcelHalfEdge<V, E, F> > halfEdges = new LinkedHashSet<DcelHalfEdge<V, E, F> >();
      // Get a copy of the edges as the iterator will be invalidated
      for (DcelHalfEdge<V, E, F> halfEdge : outHalfEdges()) {
         halfEdges.add(halfEdge);
      }
      for (DcelHalfEdge<V, E, F> halfEdge : halfEdges) {
         halfEdge.remove();
      }

      invalidate();
   }

   @Override
   public String toString() {
      return "V(" + ((vertex == null) ? "NULL" : vertex.toString()) + ") E("
              + ((leaving == null) ? "NULL" : leaving.getId()) + ")";
   }

   @Override
   public boolean equals(Object rhs) {
      if (!(rhs instanceof DcelVertex)) {
         return false;
      }
      {
         return ((DcelVertex) rhs).getVertex().equals(vertex);
      }
   }

   @Override
   public int hashCode() {
      int hash = 7;
      hash = 97 * hash + (this.vertex != null ? this.vertex.hashCode() : 0);
      return hash;
   }
}
