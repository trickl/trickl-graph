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

import com.trickl.graph.edges.DirectedEdge;
import com.vividsolutions.jts.geom.Coordinate;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;

public class ChrobakPayneLayout<V, E> implements PlanarLayout<V> {

   private static class VertexDetail<V> {

      private V left = null;
      private V right = null;
      private int seen_as_right = 0;
      private int seen = 0;
      private int delta_x = 0;
      private int y = 0;
      private int x = 0;
      private boolean installed = false;
   }
   
   private PlanarGraph<V, E> graph;
   private double scale;
   Map<V, Coordinate> drawing;
   PlanarCanonicalOrdering ordering;
   
   public ChrobakPayneLayout(PlanarGraph<V, E> graph) {
      this(graph, 1.0);
   }
      
   public ChrobakPayneLayout(PlanarGraph<V, E> graph, double scale) {
      // This feels hacky. Having to make a copy of the graph so we can
      // make it maximal planar and get a canonical ordering for the algorithm
      DoublyConnectedEdgeList<V, E, Object> graphCopy = new DoublyConnectedEdgeList<V, E, Object>(graph, Object.class);
      MaximalPlanar<V, E> maximalPlanar = new MaximalPlanar<V, E>();
      maximalPlanar.makeMaximalPlanar(graphCopy);
      ordering = new MaximalPlanarCanonicalOrdering<V, E>();
      this.graph = graphCopy;
      this.scale = scale;

      layout();
   }

   public ChrobakPayneLayout(PlanarGraph<V, E> graph, PlanarCanonicalOrdering<V, E> ordering, double scale) {
      this.ordering = ordering;
      this.graph = graph;
      this.scale = scale;

      layout();
   }

   @Override
   public Coordinate getCoordinate(V vertex) {      
      return drawing.get(vertex);
   }

   private void layout() {
      
      DirectedEdge<V> boundary = graph.getBoundary();
      List<V> order = ordering.getOrder(graph, boundary.getSource());

      Map<V, VertexDetail<V>> vertexDetails = new HashMap<V, VertexDetail<V>>();
      for (V vertex : graph.vertexSet()) {
         vertexDetails.put(vertex, new VertexDetail<V>());
      }

      int timestamp = 1;
      List<V> installedNeighbours = new LinkedList<V>();
      
      V firstVertex = order.get(0);
      V secondVertex = order.get(1);
      V thirdVertex = order.get(2);

      VertexDetail<V> firstDetail = vertexDetails.get(firstVertex);
      VertexDetail<V> secondDetail = vertexDetails.get(secondVertex);
      VertexDetail<V> thirdDetail = vertexDetails.get(thirdVertex);

      secondDetail.delta_x = 1;
      thirdDetail.delta_x = 1;

      firstDetail.y = 0;
      secondDetail.y = 0;
      thirdDetail.y = 1;

      firstDetail.right = thirdVertex;
      thirdDetail.right = secondVertex;

      firstDetail.installed = secondDetail.installed = thirdDetail.installed = true;

      for (V vertex : order.subList(3, order.size())) {
         VertexDetail<V> detail = vertexDetails.get(vertex);

         // First, find the leftmost and rightmost neighbor of v on the outer
         // cycle of the embedding.
         // Note: since we're moving clockwise through the edges adjacent to v,
         // we're actually moving from right to left among v's neighbors on the
         // outer face (since v will be installed above them all) looking for
         // the leftmost and rightmost installed neighbours
         V leftmost = null;
         V rightmost = null;

         installedNeighbours.clear();

         V prevVertex = null;
         for (E edge : graph.edgesOf(vertex)) {
            V currentVertex = graph.getEdgeSource(edge).equals(vertex)
                    ? graph.getEdgeTarget(edge) : graph.getEdgeSource(edge);

            VertexDetail<V> currentDetail = vertexDetails.get(currentVertex);

            // Skip any self-loops or parallel edges
            if (currentVertex.equals(vertex) || currentVertex.equals(prevVertex)) {
               continue;
            }

            if (currentDetail.installed) {
               currentDetail.seen = timestamp;

               if (currentDetail.right != null) {
                  vertexDetails.get(currentDetail.right).seen_as_right = timestamp;
               }
               installedNeighbours.add(currentVertex);
            }

            prevVertex = currentVertex;
         }

         for (V vi : installedNeighbours) {
            VertexDetail<V> installeddetail = vertexDetails.get(vi);
            if (installeddetail.right == null
                    || vertexDetails.get(installeddetail.right).seen != timestamp) {
               rightmost = vi;
            }
            if (installeddetail.seen_as_right != timestamp) {
               leftmost = vi;
            }
         }

         ++timestamp;

         // Stretch gaps
         VertexDetail<V> rightmostDetail = vertexDetails.get(rightmost);
         VertexDetail<V> leftmostDetail = vertexDetails.get(leftmost);
         VertexDetail<V> leftmostRightDetail = vertexDetails.get(leftmostDetail.right);

         ++leftmostRightDetail.delta_x;
         ++rightmostDetail.delta_x;

         //adjust offsets
         int deltaPQ = 0;
         V stopVertex = rightmostDetail.right;
         for (V temp = leftmostDetail.right; 
             !(temp == stopVertex || (temp != null && temp.equals(stopVertex)));
                 temp = vertexDetails.get(temp).right) {
            deltaPQ += vertexDetails.get(temp).delta_x;
         }

         detail.delta_x = ((rightmostDetail.y + deltaPQ) - leftmostDetail.y) / 2;

         detail.y = leftmostDetail.y + detail.delta_x;
         rightmostDetail.delta_x = deltaPQ - detail.delta_x;

         boolean areLeftmostAndRightmostAdjacent = leftmostDetail.right.equals(rightmost);
         if (!areLeftmostAndRightmostAdjacent) {
            leftmostRightDetail.delta_x -= detail.delta_x;
         }

         // install v
         if (!areLeftmostAndRightmostAdjacent) {
            detail.left = leftmostDetail.right;
            V nextToRightMost = null;
            for (V temp = leftmost; !temp.equals(rightmost);
                    temp = vertexDetails.get(temp).right) {
               nextToRightMost = temp;
            }
            
            vertexDetails.get(nextToRightMost).right = null;            
         } else {
            detail.left = null;
         }

         leftmostDetail.right = vertex;
         detail.right = rightmost;
         detail.installed = true;
      }

      accumulateOffsets(order.iterator().next(), 0, vertexDetails);

      drawing = new HashMap<V, Coordinate>();
      for (V vi : graph.vertexSet()) {
         VertexDetail<V> drawdetail = vertexDetails.get(vi);
         drawing.put(vi, new Coordinate(drawdetail.x * scale, drawdetail.y * scale));
      }
   }

   private void accumulateOffsets(V v,
           int offset,
           Map<V, VertexDetail<V>> details) {
      if (v != null) {
         VertexDetail<V> detail = details.get(v);
         detail.x += detail.delta_x + offset;
         accumulateOffsets(detail.left, detail.x, details);
         accumulateOffsets(detail.right, detail.x, details);
      }
   }
}

