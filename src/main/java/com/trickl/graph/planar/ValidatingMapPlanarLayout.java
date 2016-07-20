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

import com.trickl.graph.GraphArgumentException;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.jgrapht.Graphs;

/**
 *
 * @author tgee
 * @param <V>
 * @param <E>
 */
public class ValidatingMapPlanarLayout<V, E> extends MapPlanarLayout<V> {

    PlanarGraph<V, E> graph;
    private double tolerance = 1e-5;

    public ValidatingMapPlanarLayout(PlanarGraph<V, E> graph) {
        super();
        this.graph = graph;
    }

    public ValidatingMapPlanarLayout(PlanarGraph<V, E> graph, Map<V, Coordinate> vertexCoordinateMap) {
        super(vertexCoordinateMap);
        this.graph = graph;
    }

    @Override
    public void setCoordinate(V vertex, Coordinate coord) {
        super.setCoordinate(vertex, coord);

        List<LineSegment> existingLines = new LinkedList<>();
        graph.edgeSet().stream().forEach((edge) -> {
            V source = graph.getEdgeSource(edge);
            V target = graph.getEdgeTarget(edge);
            if (!(vertex == source || vertex == target)) {
                Coordinate sourceCoord = getCoordinate(source);
                Coordinate targetCoord = getCoordinate(target);
                if (sourceCoord != null && targetCoord != null) {
                    LineSegment existingLine = new LineSegment(sourceCoord, targetCoord);
                    existingLines.add(existingLine);
                }
            }
        });

        for (E edge : graph.edgesOf(vertex)) {
            V oppositeVertex = Graphs.getOppositeVertex(graph, edge, vertex);
            Coordinate oppositeCoord = getCoordinate(oppositeVertex);
            if (oppositeCoord != null) {
                LineSegment line = new LineSegment(coord, oppositeCoord);
                for (LineSegment existingLine : existingLines) {
                    Coordinate intersection = line.intersection(existingLine);
                    if (intersection != null
                            && !intersection.equals2D(existingLine.p0, tolerance)
                            && !intersection.equals2D(existingLine.p1, tolerance)
                            && !intersection.equals2D(line.p0, tolerance)
                            && !intersection.equals2D(line.p1, tolerance)) {
                            // There is an intersection betweent the lines that is 
                        // not at an endpoint.
                        throw new GraphArgumentException(graph,
                                new IllegalArgumentException("Setting coordinate for vertex " + vertex + " at " + coord + " would make the line to " +
                                        oppositeVertex + " at "+ oppositeCoord + " overlap the existing graph."));
                    }
                }
            }
        }
    }
}
