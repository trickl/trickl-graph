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
package com.trickl.graph.planar.generate;

import com.trickl.graph.edges.DirectedEdge;
import com.trickl.graph.planar.PlanarGraph;
import com.trickl.graph.planar.PlanarGraphs;
import com.trickl.graph.planar.PlanarLayout;
import com.vividsolutions.jts.algorithm.Angle;
import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.*;
import java.io.PrintStream;
import java.util.*;
import org.jgrapht.VertexFactory;

/*
 * See "A sweepline algorithm for Voronoi diagrams" by Steven Fortune.
 * ALGORITHMICA Volume 2, Numbers 1-4, 153-174, DOI: 10.1007/BF01840357 O(n)
 * space and O(n log n) time complexity in the worst-case
 */
public class FortuneVoronoiGraphGenerator<V, E> implements PlanarGraphGenerator<V, E, V>, PlanarLayout<V> {

   // The binary tree status class for storing the beach line         
   static class BeachLineTree<V> {

      // Head node
      BreakPoint<V> head;

      static abstract class Node<V> {
         // Doubly-linked for dual-direction traversal

         BreakPoint<V> parent;
      };

      // The branch nodes represent breakpoints between the parabolic arcs
      static class BreakPoint<V> extends Node<V> {

         Node<V> right;
         Node<V> left;
         // Twin the the break in the opposite direction caused by the same two sites
         BreakPoint<V> twin;
         // The next break which is clockwise from the same source
         BreakPoint<V> sourcePrev;
         V prevVertex;
         V sourceVertex;
         // TODO: Not necessary, can derive from tree
         Coordinate rightSite;
         Coordinate leftSite;

         BreakPoint(Node<V> left, Node<V> right, Coordinate leftSite, Coordinate rightSite) {
            this.left = left;
            this.right = right;
            this.leftSite = leftSite;
            this.rightSite = rightSite;

            if (right != null) {
               right.parent = this;
            }
            if (left != null) {
               left.parent = this;
            }
         }

         @Override
         public String toString() {
            return "L: " + leftSite
                    + ",R: " + rightSite
                    //+  "LP: " + (sourcePrev == null ? "*" : sourcePrev.leftSite)
                    //+ ",RP: " + (sourcePrev == null ? "*" : sourcePrev.rightSite)
                    + ",Prev: " + prevVertex
                    + ",Source: " + sourceVertex;
         }
      };

      // The leaf nodes represent arcs on the beach
      static class Arc<V> extends Node<V> {

         Coordinate site;
         CircleEvent<V> circleEvent;

         Arc(Coordinate site) {
            this.site = site;
         }

         @Override
         public String toString() {
            return "Site: " + site
                    + ((circleEvent != null && circleEvent.arc != null)
                    ? (",CircleEvent: " + circleEvent.vertexLocation) : "");

         }
      };

      BeachLineTree() {
         head = new BreakPoint<V>(null, null, null, null);
      }

      static <V> void delete(Arc<V> arc) {
         Node<V> replacementNode;
         if (arc.parent.left == arc) {
            replacementNode = arc.parent.right;
         } else {
            replacementNode = arc.parent.left;
         }
         BeachLineTree.replace(arc.parent, replacementNode);
      }

      private static <V> void replace(Node<V> original, Node<V> replacement) {
         if (original.parent.right == original) {
            original.parent.right = replacement;
         } else {
            original.parent.left = replacement;
         }
         replacement.parent = original.parent;
         original.parent = null;
      }

      Arc<V> getNextLeftArc(Arc<V> start) {
         Node<V> node = getLeftBreak(start);

         if (node instanceof BreakPoint) {
            node = ((BreakPoint<V>) node).left;
         }

         while (node instanceof BreakPoint) {
            node = ((BreakPoint<V>) node).right;
         }

         return (Arc<V>) node;
      }

      Arc<V> getNextRightArc(Arc<V> start) {
         Node<V> node = getRightBreak(start);

         if (node instanceof BreakPoint) {
            node = ((BreakPoint<V>) node).right;
         }

         while (node instanceof BreakPoint) {
            node = ((BreakPoint<V>) node).left;
         }

         return (Arc<V>) node;
      }

      BreakPoint<V> getLeftBreak(Arc<V> start) {
         Node<V> node = start;
         while (node != head && node.parent.left == node) {
            node = node.parent;
         }
         node = node.parent;

         if (node == head) {
            node = null;
         }

         return (BreakPoint<V>) node;
      }

      BreakPoint<V> getRightBreak(Arc<V> start) {
         Node<V> node = start;
         while (node != head && node.parent.right == node) {
            node = node.parent;
         }

         node = node.parent;

         if (node == head) {
            node = null;
         }

         return (BreakPoint<V>) node;
      }

      BreakPoint<V> getLeftmostBreak() {
         Node<V> node = head.left;
         BreakPoint<V> brk = (BreakPoint<V>) node;
         while (brk.left instanceof BreakPoint) {
            node = brk.left;
         }

         return (BreakPoint<V>) node;
      }

      BreakPoint<V> getRightmostBreak() {
         Node<V> node = head.left;
         BreakPoint<V> brk = (BreakPoint<V>) node;
         while (brk.right instanceof BreakPoint) {
            node = brk.right;
         }

         return (BreakPoint<V>) node;
      }
      
      List<BeachLineTree.BreakPoint<V>> getInorderBreaks() {
         List<BeachLineTree.BreakPoint<V>> breaks = new LinkedList<BeachLineTree.BreakPoint<V>>();

         // Get an inorder traversal of the BeachLineTree
         Stack<BeachLineTree.BreakPoint<V>> breakStack = new Stack<BeachLineTree.BreakPoint<V>>();
         BeachLineTree.Node<V> current = head.left;
         boolean done = false;
         while (!done) {
            if (current instanceof BeachLineTree.BreakPoint) {
               BeachLineTree.BreakPoint<V> brk = (BeachLineTree.BreakPoint<V>) current;
               breakStack.push(brk);
               current = brk.left;
            } else {
               if (!breakStack.isEmpty()) {
                  BeachLineTree.BreakPoint<V> brk = breakStack.pop();
                  current = brk;
                  breaks.add(brk);
                  current = brk.right;
               } else {
                  done = true;
               }
            }
         }
         
         return breaks;
      }
   }

   static interface SweepEvent {

      double getPosition();
   }

   static class SweepEventComparator implements Comparator<SweepEvent> {

      PrecisionModel precisionModel;

      SweepEventComparator(PrecisionModel precisionModel) {
         this.precisionModel = precisionModel;
      }

      @Override
      public int compare(SweepEvent lhs, SweepEvent rhs) {
         return new Double(precisionModel.makePrecise(lhs.getPosition())).compareTo(
                 new Double(precisionModel.makePrecise(rhs.getPosition())));
      }
   }

   static class SiteEvent implements SweepEvent {

      Coordinate site;

      SiteEvent(Coordinate site) {
         this.site = site;
      }

      @Override
      public double getPosition() {
         return site.x;
      }
   }

   static class CircleEvent<V> implements SweepEvent {

      // The arc that will be removed by the circle event
      BeachLineTree.Arc<V> arc;
      Coordinate vertexLocation;
      Coordinate eventLocation;

      CircleEvent(BeachLineTree.Arc<V> node, Coordinate vertexLocation, Coordinate eventLocation) {
         this.arc = node;
         this.vertexLocation = vertexLocation;
         this.eventLocation = eventLocation;
      }

      @Override
      public double getPosition() {
         return eventLocation.x;
      }
   }
   private Collection<Coordinate> sites = new ArrayList<Coordinate>();
   private Map<V, Coordinate> vertexToCoordinate = new HashMap<V, Coordinate>();
   // Mapping from a face edge to a site
   private Map<DirectedEdge<V>, Coordinate> faceToCoordinate = new HashMap<DirectedEdge<V>, Coordinate>();
   // Boundary of calculation
   private LinearRing boundary;
   private GeometryFactory geometryFactory = new GeometryFactory();

   public FortuneVoronoiGraphGenerator(Collection<Coordinate> sites,
           LinearRing boundary) {
      this.sites = sites;
      this.boundary = boundary;
   }

   public FortuneVoronoiGraphGenerator(Collection<Coordinate> sites) {
      this(sites, null);
   }

   @Override
   public void generateGraph(PlanarGraph<V, E> graph, VertexFactory<V> vertexFactory, Map<String, V> resultMap) {

      vertexToCoordinate.clear();
      faceToCoordinate.clear();

      // Define the boundary
      List<V> boundaryVertices = new ArrayList<V>(boundary == null ? 0
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

            V boundaryVertex = vertexFactory.createVertex();
            boundaryVertices.add(boundaryVertex);
            vertexToCoordinate.put(boundaryVertex, coord);
            graph.addVertex(boundaryVertex);
         }
      }

      // Initialize with an empty beach tree line and just site events
      BeachLineTree<V> beachLineTree = new BeachLineTree<V>();
      Queue<SweepEvent> eventQueue = new PriorityQueue<SweepEvent>(sites.size() * 2, new SweepEventComparator(geometryFactory.getPrecisionModel()));
      for (Coordinate site : sites) {
         eventQueue.add(new SiteEvent(site));
      }

      while (!eventQueue.isEmpty()) {
         processEventQueue(eventQueue, beachLineTree, graph, vertexFactory);
         //System.out.println("------------ Beach Line ---------------");
         //printBeachLineTree(beachLineTree.head.left, System.out);
         //System.out.println();
      }
      postProcessingEventQueue(beachLineTree, boundaryVertices, graph, vertexFactory);
   }

   @Override
   public Coordinate getCoordinate(V vertex) {
      return vertexToCoordinate.get(vertex);
   }

   /**
    * Process the next event in the event queue
    */
   private void processEventQueue(Queue<SweepEvent> eventQueue, BeachLineTree<V> beachLineTree, PlanarGraph<V, E> graph, VertexFactory<V> vertexFactory) {
      SweepEvent event = eventQueue.poll();
      if (event instanceof SiteEvent) {
         handleSiteEvent((SiteEvent) event, eventQueue, beachLineTree);
      } else {
         handleCircleEvent((CircleEvent) event, eventQueue, beachLineTree, graph, vertexFactory);
      }
   }

   /**
    * Finalisation of the fortune algorithm to be called once all events are
    * processed
    */
   void postProcessingEventQueue(BeachLineTree<V> beachLineTree, List<V> boundaryVertices, PlanarGraph<V, E> graph, VertexFactory<V> vertexFactory) {
      // The breakpoints still present in the beachLineTree represent half-infinite edges in the Voronoi diagram
      // Connect these to the boundary, if specified
      if (boundary != null) {
         for (BeachLineTree.BreakPoint<V> brk : beachLineTree.getInorderBreaks()) {
            V sourceVertex = brk.sourceVertex;
            V beforeVertex = brk.prevVertex;
            Coordinate sourceSite = vertexToCoordinate.get(sourceVertex);

            if (sourceVertex == null) {
               if (brk.twin != null) {
                  // The midline between two sites, terminated at both ends by the boundary
                  Coordinate midSite = new Coordinate((brk.leftSite.x + brk.rightSite.x) / 2.,
                          (brk.leftSite.y + brk.rightSite.y) / 2.);

                  // Create a vertex at each boundary interception
                  V firstBoundaryVertex = getVertexAtBoundaryInterception(midSite,
                          new Coordinate(brk.rightSite.y - brk.leftSite.y,
                          brk.leftSite.x - brk.rightSite.x),
                          boundaryVertices,
                          vertexFactory);

                  V secondBoundaryVertex = getVertexAtBoundaryInterception(midSite,
                          new Coordinate(brk.leftSite.y - brk.rightSite.y,
                          brk.rightSite.x - brk.leftSite.x),
                          boundaryVertices,
                          vertexFactory);

                  // Create the edge 
                  E edge = graph.getEdgeFactory().createEdge(firstBoundaryVertex, secondBoundaryVertex);
                  graph.addEdge(firstBoundaryVertex, secondBoundaryVertex, null, null, edge);

                  // Prevent the twin from causing a duplicate edge
                  brk.twin.twin = null;
                  brk.twin = null;
               }
            } else {
               // TODO: Remove code duplication with Delaunay Voronoi Visitor                                    
               V boundaryVertex = getVertexAtBoundaryInterception(sourceSite,
                       new Coordinate(brk.rightSite.y - brk.leftSite.y,
                       brk.leftSite.x - brk.rightSite.x),
                       boundaryVertices,
                       vertexFactory);
               if (boundaryVertex != null) {
                  // Create the new edge to this boundary intercept
                  E edge = graph.getEdgeFactory().createEdge(sourceVertex, boundaryVertex);
                  graph.addEdge(sourceVertex, boundaryVertex, beforeVertex, null, edge);
               }
            }
         }

         Collections.reverse(boundaryVertices);
         for (int prevItr = 0; prevItr < boundaryVertices.size(); ++prevItr) {
            int itr = (prevItr + 1) % boundaryVertices.size();
            int nextItr = (prevItr + 2) % boundaryVertices.size();
            V boundaryPrevious = boundaryVertices.get(prevItr);
            V boundarySource = boundaryVertices.get(itr);
            V boundaryTarget = boundaryVertices.get(nextItr);
            V boundaryBefore = null;

            if (graph.containsEdge(boundarySource, boundaryPrevious)) {
               boundaryBefore = graph.getPrevVertex(boundarySource, boundaryPrevious);
            } else if (PlanarGraphs.isVertexBoundary(graph, boundarySource)) {
               boundaryBefore = PlanarGraphs.getPrevVertexOnBoundary(graph, boundarySource);
            }

            E edge = graph.getEdgeFactory().createEdge(boundarySource, boundaryTarget);
            graph.addEdge(boundarySource, boundaryTarget, boundaryBefore, null, edge);
         }
      }
   }

   private V getVertexAtBoundaryInterception(Coordinate origin, Coordinate direction, List<V> boundaryVertices, VertexFactory<V> vertexFactory) {
      LineSegment perpendicularBisector = new LineSegment(
              origin,
              new Coordinate(origin.x - direction.x,
              origin.y - direction.y));

      // Check each segment in the boundary for intersection with the
      // bisector            
      // TODO: O(boundary size), can we do this more efficiently?
      double minDistance = Double.POSITIVE_INFINITY;
      int segmentIndex = -1;
      for (int itr = 0; itr < boundaryVertices.size(); ++itr) {
         LineSegment boundarySegment = getLinearRingSegment(itr, boundaryVertices, this);
         Coordinate intersection = getHalfLineIntersection(perpendicularBisector, boundarySegment);
         if (intersection != null) {
            // Find the nearest boundary intersection (allows a concave boundary)
            double distance = origin.distance(intersection);
            if (distance < minDistance) {
               minDistance = distance;
               segmentIndex = itr;
            }
         }
      }

      V boundaryVertex = null;
      if (segmentIndex >= 0) {
         int nextItr = (segmentIndex + 1) % boundaryVertices.size();
         LineSegment boundarySegment = getLinearRingSegment(segmentIndex, boundaryVertices, this);
         Coordinate intersection = getHalfLineIntersection(perpendicularBisector, boundarySegment);

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

   private void handleSiteEvent(SiteEvent siteEvent, Queue<SweepEvent> eventQueue, BeachLineTree<V> beachLineTree) {
      Coordinate site = siteEvent.site;

      if (beachLineTree.head.left == null) {
         BeachLineTree.Arc<V> firstArc = new BeachLineTree.Arc<V>(site);
         beachLineTree.head.left = firstArc;
         beachLineTree.head.left.parent = beachLineTree.head;
      } else {
         // Search for the arc to the left of this site
         BeachLineTree.Arc<V> intersectArc = findIntersectionArc(site, beachLineTree.head.left);

         // If this arc has a circle event, it is a false alarm, mark it as invalid              
         // by setting it's owning arc to null. This avoids the O(n) time required to find
         // it and delete it in the queue. When it is eventually encountered, it will be discarded.
         if (intersectArc.circleEvent != null) {
            intersectArc.circleEvent.arc = null;
         }

         // Get the arcs left and right, these are required for circle event checking
         BeachLineTree.Arc<V> leftArc = beachLineTree.getNextLeftArc(intersectArc);
         BeachLineTree.Arc<V> rightArc = beachLineTree.getNextRightArc(intersectArc);
         BeachLineTree.Arc<V> newArc = new BeachLineTree.Arc<V>(site);
         BeachLineTree.Arc<V> leftIntersectArc = new BeachLineTree.Arc<V>(intersectArc.site);
         BeachLineTree.Arc<V> rightIntersectArc = new BeachLineTree.Arc<V>(intersectArc.site);

         // Replace the intersected arc with the three new arcs and two break points
         BeachLineTree.BreakPoint<V> rightBreak = new BeachLineTree.BreakPoint<V>(
                 newArc,
                 rightIntersectArc,
                 site, intersectArc.site);

         BeachLineTree.BreakPoint<V> leftBreak =
                 new BeachLineTree.BreakPoint<V>(
                 leftIntersectArc,
                 rightBreak, intersectArc.site, site);
         rightBreak.twin = leftBreak;
         leftBreak.twin = rightBreak;

         //rightBreak.next = leftBreak;
         //leftBreak.next = rightBreak;

         BeachLineTree.replace(intersectArc, leftBreak);

         // Check the two triples of consecutive arcs for converging break lines
         checkForCircleEvent(leftArc, leftIntersectArc, newArc, eventQueue);
         checkForCircleEvent(newArc, rightIntersectArc, rightArc, eventQueue);
      }
   }

   private void handleCircleEvent(CircleEvent circleEvent, Queue<SweepEvent> eventQueue, BeachLineTree<V> beachLineTree, PlanarGraph<V, E> graph, VertexFactory<V> vertexFactory) {

      // The circle event may have been marked as a false alarm
      if (circleEvent.arc != null) {
         // Remove the circle events for the two neightbouring arcs
         BeachLineTree.Arc<V> leftArc = beachLineTree.getNextLeftArc(circleEvent.arc);
         BeachLineTree.Arc<V> rightArc = beachLineTree.getNextRightArc(circleEvent.arc);
         BeachLineTree.BreakPoint<V> leftBreak = beachLineTree.getLeftBreak(circleEvent.arc);
         BeachLineTree.BreakPoint<V> rightBreak = beachLineTree.getRightBreak(circleEvent.arc);
         for (BeachLineTree.Arc arc : new BeachLineTree.Arc[]{leftArc, rightArc}) {
            if (arc.circleEvent != null) {
               arc.circleEvent.arc = null;
               arc.circleEvent = null;
            }
         }

         // Add the center of the circle as a new vertex
         V circleEventVertex = vertexFactory.createVertex();
         vertexToCoordinate.put(circleEventVertex, circleEvent.vertexLocation);
         graph.addVertex(circleEventVertex);

         // Add edges where possible
         if (leftBreak.sourceVertex != null) {
            graph.addEdge(leftBreak.sourceVertex, circleEventVertex, leftBreak.prevVertex, null);
         }

         if (rightBreak.sourceVertex != null) {
            graph.addEdge(rightBreak.sourceVertex, circleEventVertex, rightBreak.prevVertex, leftBreak == null ? null : leftBreak.sourceVertex);
         }

         V leftBreakSourceVertex = leftBreak.sourceVertex;
         BeachLineTree.BreakPoint<V> leftBreakTwin = leftBreak.twin;
         if (leftBreak.twin != null) {
            leftBreak.twin.sourceVertex = circleEventVertex;
            leftBreak.twin.prevVertex = rightBreak.sourceVertex;
            leftBreak.twin.twin = null;
            leftBreak.twin.sourcePrev = rightBreak.twin;
         }

         V rightBreakSourceVertex = rightBreak.sourceVertex;
         BeachLineTree.BreakPoint<V> rightBreakTwin = rightBreak.twin;
         if (rightBreak.twin != null) {
            rightBreak.twin.sourceVertex = circleEventVertex;
            rightBreak.twin.sourcePrev = leftBreak.twin;
            rightBreak.twin.twin = null;
         }
         if (rightBreak.sourcePrev != null && rightBreak.sourcePrev.sourceVertex == rightBreak.sourceVertex) {
            rightBreak.sourcePrev.prevVertex = circleEventVertex;
         }
         if (leftBreak.sourcePrev != null && leftBreak.sourcePrev.sourceVertex == leftBreak.sourceVertex) {
            leftBreak.sourcePrev.prevVertex = circleEventVertex;
         }

         BeachLineTree.delete(circleEvent.arc);

         BeachLineTree.BreakPoint<V> newBreak = beachLineTree.getRightBreak(leftArc);
         newBreak.sourceVertex = circleEventVertex;
         newBreak.prevVertex = leftBreakSourceVertex;
         newBreak.rightSite = rightArc.site;
         newBreak.leftSite = leftArc.site;
         newBreak.twin = null;
         newBreak.sourcePrev = rightBreakTwin;

         if (leftBreakTwin != null) {
            leftBreakTwin.sourcePrev = newBreak;
         }

         // Check the new arc triples for circle events
         BeachLineTree.Arc<V> leftLeftArc = beachLineTree.getNextLeftArc(leftArc);
         BeachLineTree.Arc<V> rightRightArc = beachLineTree.getNextRightArc(rightArc);

         // Check the two triples of consecutive arcs for converging break lines
         checkForCircleEvent(leftLeftArc, leftArc, rightArc, eventQueue);
         checkForCircleEvent(leftArc, rightArc, rightRightArc, eventQueue);
      }
   }

   void checkForCircleEvent(final BeachLineTree.Arc<V> topArc,
           final BeachLineTree.Arc<V> middleArc,
           final BeachLineTree.Arc<V> bottomArc,
           Queue<SweepEvent> eventQueue) {
      if (bottomArc != null && middleArc != null && topArc != null) {
         // Check for intersection of the *half-line* bisectors
         if (Angle.getTurn(Angle.angle(bottomArc.site, middleArc.site),
                 Angle.angle(middleArc.site, topArc.site))
                 == Angle.CLOCKWISE) {
            Coordinate circumcenter = Triangle.circumcentre(bottomArc.site, middleArc.site, topArc.site);

            if (boundary == null || CGAlgorithms.isPointInRing(circumcenter, boundary.getCoordinates())) {
               CircleEvent<V> event = new CircleEvent<V>(middleArc, circumcenter,
                       new Coordinate(circumcenter.x + Math.sqrt(Math.pow(bottomArc.site.x - circumcenter.x, 2) + Math.pow(bottomArc.site.y - circumcenter.y, 2)), circumcenter.y));

               middleArc.circleEvent = event;
               eventQueue.add(event);
            }
         }
      }
   }

   // Find the arc that intersects the line y = x0 with the directrix x
   // O(log n)
   BeachLineTree.Arc<V> findIntersectionArc(final Coordinate site, final BeachLineTree.Node<V> start) {
      BeachLineTree.Node<V> node = start;
      while (node instanceof BeachLineTree.BreakPoint) {
         BeachLineTree.BreakPoint<V> branch = (BeachLineTree.BreakPoint<V>) (node);

         double x0 = site.x;
         double directixY = site.y;
         double p1 = (x0 - branch.leftSite.x) / 2.;
         double p2 = (x0 - branch.rightSite.x) / 2.;
         double h1 = branch.leftSite.y;
         double h2 = branch.rightSite.y;
         double k1 = branch.leftSite.x + p1;
         double k2 = branch.rightSite.x + p2;

         // Intersection of two parabola is a quadratic equation
         double a = p2 - p1;
         double b = 2. * ((p1 * h2) - (p2 * h1));
         double c = (p2 * h1 * h1) - (p1 * h2 * h2) + 4. * p1 * p2 * (k2 - k1);
         double discriminant = (b * b) - (4 * a * c);

         // Use the first root, the tuples are ordered appropriately to ensure this is the one we want.
         double r1 = (-b - Math.sqrt(discriminant)) / (2 * a);

         PrecisionModel precisionModel = geometryFactory.getPrecisionModel();
         if (precisionModel.makePrecise(r1)
                 > precisionModel.makePrecise(directixY)) {
            node = branch.right;
         } else {
            node = branch.left;
         }
      }

      return (BeachLineTree.Arc<V>) node;
   }

   // TODO: Remove code duplication with Delaunay Voronoi Visitor
   private static <V> LineSegment getLinearRingSegment(int segmentIndex, List<V> vertices, PlanarLayout<V> layout) {
      int nextItr = (segmentIndex + 1) % vertices.size();
      V boundarySource = vertices.get(segmentIndex);
      V boundaryTarget = vertices.get(nextItr);
      return new LineSegment(
              layout.getCoordinate(boundarySource),
              layout.getCoordinate(boundaryTarget));
   }

   // TODO: Remove code duplication with Delaunay Voronoi Visitor
   private static Coordinate getHalfLineIntersection(LineSegment halfLine, LineSegment segment) {

      // The half line can be extended forwards (factor must be positive)
      double factor = Math.max(0, Math.max(halfLine.projectionFactor(segment.p0),
              halfLine.projectionFactor(segment.p1)));

      LineSegment extendedBisector =
              new LineSegment(halfLine.p0, halfLine.pointAlong(factor));

      // Check for intersection with this boundary segment
      Coordinate boundaryIntersection = extendedBisector.intersection(segment);
      return boundaryIntersection;
   }

   private static <V> void printBeachLineTree(BeachLineTree.Node<V> node, PrintStream out) {
      // In order traversal 
      if (node == null) {
         return;
      }
      if (node instanceof BeachLineTree.BreakPoint) {
         BeachLineTree.BreakPoint<V> brk = (BeachLineTree.BreakPoint<V>) node;
         printBeachLineTree(brk.left, out);
         out.println("BREAK:" + brk);
         printBeachLineTree(brk.right, out);
      } else {
         BeachLineTree.Arc<V> arc = (BeachLineTree.Arc<V>) node;
         out.println("ARC:" + arc);
      }
   }
}
