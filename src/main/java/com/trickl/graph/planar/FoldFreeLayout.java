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

import com.trickl.graph.BreadthFirstSearch;
import com.trickl.graph.HopCountVisitor;
import com.trickl.graph.SpanningSearchVisitor;
import com.vividsolutions.jts.geom.Coordinate;
import java.util.HashMap;
import java.util.Map;

/*
 * This algorithm is described in the following paper:
 * Anchor-Free Distributed Localization in Sensor Networks
 *    Nissanka B. Priyantha, Hari Balakrishnan, Erik Demaine, and Seth Teller
 *       Tech Report #892, April 15, 2003
 *       MIT Laboratory for Computer Science
 *       http://nms.lcs.mit.edu/cricket/
 *
 */
public class FoldFreeLayout<V, E> implements PlanarLayout<V> {

   public static class LastVertexVisitor<V, E> implements SpanningSearchVisitor<V, E> {

      V lastVertex;

      public LastVertexVisitor() {
      }

      @Override
      public void initializeVertex(V u) {
      }

      @Override
      public void startVertex(V u) {
      }

      @Override
      public void discoverVertex(V u) {
      }

      @Override
      public void examineEdge(V source, V target) {
      }

      @Override
      public void treeEdge(V source, V target) {
      }

      @Override
      public void backEdge(V source, V target) {
      }

      @Override
      public void forwardOrCrossEdge(V source, V target) {
      }

      @Override
      public void finishVertex(V u) {
         this.lastVertex = u;
      }

      public V getLastVertex() {
         return lastVertex;
      }
   }
   private PlanarGraph<V, E> graph;
   private BreadthFirstSearch<V, E> breadthFirstSearch;
   private double scale = 100;
   private Coordinate centre;
   private V initialVertex;

   public FoldFreeLayout(PlanarGraph<V, E> graph) {
      this(graph, null);
   }

   public FoldFreeLayout(PlanarGraph<V, E> graph, V initialVertex) {
      this.graph = graph;
      this.breadthFirstSearch = new BreadthFirstSearch<V, E>(graph);
      this.centre = new Coordinate(0, 0);
      if (initialVertex == null && !graph.vertexSet().isEmpty())
      {
         this.initialVertex = graph.vertexSet().iterator().next();
      }
      else {
         this.initialVertex = initialVertex;
      }
   }

   Map<V, Coordinate> vertexLocations;

   public double getScale() {
      return scale;
   }

   public void setScale(double scale) {
      this.scale = scale;
   }

   public Coordinate getCentre() {
      return centre;
   }

   public void setCentre(Coordinate centre) {
      this.centre = centre;
   }
   
   @Override
   public Coordinate getCoordinate(V vertex) {
      if (vertexLocations == null) layout();
      return vertexLocations.get(vertex);
   }

   private void layout() {
      if (initialVertex == null) return;

      // Select [n1] to maximize h[0, 1]
      LastVertexVisitor<V, E> lastVertexVisitor = new LastVertexVisitor<V, E>();
      breadthFirstSearch.traverse(initialVertex, lastVertexVisitor);
      V n1 = lastVertexVisitor.getLastVertex();

      // Select [n2] to maximize h[1, 2]
      Map<V, Integer> h1 = new HashMap<V, Integer>();
      breadthFirstSearch.traverse(n1, lastVertexVisitor);
      breadthFirstSearch.traverse(n1, new HopCountVisitor<V, E>(h1));
      V n2 = lastVertexVisitor.getLastVertex();

      // Select [n3] to minimize h[1,3] - h[2,3], while maximizing h[1,3] + h[2, 3] in a tie
      Map<V, Integer> h2 = new HashMap<V, Integer>();
      breadthFirstSearch.traverse(n2, new HopCountVisitor<V, E>(h2));
      V n3 = n1;
      int h1323minDiff = Integer.MAX_VALUE;
      int h1323maxSum = 0;
      for (V v : graph.vertexSet()) {
         Integer h13 = h1.get(v);
         Integer h23 = h2.get(v);

         if (h13 != null || h23 != null) {
            int h1323diff = Math.abs(h13 - h23);
            int h1323sum = h13 + h23;

            if (h1323diff < h1323minDiff
                    || (h1323diff == h1323minDiff && h1323sum > h1323maxSum)) {
               h1323minDiff = h1323diff;
               h1323maxSum = h1323sum;
               n3 = v;
            }
         }
      }

      // Select [n4] to minimize h[1,4] - h[2,4], while maximizing h[3,4] in a tie
      Map<V, Integer> h3 = new HashMap<V, Integer>();
      breadthFirstSearch.traverse(n3, new HopCountVisitor<V, E>(h3));
      V n4 = n1;
      int h1424minDiff = Integer.MAX_VALUE;
      int h34max = 0;
      for (V v : graph.vertexSet()) {
         Integer h14 = h1.get(v);
         Integer h24 = h2.get(v);

         if (h14 != null && h24 != null) {
            int h1424_diff = Math.abs(h14 - h24);
            int h34 = h3.get(v);

            if (h1424_diff < h1424minDiff
                    || (h1424_diff == h1424minDiff && h34 > h34max)) {
               h1424minDiff = h1424_diff;
               h34max = h34;
               n4 = v;
            }
         }
      }

      // Select [n5] to minimize h[1,5] - h[2,5], while minimizing h[3,5] - h[4,5] in a tie
      Map<V, Integer> h4 = new HashMap<V, Integer>();
      breadthFirstSearch.traverse(n4, new HopCountVisitor<V, E>(h4));

      V n5 = n1;
      int h1525minDiff = Integer.MAX_VALUE;
      int h3545minDiff = Integer.MAX_VALUE;
      for (V v : graph.vertexSet()) {
         Integer h15 = h1.get(v);
         Integer h25 = h2.get(v);
         Integer h35 = h3.get(v);
         Integer h45 = h4.get(v);

         if (h15 != null & h25 != null && h35 != null && h45 != null) {
            int h1525diff = Math.abs(h15 - h25);
            int h3545diff = Math.abs(h35 - h45);

            if (h1525diff < h1525minDiff
                    || (h1525diff == h1525minDiff && h3545diff < h3545minDiff)) {
               h1525minDiff = h1525diff;
               h3545minDiff = h3545diff;
               n5 = v;
            }
         }
      }

      // Get the hop_counts from [n5]
      Map<V, Integer> h5 = new HashMap<V, Integer>();
      breadthFirstSearch.traverse(n5, new HopCountVisitor<V, E>(h5));

      vertexLocations = new HashMap<V, Coordinate>();
      for (V v : graph.vertexSet()) {
         double radius = h5.get(v) * scale;
         double theta = Math.atan2((double) h2.get(v) - (double) h1.get(v),
                                   (double) h4.get(v) - (double) h3.get(v));

         vertexLocations.put(v, new Coordinate(centre.x + (radius * Math.cos(theta)),
                                       centre.y + (radius * Math.sin(theta))));
      }
   }
}


