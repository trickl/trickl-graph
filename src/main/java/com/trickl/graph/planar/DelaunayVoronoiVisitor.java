package com.trickl.algo.graph.planar;

import com.trickl.algo.graph.planar.circlepacking.RadiusProvider;
import com.trickl.algo.graph.edges.DirectedEdge;
import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.algorithm.ConvexHull;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateList;
import com.vividsolutions.jts.geom.CoordinateSequenceFactory;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Triangle;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequenceFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.jgrapht.VertexFactory;

public class DelaunayVoronoiVisitor<V1, E1, V2, E2> extends DualGraphVisitor<V1, E1, V2, E2> implements PlanarLayout<V2> {

   final private PlanarLayout<V1> inputLocations;
   final private Map<V2, Coordinate> dualLocations;
   final private LinearRing boundary;
   final private List<V2> boundaryVertices;

   /**
    *
    * @param inputGraph
    * @param inputLocations
    * @param dualGraph
    * @param boundary Outward facing boundary
    * @param vertexFactory
    */
   public DelaunayVoronoiVisitor(PlanarGraph<V1, E1> inputGraph,
           PlanarLayout<V1> inputLocations,
           PlanarGraph<V2, E2> dualGraph,
           LinearRing boundary,
           VertexFactory<V2> vertexFactory) {
      super(inputGraph, dualGraph, vertexFactory);
      if (inputLocations == null) {
         throw new NullPointerException();
      }

      this.inputLocations = inputLocations;
      this.dualLocations = new Hashtable<V2, Coordinate>();
      this.boundary = boundary;
      this.boundaryVertices = new ArrayList<V2>(boundary == null ? 0
              : boundary.getCoordinates().length);

      if (boundary != null) {
         // Note that in a linear ring, end element equals start element
         for (int i = 0; i < boundary.getCoordinates().length - 1; ++i) {
            V2 boundaryVertex = vertexFactory.createVertex();
            boundaryVertices.add(boundaryVertex);
            dualLocations.put(boundaryVertex, boundary.getCoordinateN(i));
            this.dualGraph.addVertex(boundaryVertex);
         }
      }
   }

   @Override
   public void nextEdge(V1 source, V1 target) {
      super.nextEdge(source, target);

      V2 dualSource = getEdgeToVertexMap().get(new DirectedEdge<V1>(target, source));

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

      // TODO: Handle cases where the half lines to the boundary intersect
      // (currently causes an error about the before and after edges not
      // sharing a face - which is true...).

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
      if (!dualLocations.containsKey(voronoiSource)) {

         // Record the vertex before this boundary edge
         V2 voronoiBefore = dualGraph.containsEdge(voronoiTarget, voronoiSource)
                 ? dualGraph.getPrevVertex(voronoiTarget, voronoiSource) : null;
         if (inputGraph.isBoundary(delaunaySource, delaunayTarget)
                 || inputGraph.isBoundary(delaunayTarget, delaunaySource)) {
            // Remove the edge created by the dual graph visitor
            dualGraph.removeEdge(voronoiTarget, voronoiSource);
            getEdgeToVertexMap().remove(new DirectedEdge<V1>(delaunaySource, delaunayTarget));
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
            dualLocations.put(voronoiSource, circumcentre);
         } else {
            // Location is the intersection of the boundary and the perpendicular
            // bisector of the delaunay edge            
            V2 boundaryVertex = null;
            Coordinate midPoint = ab.midPoint();
            LineSegment perpendicularBisector = new LineSegment(
                    midPoint,
                    new Coordinate(midPoint.x - (b.y - a.y),
                    midPoint.y - (a.x - b.x)));

            // Check each segment in the boundary for intersection with the
            // bisector            
            double minDistance = Double.POSITIVE_INFINITY;
            int segmentIndex = -1;
            for (int itr = 0; itr < boundaryVertices.size(); ++itr) {
               Coordinate intersection = getBoundaryIntersection(perpendicularBisector, itr);
               if (intersection != null) {
                  // Find the nearest boundary intersection
                  double distance = midPoint.distance(intersection);
                  if (distance < minDistance) {
                     minDistance = distance;
                     segmentIndex = itr;
                  }
               }
            }

            if (segmentIndex >= 0) {
               int nextItr = (segmentIndex + 1) % boundaryVertices.size();
               LineSegment boundarySegment = getBoundarySegment(segmentIndex);
               Coordinate intersection = getBoundaryIntersection(perpendicularBisector, segmentIndex);

               if (intersection.equals(boundarySegment.p0)) {
                  boundaryVertex = boundaryVertices.get(segmentIndex);
               } else if (intersection.equals(boundarySegment.p1)) {
                  boundaryVertex = boundaryVertices.get(nextItr);
               } else {
                  boundaryVertex = vertexFactory.createVertex();
                  dualLocations.put(boundaryVertex, intersection);
                  boundaryVertices.add(nextItr, boundaryVertex);
               }

               // Create the new edge to this boundary intercept
               //if (dualGraph.edgeSet().size() < 20) {
                  E2 edge = edgeFactory.createEdge(voronoiTarget, boundaryVertex);
                  dualGraph.addEdge(voronoiTarget, boundaryVertex, voronoiBefore, null, edge);
                  getEdgeToVertexMap().put(new DirectedEdge<V1>(delaunaySource, delaunayTarget), boundaryVertex);
               //}
            }
         }
      }
   }
   
   private LineSegment getBoundarySegment(int boundarySegmentIndex) {      
      int nextItr = (boundarySegmentIndex + 1) % boundaryVertices.size();
      V2 boundarySource = boundaryVertices.get(boundarySegmentIndex);
      V2 boundaryTarget = boundaryVertices.get(nextItr);
      return new LineSegment(
              dualLocations.get(boundarySource),
              dualLocations.get(boundaryTarget));
   }

   private Coordinate getBoundaryIntersection(LineSegment line, int boundarySegmentIndex) {
      Coordinate boundaryIntersection = null;
      LineSegment boundarySegment = getBoundarySegment(boundarySegmentIndex);

      // The bisector needs to be extended to reach the boundary,
      // only project fowards (factor must be positive)
      double factor = Math.max(0, Math.max(line.projectionFactor(boundarySegment.p0),
              line.projectionFactor(boundarySegment.p1)));

      LineSegment extendedBisector =
              new LineSegment(line.p0, line.pointAlong(factor));

      // Check for intersection with this boundary segment
      boundaryIntersection = extendedBisector.intersection(boundarySegment);
      return boundaryIntersection;
   }

   @Override
   public Coordinate getCoordinate(V2 vertex) {
      return dualLocations.get(vertex);


   }

   public static <V> LinearRing getOffsetBoundary(List<V> boundaryVertices, PlanarLayout<V> planarLayout, RadiusProvider<V> radiusProvider) {

      CoordinateSequenceFactory coordinateSequenceFactory = CoordinateArraySequenceFactory.instance();
      GeometryFactory geometryFactory = new GeometryFactory(coordinateSequenceFactory);



      if (boundaryVertices.isEmpty()) {
         return null;
      }

      CoordinateList boundaryCoords = new CoordinateList();


      for (int i = 0; i
              < boundaryVertices.size();
              ++i) {
         V prevVertex = boundaryVertices.get(i);
         V vertex = boundaryVertices.get((i + 1) % boundaryVertices.size());
         V nextVertex = boundaryVertices.get((i + 2) % boundaryVertices.size());

         Coordinate a = planarLayout.getCoordinate(prevVertex);
         Coordinate b = planarLayout.getCoordinate(vertex);
         Coordinate c = planarLayout.getCoordinate(nextVertex);
         LineSegment perpendicularBisector = new LineSegment(
                 b,
                 new Coordinate(b.x - (c.y - a.y),
                 b.y - (a.x - c.x)));

         // Calculate an offset to guarantee bisectors do not intersect before
         // meeting boundary
         double radiusA = radiusProvider.getRadius(prevVertex);
         double radiusB = radiusProvider.getRadius(vertex);
         double radiusC = radiusProvider.getRadius(nextVertex);
         double effectiveRadius = Math.min((radiusA + radiusB) / 2,
                                           (radiusB + radiusC) / 2);
         Coordinate boundaryPoint = perpendicularBisector.pointAlong(effectiveRadius / perpendicularBisector.getLength());
         boundaryCoords.add(boundaryPoint);
      }

      // Close the boundary
      boundaryCoords.add(boundaryCoords.get(0));
      LinearRing offsetBoundary = new LinearRing(coordinateSequenceFactory.create(boundaryCoords.toCoordinateArray()),
              geometryFactory);


      return offsetBoundary;


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
