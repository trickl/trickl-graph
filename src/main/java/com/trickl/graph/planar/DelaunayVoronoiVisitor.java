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
import com.vividsolutions.jts.algorithm.Angle;
import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.algorithm.ConvexHull;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequenceFactory;
import java.util.*;
import org.jgrapht.VertexFactory;

public class DelaunayVoronoiVisitor<V1, E1, V2, E2> extends DualGraphVisitor<V1, E1, V2, E2> implements PlanarLayout<V2> {

   final private PlanarLayout<V1> inputLocations;
   final private Map<V2, Coordinate> vertexToCoordinate;
   final private LinearRing boundary;
   final private List<V2> boundaryVertices;

   /**
    *
    * @param inputGraph
    * @param inputLayout
    * @param dualGraph
    * @param boundary Outward facing boundary
    * @param vertexFactory
    */
   public DelaunayVoronoiVisitor(PlanarGraph<V1, E1> inputGraph,
           PlanarLayout<V1> inputLayout,
           PlanarGraph<V2, E2> dualGraph,
           LinearRing boundary,
           VertexFactory<V2> vertexFactory) {
      super(inputGraph, dualGraph, vertexFactory);
      if (inputLayout == null) {
         throw new NullPointerException();
      }

      // Validate that dual graph boundary is convex
      if (!PlanarGraphs.isBoundaryConvex(inputGraph, inputLayout)) {
         throw new IllegalArgumentException("Delaunay graphs must have a convex boundary.");
      }

      this.inputLocations = inputLayout;
      this.vertexToCoordinate = new HashMap<V2, Coordinate>();
      this.boundary = boundary;
      this.boundaryVertices = new ArrayList<V2>(boundary == null ? 0
              : boundary.getCoordinates().length);

      if (boundary != null) {
         // Note that in a linear ring, end element equals start element
         for (int i = 0; i < boundary.getCoordinates().length - 1; ++i) {
            Coordinate coord = boundary.getCoordinateN(i);
            Coordinate nextCoord = boundary.getCoordinateN(i + 1);
            Coordinate nextNextCoord = boundary.getCoordinateN((i + 2) 
                    % (boundary.getCoordinates().length - 1));
            if (Angle.getTurn(Angle.angle(coord, nextCoord),
                    Angle.angle(nextCoord, nextNextCoord))
                    == Angle.COUNTERCLOCKWISE) {
               throw new IllegalArgumentException("Boundary must be defined clockwise");
            }

            V2 boundaryVertex = vertexFactory.createVertex();
            boundaryVertices.add(boundaryVertex);
            vertexToCoordinate.put(boundaryVertex, coord);
            this.dualGraph.addVertex(boundaryVertex);
         }
      }
   }

   @Override
   public void nextEdge(V1 source, V1 target) {
      super.nextEdge(source, target);

      V2 dualSource = getFaceToVertexMap().get(new DirectedEdge<V1>(target, source));

      // Position the vertex inside the boundary first      
      if (dualSource != null && dualTarget != null
              && dualGraph.containsEdge(dualSource, dualTarget)) {
         locateVertex(inputGraph, target, source, dualSource, dualTarget);
         locateVertex(inputGraph, source, target, dualTarget, dualSource);
      }
   }

   @Override
   public void endTraversal() {
      super.endTraversal();

      // Assumes the boundary is convex so half lines should never intersect.
      // Join up the boundary vertices, note the boundary faces
      // outwards so we take care to create an inward face

      Collections.reverse(boundaryVertices);
      for (int prevItr = 0; prevItr < boundaryVertices.size(); ++prevItr) {
         int itr = (prevItr + 1) % boundaryVertices.size();
         int nextItr = (prevItr + 2) % boundaryVertices.size();
         V2 boundaryPrevious = boundaryVertices.get(prevItr);
         V2 boundarySource = boundaryVertices.get(itr);
         V2 boundaryTarget = boundaryVertices.get(nextItr);
         V2 boundaryBefore = null;

         if (dualGraph.containsEdge(boundarySource, boundaryPrevious)) {
            boundaryBefore = dualGraph.getPrevVertex(boundarySource, boundaryPrevious);
         } else if (PlanarGraphs.isVertexBoundary(dualGraph, boundarySource)) {
            boundaryBefore = PlanarGraphs.getPrevVertexOnBoundary(dualGraph, boundarySource);
         }

         E2 edge = edgeFactory.createEdge(boundarySource, boundaryTarget);
         dualGraph.addEdge(boundarySource, boundaryTarget, boundaryBefore, null, edge);
      }

   }

   private void locateVertex(PlanarGraph<V1, E1> inputGraph,
           V1 delaunaySource,
           V1 delaunayTarget,
           V2 voronoiSource,
           V2 voronoiTarget) {
      if (!vertexToCoordinate.containsKey(voronoiSource)) {

         // Record the vertex before this boundary edge
         V2 voronoiBefore = dualGraph.containsEdge(voronoiTarget, voronoiSource)
                 ? dualGraph.getPrevVertex(voronoiTarget, voronoiSource) : null;
         if (inputGraph.isBoundary(delaunaySource, delaunayTarget)
                 || inputGraph.isBoundary(delaunayTarget, delaunaySource)) {
            // Remove the edge created by the dual graph visitor
            dualGraph.removeEdge(voronoiTarget, voronoiSource);
            getFaceToVertexMap().remove(new DirectedEdge<V1>(delaunaySource, delaunayTarget));
            if (voronoiBefore != null && voronoiBefore.equals(voronoiSource)) {
               // This edge was removed
               voronoiBefore = null;
            }
         }

         // Location is the cirumcenter of face, which is also the circumcenter of
         // any set of three points of the face
         Coordinate a = inputLocations.getCoordinate(delaunaySource);
         Coordinate b = inputLocations.getCoordinate(delaunayTarget);
         Coordinate c = inputLocations.getCoordinate(inputGraph.getNextVertex(delaunaySource, delaunayTarget));
         LineSegment ab = new LineSegment(a, b);

         // Inner faces should be defined counterclockwise by convention
         Coordinate circumcentre = !inputGraph.isBoundary(delaunaySource, delaunayTarget)
                 ? Triangle.circumcentre(a, b, c) : null;

         if (circumcentre != null
                 && CGAlgorithms.isPointInRing(circumcentre, boundary.getCoordinates())) {
            // Location is the cirumcenter of face, which is also the circumcenter of
            // any set of three points of the face                        
            vertexToCoordinate.put(voronoiSource, circumcentre);
         } else {
            // Location is the intersection of the boundary and the perpendicular
            // bisector of the delaunay edge                        
            Coordinate midPoint = ab.midPoint();
            V2 boundaryVertex = createVertexAtBoundaryInterception(new LineSegment(
                    midPoint,
                    new Coordinate(midPoint.x - (b.y - a.y),
                    midPoint.y - (a.x - b.x))),
                       boundaryVertices,
                       vertexFactory,
                       this);
            if (boundaryVertex != null) {
              // Create the new edge to this boundary intercept
               E2 edge = edgeFactory.createEdge(voronoiTarget, boundaryVertex);
               dualGraph.addEdge(voronoiTarget, boundaryVertex, voronoiBefore, null, edge);
               getFaceToVertexMap().put(new DirectedEdge<V1>(delaunaySource, delaunayTarget), boundaryVertex);
            }
         }
      }
   }
   
   private V2 createVertexAtBoundaryInterception(LineSegment halfLine, List<V2> boundaryVertices, VertexFactory<V2> vertexFactory, PlanarLayout<V2> layout) {
      V2 boundaryVertex = null;
      int segmentIndex = PlanarGraphs.getNearestInterceptingLineSegment(halfLine, boundaryVertices, layout);      
      if (segmentIndex >= 0) {
         int nextItr = (segmentIndex + 1) % boundaryVertices.size();
         LineSegment boundarySegment = PlanarGraphs.getLineSegment(segmentIndex, boundaryVertices, layout);
         Coordinate intersection = PlanarGraphs.getHalfLineIntersection(halfLine, boundarySegment);

         if (intersection.equals(boundarySegment.p0)) {
            boundaryVertex = boundaryVertices.get(segmentIndex);
         } else if (intersection.equals(boundarySegment.p1)) {
            boundaryVertex = boundaryVertices.get(nextItr);
         } else {
            boundaryVertex = vertexFactory.createVertex();
            vertexToCoordinate.put(boundaryVertex, intersection);
            boundaryVertices.add(nextItr, boundaryVertex);
         }
      }

      return boundaryVertex;
   }

   @Override
   public Coordinate getCoordinate(V2 vertex) {
      return vertexToCoordinate.get(vertex);
   }

   public static <V> List<V> getConvexBoundaryVertices(List<V> vertices, PlanarLayout<V> planarLayout) {
      Map<Coordinate, V> coordinateToVertex = new HashMap<Coordinate, V>();
      CoordinateList flattenedLocations = new CoordinateList();

      for (V vertex : vertices) {
         Coordinate location = planarLayout.getCoordinate(vertex);

         if (location != null) {
            flattenedLocations.add(location);
            coordinateToVertex.put(location, vertex);
         }
      }

      CoordinateSequenceFactory coordinateSequenceFactory = CoordinateArraySequenceFactory.instance();
      GeometryFactory geometryFactory = new GeometryFactory(coordinateSequenceFactory);
      ConvexHull convexHull = new ConvexHull(flattenedLocations.toCoordinateArray(), geometryFactory);
      Coordinate[] boundary = convexHull.getConvexHull().getCoordinates();

      List<V> boundaryVertices = new LinkedList<V>();

      for (int i = 0; i < (boundary.length - 1); ++i) {
         boundaryVertices.add(coordinateToVertex.get(boundary[i]));
      }

      return boundaryVertices;
   }
}
