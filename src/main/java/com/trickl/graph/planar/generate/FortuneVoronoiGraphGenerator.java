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

   static abstract class Node<V> {
      // Doubly-linked for dual-direction traversal

      BreakPoint<V> parent;
   };

   // The branch nodes represent breakpoints between the parabolic arcs
   static class BreakPoint<V> extends Node<V> {

      static int maxid;
      int id;
      Node<V> right;
      Node<V> left;
      // Twin the the break in the opposite direction caused by the same two sites
      BreakPoint<V> twin;
      // The next break which is clockwise from the same source
      BreakPoint<V> sourcePrev;
      V prevVertex;
      V sourceVertex;

      // TODO: Not necessary, can derive from tree
      //Coordinate rightSite;
      //Coordinate leftSite;
      BreakPoint(Node<V> left, Node<V> right) {
         this.left = left;
         this.right = right;

         if (right != null) {
            right.parent = this;
         }
         if (left != null) {
            left.parent = this;
         }

         this.id = maxid++;
      }

      @Override
      public String toString() {
         return //"L: " + getLeftArc(this).site
                 //+ ",R: " + getRightArc(this).site
                 //+  "LP: " + (sourcePrev == null ? "*" : sourcePrev.leftSite)
                 //+ ",RP: " + (sourcePrev == null ? "*" : sourcePrev.rightSite)
                 "Id:" + id
                 + ",Prev: " + prevVertex
                 + ",Source: " + sourceVertex;
      }
   };

   // The leaf nodes represent arcs on the beach
   static class Arc<V> extends Node<V> {

      Coordinate site;
      CircleEvent<V> circleEvent;
      BreakPoint<V> leftBreak;
      BreakPoint<V> rightBreak;

      Arc(Coordinate site) {
         this.site = site;
      }

      @Override
      public String toString() {
         return "Site: " + site
                 + ",LB=" + (leftBreak != null ? leftBreak.id : "Null")
                 + ",RB=" + (rightBreak != null ? rightBreak.id : "Null")
                 + ((circleEvent != null && circleEvent.arc != null)
                 ? (",CircleEvent: " + circleEvent.vertexLocation) : "");

      }
   };

   // The binary tree status class for storing the beach line         
   static class BeachLineTree<V> {

      // Head node
      BreakPoint<V> head;

      BeachLineTree() {
         head = new BreakPoint<V>(null, null);
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

      Arc<V> getLeftArc(final Node<V> start) {
         Node<V> node = start;
         if (node instanceof Arc) {
            if (node.parent.right == node) {
               node = node.parent;
            } else {
               while (node.parent != head && node.parent.left == node) {
                  node = node.parent;
               }
               node = node.parent;
            }
         }

         if (node == head) {
            node = null;
         } else {
            node = ((BreakPoint<V>) node).left;
         }

         while (node instanceof BreakPoint) {
            BreakPoint<V> brk = (BreakPoint<V>) node;
            node = brk.right;
         }

         return (Arc<V>) node;
      }

      Arc<V> getRightArc(final Node<V> start) {
         Node<V> node = start;
         if (node instanceof Arc) {
            if (node.parent.left == node) {
               node = node.parent;
            } else {
               while (node.parent != head && node.parent.right == node) {
                  node = node.parent;
               }
               node = node.parent;
            }
         }

         if (node == head) {
            node = null;
         } else {
            node = ((BreakPoint<V>) node).right;
         }

         while (node instanceof BreakPoint) {
            BreakPoint<V> brk = (BreakPoint<V>) node;
            node = brk.left;
         }

         return (Arc<V>) node;
      }

      BreakPoint<V> getLeftBreak(final Node<V> start) {
         Node<V> node = start;
         while (node.parent != head && node.parent.left == node) {
            node = node.parent;
         }
         node = node.parent;

         if (node == head) {
            node = null;
         }

         return (BreakPoint<V>) node;
      }

      BreakPoint<V> getRightBreak(final Node<V> start) {
         Node<V> node = start;
         while (node.parent != head && node.parent.right == node) {
            node = node.parent;
         }

         node = node.parent;

         if (node == head) {
            node = null;
         }

         return (BreakPoint<V>) node;
      }

      List<BreakPoint<V>> getInorderBreaks() {
         List<BreakPoint<V>> breaks = new LinkedList<BreakPoint<V>>();

         // Get an inorder traversal of the BeachLineTree
         Stack<BreakPoint<V>> breakStack = new Stack<BreakPoint<V>>();
         Node<V> current = head.left;
         boolean done = false;
         while (!done) {
            if (current instanceof BreakPoint) {
               BreakPoint<V> brk = (BreakPoint<V>) current;
               breakStack.push(brk);
               current = brk.left;
            } else {
               if (!breakStack.isEmpty()) {
                  BreakPoint<V> brk = breakStack.pop();
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
      Arc<V> arc;
      Coordinate vertexLocation;
      Coordinate eventLocation;

      CircleEvent(Arc<V> node, Coordinate vertexLocation, Coordinate eventLocation) {
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
   
   public FortuneVoronoiGraphGenerator(Set<V> vertices, PlanarLayout<V> layout, LinearRing boundary) {      
      sites = new ArrayList<>(vertices.size());
      vertices.stream().forEach((vertex) -> {
          sites.add(layout.getCoordinate(vertex));
       });
      this.boundary = boundary;
   }

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
          if (CGAlgorithms.signedArea(boundary.getCoordinates()) < 0)
            {
               throw new IllegalArgumentException("Boundary must be defined clockwise");
            } 
          
         // Note that in a linear ring, end element equals start element
         for (int i = 0; i < boundary.getCoordinates().length - 1; ++i) {
            Coordinate coord = boundary.getCoordinateN(i);
            V boundaryVertex = vertexFactory.createVertex();
            boundaryVertices.add(boundaryVertex);
            vertexToCoordinate.put(boundaryVertex, coord);
            graph.addVertex(boundaryVertex);
         }
      }

      // Initialize with an empty beach tree line and just site events
      BeachLineTree<V> beachLineTree = new BeachLineTree<V>();
      if (!sites.isEmpty()) {
         Queue<SweepEvent> eventQueue = new PriorityQueue<SweepEvent>(sites.size() * 2, new SweepEventComparator(geometryFactory.getPrecisionModel()));
         for (Coordinate site : sites) {
            eventQueue.add(new SiteEvent(site));
         }

         while (!eventQueue.isEmpty()) {
            //printBeachLineTree(beachLineTree.head.left, System.out);
            //System.out.println("-----");
            processEventQueue(eventQueue, beachLineTree, graph, vertexFactory);
         }
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
   private void postProcessingEventQueue(BeachLineTree<V> beachLineTree, List<V> boundaryVertices, PlanarGraph<V, E> graph, VertexFactory<V> vertexFactory) {
      // The breakpoints still present in the beachLineTree represent half-infinite edges in the Voronoi diagram
      // Connect these to the boundary, if specified      
      //printBeachLineTree(beachLineTree.head.left, System.out);
      if (boundary != null) {
         for (BreakPoint<V> brk : beachLineTree.getInorderBreaks()) {
            V sourceVertex = brk.sourceVertex;
            V beforeVertex = brk.prevVertex;
            Coordinate sourceSite = vertexToCoordinate.get(sourceVertex);

            Arc<V> leftArc = beachLineTree.getLeftArc(brk);
            Arc<V> rightArc = beachLineTree.getRightArc(brk);

            if (sourceVertex == null) {
               if (brk.twin != null) {
                  // The midline between two sites, terminated at both ends by the boundary
                  Coordinate midSite = new Coordinate((leftArc.site.x + rightArc.site.x) / 2.,
                          (leftArc.site.y + rightArc.site.y) / 2.);

                  // Create a vertex at each boundary interception
                  V firstBoundaryVertex = createVertexAtBoundaryInterception(new LineSegment(midSite,
                          new Coordinate(midSite.x + rightArc.site.y - leftArc.site.y,
                          midSite.y + leftArc.site.x - rightArc.site.x)),
                          boundaryVertices,
                          vertexFactory,
                          this);

                  V secondBoundaryVertex = createVertexAtBoundaryInterception(new LineSegment(midSite,
                          new Coordinate(midSite.x + leftArc.site.y - rightArc.site.y,
                          midSite.y + rightArc.site.x - leftArc.site.x)),
                          boundaryVertices,
                          vertexFactory,
                          this);

                  // Create the edge 
                  E edge = graph.getEdgeFactory().createEdge(firstBoundaryVertex, secondBoundaryVertex);
                  graph.addEdge(firstBoundaryVertex, secondBoundaryVertex, null, null, edge);

                  // Prevent the twin from causing a duplicate edge
                  brk.twin.twin = null;
                  brk.twin = null;
               }
            } else {
               V boundaryVertex = createVertexAtBoundaryInterception(new LineSegment(sourceSite,
                       new Coordinate(sourceSite.x + leftArc.site.y - rightArc.site.y,
                       sourceSite.y + rightArc.site.x - leftArc.site.x)),
                       boundaryVertices,
                       vertexFactory,
                       this);
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

   private void handleSiteEvent(SiteEvent siteEvent, Queue<SweepEvent> eventQueue, BeachLineTree<V> beachLineTree) {
      Coordinate site = siteEvent.site;

      if (beachLineTree.head.left == null) {
         Arc<V> firstArc = new Arc<V>(site);
         beachLineTree.head.left = firstArc;
         beachLineTree.head.left.parent = beachLineTree.head;  
      } else {
         // Search for the arc to the left of this site
         Arc<V> intersectArc = findIntersectionArc(site, beachLineTree.head.left, beachLineTree);

         // If this arc has a circle event, it is a false alarm, mark it as invalid              
         // by setting it's owning arc to null. This avoids the O(n) time required to find
         // it and delete it in the queue. When it is eventually encountered, it will be discarded.
         if (intersectArc.circleEvent != null) {
            intersectArc.circleEvent.arc = null;
         }

         // Get the arcs left and right, these are required for circle event checking
         Arc<V> leftArc = beachLineTree.getLeftArc(intersectArc);
         Arc<V> rightArc = beachLineTree.getRightArc(intersectArc);
         Arc<V> newArc = new Arc<V>(site);
         Arc<V> leftIntersectArc = new Arc<V>(intersectArc.site);
         Arc<V> rightIntersectArc = new Arc<V>(intersectArc.site);

         // Replace the intersected arc with the three new arcs and two break points
         BreakPoint<V> rightBreak = new BreakPoint<V>(
                 newArc,
                 rightIntersectArc);

         BreakPoint<V> leftBreak = new BreakPoint<V>(
                 leftIntersectArc,
                 rightBreak);

         leftIntersectArc.leftBreak = intersectArc.leftBreak;
         leftIntersectArc.rightBreak = leftBreak;
         newArc.leftBreak = leftBreak;
         newArc.rightBreak = rightBreak;
         rightIntersectArc.leftBreak = rightBreak;
         rightIntersectArc.rightBreak = intersectArc.rightBreak;
         rightBreak.twin = leftBreak;
         leftBreak.twin = rightBreak;

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
         Arc<V> leftArc = beachLineTree.getLeftArc(circleEvent.arc);
         Arc<V> rightArc = beachLineTree.getRightArc(circleEvent.arc);
         BreakPoint<V> leftBreak = circleEvent.arc.leftBreak;
         BreakPoint<V> rightBreak = circleEvent.arc.rightBreak;
  
         for (Arc arc : new Arc[]{leftArc, rightArc}) {
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
         BreakPoint<V> leftBreakTwin = leftBreak.twin;
         if (leftBreak.twin != null) {
            leftBreak.twin.sourceVertex = circleEventVertex;
            leftBreak.twin.prevVertex = rightBreak.sourceVertex;
            leftBreak.twin.twin = null;
            leftBreak.twin.sourcePrev = rightBreak.twin;
         }

         BreakPoint<V> rightBreakTwin = rightBreak.twin;
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

         BreakPoint<V> newBreak = beachLineTree.getRightBreak(leftArc);
         //BreakPoint<V> newBreak = leftArc.rightBreak;
         newBreak.sourceVertex = circleEventVertex;
         newBreak.prevVertex = leftBreakSourceVertex;
         //newBreak.rightSite = rightArc.site;
         //newBreak.leftSite = leftArc.site;
         newBreak.twin = null;
         newBreak.sourcePrev = rightBreakTwin;

         if (leftBreakTwin != null) {
            leftBreakTwin.sourcePrev = newBreak;
         }

         leftArc.rightBreak = newBreak;
         rightArc.leftBreak = newBreak;

         // Check the new arc triples for circle events
         Arc<V> leftLeftArc = beachLineTree.getLeftArc(leftArc);
         Arc<V> rightRightArc = beachLineTree.getRightArc(rightArc);

         // Check the two triples of consecutive arcs for converging break lines
         checkForCircleEvent(leftLeftArc, leftArc, rightArc, eventQueue);
         checkForCircleEvent(leftArc, rightArc, rightRightArc, eventQueue);
      }
   }

   private void checkForCircleEvent(final Arc<V> topArc,
           final Arc<V> middleArc,
           final Arc<V> bottomArc,
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
   private Arc<V> findIntersectionArc(final Coordinate site, final Node<V> start, final BeachLineTree<V> beachLineTree) {
      Node<V> node = start;
      while (node instanceof BreakPoint) {
         BreakPoint<V> brk = (BreakPoint<V>) (node);
         Arc<V> leftArc = beachLineTree.getLeftArc(brk);
         Arc<V> rightArc = beachLineTree.getRightArc(brk);

         double x0 = site.x;
         double directixY = site.y;
         double p1 = (x0 - leftArc.site.x) / 2.;
         double p2 = (x0 - rightArc.site.x) / 2.;
         double h1 = leftArc.site.y;
         double h2 = rightArc.site.y;
         double k1 = leftArc.site.x + p1;
         double k2 = rightArc.site.x + p2;

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
            node = brk.right;
         } else {
            node = brk.left;
         }
      }

      return (Arc<V>) node;
   }

   private V createVertexAtBoundaryInterception(LineSegment halfLine, List<V> boundaryVertices, VertexFactory<V> vertexFactory, PlanarLayout<V> layout) {
      V boundaryVertex = null;
      int segmentIndex = PlanarGraphs.getNearestInterceptingLineSegment(halfLine, boundaryVertices, layout);
      if (segmentIndex >= 0) {
         int nextItr = (segmentIndex + 1) % boundaryVertices.size();
         LineSegment boundarySegment = PlanarGraphs.getLineSegment(segmentIndex, boundaryVertices, layout);
         Coordinate intersection = boundarySegment.lineIntersection(halfLine);

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

   // For debug purposes, should delete
   private static <V> void printBeachLineTree(Node<V> node, PrintStream out) {
      // In order traversal 
      if (node == null) {
         return;
      }
      if (node instanceof BreakPoint) {
         BreakPoint<V> brk = (BreakPoint<V>) node;
         printBeachLineTree(brk.left, out);
         out.println("BREAK:" + brk);
         printBeachLineTree(brk.right, out);
      } else {
         Arc<V> arc = (Arc<V>) node;
         out.println("ARC:" + arc);
      }
   }
}
