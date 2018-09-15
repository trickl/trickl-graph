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

import com.vividsolutions.jts.geom.Coordinate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author tgee
 * @param <V>
 */
public class MapPlanarLayout<V> implements PlanarLayoutStore<V> {
   
   final private Map<V, Coordinate> vertexCoordinateMap;
   
   public MapPlanarLayout() {
      this(new HashMap<>());
   }
   
   public MapPlanarLayout(Map<V, Coordinate> vertexCoordinateMap) {
      this.vertexCoordinateMap = vertexCoordinateMap;
   }
   
   public MapPlanarLayout(Set<V> items, PlanarLayout<V> layout) {
      this();
      for (V item: items) {
          vertexCoordinateMap.put(item, layout.getCoordinate(item));
      }      
   }

   @Override
   public Coordinate getCoordinate(V vertex) {
      return vertexCoordinateMap.get(vertex);
   }   

    @Override
    public void setCoordinate(V vertex, Coordinate coord) {
        vertexCoordinateMap.put(vertex, coord);
    }
}
