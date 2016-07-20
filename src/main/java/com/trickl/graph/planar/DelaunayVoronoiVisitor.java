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

import com.vividsolutions.jts.algorithm.Angle;
import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.algorithm.ConvexHull;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequenceFactory;
import java.util.*;
import org.jgrapht.VertexFactory;

public class DelaunayVoronoiVisitor<V1, E1, V2, E2> extends DualGraphVisitor<V1, E1, V2, E2> {

    final private PlanarLayout<V1> inputLayout;
    final private PlanarLayoutStore<V2> outputLayout;
    final private LinearRing boundary;
    final private List<V2> boundaryVertices;
    final private GeometryFactory geometryFactory;

    /**
     *
     * @param inputGraph
     * @param inputLayout
     * @param dualGraph
     * @param outputLayout
     * @param boundary Outward facing boundary
     * @param vertexFactory
     */
    public DelaunayVoronoiVisitor(PlanarGraph<V1, E1> inputGraph,
            PlanarLayout<V1> inputLayout,
            PlanarGraph<V2, E2> dualGraph,
            PlanarLayoutStore<V2> outputLayout,
            LinearRing boundary,
            VertexFactory<V2> vertexFactory) {
        super(inputGraph, dualGraph, vertexFactory);
        
        this.geometryFactory = new GeometryFactory();
        
        if (inputLayout == null) {
            throw new NullPointerException();
        }
  
        // Validate the boundary is defined clockwise
        if (boundary != null) {
            if (CGAlgorithms.signedArea(boundary.getCoordinates()) < 0)
            {
               throw new IllegalArgumentException("Boundary must be defined clockwise");
            }            
        }

        // Validate that dual graph boundary is convex
        if (!PlanarGraphs.isBoundaryConvex(inputGraph, inputLayout)) {
            throw new IllegalArgumentException("Delaunay graphs must have a convex boundary.");
        }

        this.inputLayout = inputLayout;
        this.outputLayout = outputLayout;
        this.boundary = boundary;
        this.boundaryVertices = new ArrayList<V2>(boundary == null ? 0
                : boundary.getCoordinates().length);

        if (boundary != null) {
            // Note that in a linear ring, end element equals start element
            for (int i = 0; i < boundary.getCoordinates().length - 1; ++i) {
                Coordinate coord = boundary.getCoordinateN(i);
                V2 boundaryVertex = vertexFactory.createVertex();
                boundaryVertices.add(boundaryVertex);
                this.outputLayout.setCoordinate(boundaryVertex, coord);
                this.dualGraph.addVertex(boundaryVertex);
            }
        }
    }

    @Override
    public void beginFace(V1 source, V1 target) {
        Coordinate dualTargetLocation = getDualLocation(source, target);
        
        if (dualTargetLocation != null && 
            CGAlgorithms.isPointInRing(dualTargetLocation, boundary.getCoordinates())) {
            super.beginFace(source, target);
            outputLayout.setCoordinate(dualTarget, dualTargetLocation);
        }
    }

    @Override
    public void nextEdge(V1 source, V1 target) {

        if (dualTarget == null) {
            Coordinate dualTargetLocation = getDualLocation(source, target);            
            Coordinate dualSourceLocation = getDualLocation(target, source);
            if (dualSourceLocation != null) {
                if (dualTargetLocation == null &&
                    // There's no dual target as this is the boundary of the graph
                    CGAlgorithms.isPointInRing(dualSourceLocation, boundary.getCoordinates())) {                    
                    createEdgeToBoundary(source, target);
                }
                else if (dualTargetLocation != null) {
                    // The dual target exists, but is outside the boundary
                    LineSegment line = new LineSegment(dualSourceLocation, dualTargetLocation);
                    if (boundary.intersects(line.toGeometry(geometryFactory))) {
                        createEdgeToBoundary(source, target);
                    }
                }                
            }
        } else {
            super.nextEdge(source, target);
        }
    }

    /**
     * Get the location of the Voronoi vertex given a delaunay edge
     *
     * @param delaunaySource
     * @param delaunayTarget
     * @return If the corresponding vertex is inside the boundary, returns the
     * coordinates of the vertex, otherwise returns null.
     */
    private Coordinate getDualLocation(V1 delaunaySource, V1 delaunayTarget) {
        if (inputGraph.isBoundary(delaunaySource, delaunayTarget)) {
            return null;
        }

        Coordinate a = inputLayout.getCoordinate(delaunaySource);
        Coordinate b = inputLayout.getCoordinate(delaunayTarget);
        Coordinate c = inputLayout.getCoordinate(inputGraph.getNextVertex(delaunaySource, delaunayTarget));

        // Inner faces should be defined counterclockwise by convention
        Coordinate circumcentre = Triangle.circumcentre(a, b, c);

        return circumcentre;
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

    private void createEdgeToBoundary(V1 source, V1 target) {
        LineSegment halfLine = getHalfLineToBoundary(source, target);
        int segmentIndex = PlanarGraphs.getNearestInterceptingLineSegment(halfLine, boundaryVertices, outputLayout);
        LineSegment boundarySegment = PlanarGraphs.getLineSegment(segmentIndex, boundaryVertices, outputLayout);
        Coordinate dualTargetLocation = boundarySegment.lineIntersection(halfLine);

        super.beginFace(source, target);
        boundaryVertices.add((segmentIndex + 1) % boundaryVertices.size(), dualTarget);
        outputLayout.setCoordinate(dualTarget, dualTargetLocation);
        super.nextEdge(source, target);
        super.endFace(source, target);
    }

    private LineSegment getHalfLineToBoundary(V1 delaunaySource, V1 delaunayTarget) {
        Coordinate dualSourceLocation = getDualLocation(delaunayTarget, delaunaySource);
        Coordinate a = inputLayout.getCoordinate(delaunaySource);
        Coordinate b = inputLayout.getCoordinate(delaunayTarget);
        LineSegment ab = new LineSegment(a, b);
        Coordinate midPoint = ab.midPoint();
        Coordinate reflectedDualSourceLocation =
                new Coordinate(2 * midPoint.x - dualSourceLocation.x, 
                               2 * midPoint.y - dualSourceLocation.y);
        
        return new LineSegment(midPoint, reflectedDualSourceLocation);
    }

    public static <V> List<V> getConvexBoundaryVertices(List<V> vertices, PlanarLayout<V> planarLayout) {
        Map<Coordinate, V> coordinateToVertex = new HashMap<>();
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

        List<V> boundaryVertices = new LinkedList<>();

        for (int i = 0; i < (boundary.length - 1); ++i) {
            boundaryVertices.add(coordinateToVertex.get(boundary[i]));
        }

        return boundaryVertices;
    }
}
