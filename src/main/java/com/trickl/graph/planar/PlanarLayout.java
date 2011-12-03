package com.trickl.graph.planar;

import com.vividsolutions.jts.geom.Coordinate;

public interface PlanarLayout<V> {
   Coordinate getCoordinate(V vertex);
}
