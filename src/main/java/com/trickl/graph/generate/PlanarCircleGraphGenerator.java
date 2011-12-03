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
package com.trickl.graph.generate;

import com.trickl.graph.planar.PlanarGraph;
import com.trickl.graph.planar.PlanarGraphs;
import com.trickl.graph.planar.PlanarLayout;
import com.vividsolutions.jts.geom.Coordinate;
import java.util.Map;
import java.util.Hashtable;
import java.util.PriorityQueue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jgrapht.VertexFactory;

public class PlanarCircleGraphGenerator<V, E>
        implements PlanarGraphGenerator<V, E, V>, PlanarLayout<V> {

   private static class Site implements Comparable<Site>, PlanarLayout<Coordinate> {
      // Logical Co-ords
      final private static double tolerance = 1e-6;
      final private double scale;
      final private Coordinate logicalPosition;
      final private int discoveryDirection;
      final private AngleComparator<Coordinate> angleComparator;

      public Site(Coordinate logicalPosition, double scale) {
         this(logicalPosition, scale, 0);
      }

      public Site(Coordinate logicalPosition, double scale, int discoveryDirection) {
         this.logicalPosition = logicalPosition;
         this.discoveryDirection = discoveryDirection;
         this.scale = scale;
         angleComparator = new AngleComparator<Coordinate>(new Coordinate(0, 0), new Coordinate(1, 0), this);
      }

      public Coordinate getLogicalPosition() {
         return logicalPosition;
      }

      @Override
      public Coordinate getCoordinate(Coordinate coord) {
         return coord;
      }

      public Coordinate getCoordinate() {
         return new Coordinate(Math.sqrt(3) * scale * logicalPosition.x, scale * logicalPosition.y);
      }

      public double distanceToOrigin() {
         return getCoordinate().distance(new Coordinate(0, 0));
      }

      private int getDiscoveryDirection() {
         return discoveryDirection;
      }

      /**
       * Natural order of sites is those closest to the origin and
       * in the case of a tie, clockwise.
       * @param other
       * @return
       */
      @Override
      public int compareTo(Site other) {
         if (Math.abs(distanceToOrigin() - other.distanceToOrigin()) < tolerance) {
            return -angleComparator.compare(this.getCoordinate(), other.getCoordinate());
         } else if (distanceToOrigin() > other.distanceToOrigin()) {
            return 1;
         } else {
            return -1;
         }
      }

      @Override
      public boolean equals(Object other) {
         if (other instanceof Site
             && ((Site) other).logicalPosition.equals(logicalPosition))
            return true;
         return false;
      }

      @Override
      public int hashCode() {
         int hash = 3;         
         hash = 97 * hash + (this.logicalPosition != null ? this.logicalPosition.hashCode() : 0);
         return hash;
      }
   }

   private static class AngleComparator<V> implements Comparator<V> {

      final private V source;
      final private V pivot;      
      final private PlanarLayout<V> planarLayout;

      AngleComparator(V source, V pivot, PlanarLayout<V> planarLayout) {
         this.source = source;
         this.pivot = pivot;         
         this.planarLayout = planarLayout;
      }

      @Override
      public int compare(V first, V second) {
         // Compare angle with respect to the common vertex (the pivot)            
         return -Double.compare(getAngle(source, pivot, first),
                               getAngle(source, pivot, second));
      }

      private double getAngle(V source, V pivot, V target) {
         double angle = 0;
         if (source.equals(target)) return 0;
         Coordinate sourcePosition = planarLayout.getCoordinate(source);
         Coordinate pivotPosition  = planarLayout.getCoordinate(pivot);
         Coordinate targetPosition = planarLayout.getCoordinate(target);
         Coordinate pivotTargetPosition =
                 new Coordinate(targetPosition.x - pivotPosition.x,
                                    targetPosition.y - pivotPosition.y);
         Coordinate pivotSourcePosition =
                 new Coordinate(sourcePosition.x - pivotPosition.x,
                                    sourcePosition.y - pivotPosition.y);
         angle = -Math.atan2(pivotSourcePosition.x, pivotSourcePosition.y)
                 +Math.atan2(pivotTargetPosition.x, pivotTargetPosition.y);
         return -angle;
      }
   }

   final private int size;
   final private double scale;
   final private Map<V, Site> sites = new Hashtable<V, Site>();

   public PlanarCircleGraphGenerator(int size) {
      this(size, 100);
   }

   public PlanarCircleGraphGenerator(int size, double scale) {
      this.size = size;
      this.scale = scale;
   }

   @Override
   public Coordinate getCoordinate(V vertex) {
      return sites.get(vertex).getCoordinate();
   }

   @Override
   public void generateGraph(PlanarGraph<V, E> graph, VertexFactory<V> vertexFactory,
           java.util.Map<java.lang.String, V> resultMap) {
      ArrayList<V> vertices = new ArrayList<V>(size);

      for (int k = 0; k < size; ++k) {
         V vertex = vertexFactory.createVertex();
         vertices.add(vertex);
         graph.addVertex(vertex);
      }
      
      Map<Coordinate, Integer> existingSites = new Hashtable<Coordinate, Integer>();
      PriorityQueue<Site> potentialSitesQueue = new PriorityQueue<Site>();
      potentialSitesQueue.add(new Site(new Coordinate(0, 0), scale));

      // Find the nearest potential vertex to the centre
      for (int k = 0; k < size; ++k) {
         Site site = potentialSitesQueue.poll();
         V vertex = vertices.get(k);

         Coordinate position = site.getLogicalPosition();
         sites.put(vertices.get(k), site);
         existingSites.put(position, k);

         // Add new potential sites as neighbours of this one
         for (int orientationIndex = 0; orientationIndex < 6; ++orientationIndex) {
            // Base the neighbour on a logical hexagonal grid
            Coordinate neighbourPosition = new Coordinate(position.x, position.y);

            // Search for neighbours starting by looking outwards from the grid
            // (outwards can be determined by the site discovery orientation)
            int direction = (orientationIndex + site.getDiscoveryDirection()) % 6;
            switch (direction) {
               case 0:
                  neighbourPosition.y += 2;
                  break;
               case 1:
                  neighbourPosition.x -= 1;
                  neighbourPosition.y += 1;
                  break;
               case 2:
                  neighbourPosition.x -= 1;
                  neighbourPosition.y -= 1;
                  break;
               case 3:
                  neighbourPosition.y -= 2;
                  break;
               case 4:
                  neighbourPosition.x += 1;
                  neighbourPosition.y -= 1;
                  break;
               case 5:
                  neighbourPosition.x += 1;
                  neighbourPosition.y += 1;
                  break;
            }

            Integer k_neighbour = existingSites.get(neighbourPosition);
            if (k_neighbour != null) {
               V neighbour = vertices.get(k_neighbour);
               addEdge(graph, neighbour, vertex);         
               addEdge(graph, vertex, neighbour);                                                                                 
            } else {
               Site neighbourSite = new Site(neighbourPosition, scale, direction);
               if (!potentialSitesQueue.contains(neighbourSite)) {
                  potentialSitesQueue.add(neighbourSite);
               }
            }
         }
      }
   }

   private E addEdge(PlanarGraph<V, E> graph, V source, V target) {
      if (graph.containsEdge(source, target)) return
          graph.getEdge(source, target);

      AngleComparator<V> angleComparator = new AngleComparator<V>(target, source, this);
      List<V> perimeter = PlanarGraphs.getConnectedVertices(graph, source);
      V before = null;
      if (!perimeter.isEmpty())
      {
         Collections.sort(perimeter, angleComparator);
         int index = Collections.binarySearch(perimeter, target, angleComparator);
         if (index < 0) {
            // Find the first edge clockwise from this edge
            before = perimeter.get((-index - 2 + perimeter.size()) % perimeter.size());
         }
         else {
            before = perimeter.get(index);
         }
      }

      return graph.addEdge(source, target, before, null);
   }
}
