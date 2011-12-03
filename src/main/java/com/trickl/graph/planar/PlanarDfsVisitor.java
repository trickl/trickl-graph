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

import com.trickl.graph.SpanningSearchVisitor;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Map;
import org.jgrapht.Graph;

class PlanarDfsVisitor<V, E> implements SpanningSearchVisitor<V, E> {

   private static class PlanarDfsDetail<V, E> {

      int lowPoint;
      V parent;
      int dfsNumber;
      int leastAncestor;
      E dfsEdge;      
   }
   
   private Map<V, PlanarDfsDetail<V, E>> vertexDetails;
   private int count = 0;
   private Graph<V, E> graph;

   public PlanarDfsVisitor(Graph<V, E> graph) {
      this.graph = graph;
      vertexDetails = new Hashtable<V, PlanarDfsDetail<V, E>>();
   }

   @Override
   public void startVertex(V u) {
      PlanarDfsDetail<V, E> detail = vertexDetails.get(u);
      if (detail == null) {
         detail = new PlanarDfsDetail<V, E>();
         vertexDetails.put(u, detail);
      }
      detail.parent = u;
      detail.leastAncestor = count;
   }

   @Override
   public void discoverVertex(V u) {
      PlanarDfsDetail<V, E> detail = vertexDetails.get(u);
      if (detail == null) {
         detail = new PlanarDfsDetail<V, E>();
         vertexDetails.put(u, detail);
      }
      detail.lowPoint = count;
      detail.dfsNumber = count;
      setCount(getCount() + 1);
   }

   @Override
   public void treeEdge(V source, V target) {
      E edge = graph.getEdge(source, target);
      PlanarDfsDetail<V, E> targetDetail = vertexDetails.get(target);
      PlanarDfsDetail<V, E> sourceDetail = vertexDetails.get(source);
      targetDetail.parent = source;
      targetDetail.dfsEdge = edge;
      targetDetail.leastAncestor = sourceDetail.dfsNumber;
   }

   @Override
   public void backEdge(V source, V target) {
      E edge = graph.getEdge(source, target);
      PlanarDfsDetail<V, E> sourceDetail = vertexDetails.get(source);
      PlanarDfsDetail<V, E> targetDetail = vertexDetails.get(target);
      if (!target.equals(sourceDetail.parent)) {
         int sourceLowPoint = sourceDetail.lowPoint;
         int targetDfsNumber = targetDetail.dfsNumber;
         int sourceLeastAncestor = sourceDetail.leastAncestor;
         sourceDetail.lowPoint = Math.min(sourceLowPoint, targetDfsNumber);
         sourceDetail.leastAncestor = Math.min(sourceLeastAncestor, targetDfsNumber);
      }
   }

   @Override
   public void finishVertex(V u) {
      PlanarDfsDetail<V, E> detail = vertexDetails.get(u);
      V parent = detail.parent;
      PlanarDfsDetail<V, E> parentDetail = vertexDetails.get(parent);
      int parentlowPoint = parentDetail.lowPoint;
      int lowPoint = detail.lowPoint;
      if (!parent.equals(u)) {
         parentDetail.lowPoint = Math.min(lowPoint, parentlowPoint);
      }
   }

   @Override
   public void initializeVertex(V u) {
   }

   @Override
   public void examineEdge(V source, V target) {
   }

   @Override
   public void forwardOrCrossEdge(V source, V target) {
   }

   public static class LowPointComparator<V, E> implements Comparator<V> {

      protected Map<V, PlanarDfsDetail<V, E>> vertexDetails;

      public LowPointComparator(Map<V, PlanarDfsDetail<V, E>> vertexDetails) {
         this.vertexDetails = vertexDetails;
      }

      @Override
      public int compare(V lhs, V rhs) {
         int lhsLowPoint = vertexDetails.get(lhs).lowPoint;
         int rhsLowPoint = vertexDetails.get(rhs).lowPoint;
         return lhsLowPoint > rhsLowPoint ? 1 : (lhsLowPoint < rhsLowPoint ? -1 : 0);
      }
   }

   public static class DfsNumberComparator<V, E> implements Comparator<V> {

      protected Map<V, PlanarDfsDetail<V, E>> vertexDetails;

      public DfsNumberComparator(Map<V, PlanarDfsDetail<V, E>> vertexDetails) {
         this.vertexDetails = vertexDetails;
      }

      @Override
      public int compare(V lhs, V rhs) {
         int lhsDfsNumber = vertexDetails.get(lhs).dfsNumber;
         int rhsDfsNumber = vertexDetails.get(rhs).dfsNumber;
         return lhsDfsNumber > rhsDfsNumber ? 1 : (lhsDfsNumber < rhsDfsNumber ? -1 : 0);
      }
   }

   public Map<V, PlanarDfsDetail<V, E>> getVertexDetails() {
      return vertexDetails;
   }

   public void setVertexDetails(Map<V, PlanarDfsDetail<V, E>> vertexDetails) {
      this.vertexDetails = vertexDetails;
   }

   public int getCount() {
      return count;
   }

   public void setCount(int count) {
      this.count = count;
   }
}
