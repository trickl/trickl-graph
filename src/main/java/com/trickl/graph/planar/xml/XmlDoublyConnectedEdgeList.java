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
package com.trickl.graph.planar.xml;

import com.trickl.graph.planar.DcelFace;
import com.trickl.graph.planar.DcelHalfEdge;
import com.trickl.graph.planar.DcelVertex;
import com.trickl.graph.planar.FaceFactory;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.jgrapht.EdgeFactory;

@XmlRootElement(name="dcel")
@XmlType(name="dcel", propOrder=
   {"vertices", "edges", "faces",
    "dcelVertices", "dcelHalfEdges", "dcelFaces",
    "dcelBoundaryFace", "edgeFactory", "faceFactory"})
public class XmlDoublyConnectedEdgeList<V, E, F> {

   private List<V> vertices;
   private List<E> edges;
   private List<F> faces;
   private List<DcelVertex<V, E, F>> dcelVertices;
   private List<DcelHalfEdge<V, E, F>> dcelHalfEdges;
   private List<DcelFace<V, E, F>> dcelFaces;

   private DcelFace<V, E, F> dcelBoundaryFace = null;
   private FaceFactory<V, F> faceFactory = null;
   private EdgeFactory<V, E> edgeFactory = null;

   public XmlDoublyConnectedEdgeList()
   {
      vertices = new LinkedList<V>();
      edges = new LinkedList<E>();
      faces = new LinkedList<F>();
      dcelVertices = new LinkedList<DcelVertex<V, E, F>>();
      dcelHalfEdges = new LinkedList<DcelHalfEdge<V, E, F>>();
      dcelFaces = new LinkedList<DcelFace<V, E, F>>();
   }

   @XmlElementWrapper(name="vertices")
   @XmlElement(name="vertex")
   public List<V> getVertices() {
      return vertices;
   }

   @XmlElementWrapper(name="edges")
   @XmlElement(name="edge")
   public List<E> getEdges() {
      return edges;
   }

   @XmlElementWrapper(name="faces")
   @XmlElement(name="face")
   public List<F> getFaces() {
      return faces;
   }

   @XmlElementWrapper(name="dcel-vertices")
   @XmlElement(name = "dcel-vertex")
   public List<DcelVertex<V, E, F>> getDcelVertices() {
      return dcelVertices;
   }

   @XmlElementWrapper(name = "dcel-half-edges")
   @XmlElement(name = "dcel-half-edge")
   public List<DcelHalfEdge<V, E, F>> getDcelHalfEdges() {
      return dcelHalfEdges;
   }

   @XmlElementWrapper(name = "dcel-faces")
   @XmlElement(name = "dcel-face")
   public List<DcelFace<V, E, F>> getDcelFaces() {
      return dcelFaces;
   }

   @XmlElement(name="dcel-boundary-face-id")
   @XmlIDREF
   public DcelFace<V, E, F> getDcelBoundaryFace() {
      return dcelBoundaryFace;
   }

   // Jackson does not support XmlAnyElement in 2.8     
   @XmlJavaTypeAdapter(value=XmlAnyWrapperAdapter.class)   
   @XmlElement(name="edge-factory")
   public EdgeFactory<V, E> getEdgeFactory() {
      return edgeFactory;
   }

   // Jackson does not support XmlAnyElement in 2.8     
   @XmlJavaTypeAdapter(value=XmlAnyWrapperAdapter.class)
   @XmlElement(name="face-factory")
   public FaceFactory<V, F> getFaceFactory() {
      return faceFactory;
   }

   public void setVertices(List<V> vertices) {
      this.vertices = vertices;
   }

   public void setEdges(List<E> edges) {
      this.edges = edges;
   }

   public void setFaces(List<F> faces) {
      this.faces = faces;
   }

   public void setDcelVertices(List<DcelVertex<V, E, F>> dcelVertices) {
      this.dcelVertices = dcelVertices;
   }

   public void setDcelHalfEdges(List<DcelHalfEdge<V, E, F>> dcelHalfEdges) {
      this.dcelHalfEdges = dcelHalfEdges;
   }

   public void setDcelFaces(List<DcelFace<V, E, F>> dcelFaces) {
      this.dcelFaces = dcelFaces;
   }

   public void setDcelBoundaryFace(DcelFace<V, E, F> dcelBoundaryFace) {
      this.dcelBoundaryFace = dcelBoundaryFace;
   }

   public void setFaceFactory(FaceFactory<V, F> faceFactory) {
      this.faceFactory = faceFactory;
   }

   public void setEdgeFactory(EdgeFactory<V, E> edgeFactory) {
      this.edgeFactory = edgeFactory;
   }
}
