package com.trickl.graph.planar;

import com.vividsolutions.jts.geom.Coordinate;
import java.util.List;

public final class PlanarLayouts {

   private PlanarLayouts() {
   }

   static public <V, E> double  getMeanSeparation(PlanarGraph<V, E> planarGraph, PlanarLayout<V> planarLayout, V vertex) {
      List<V> neighbours = PlanarGraphs.getConnectedVertices(planarGraph, vertex);
      double distanceSum = 0;
      Coordinate local = planarLayout.getCoordinate(vertex);
      if (local != null) {
         for (V neighbour : neighbours) {
            Coordinate neighbourLocation = planarLayout.getCoordinate(neighbour);
            if (neighbourLocation != null) {
               distanceSum += local.distance(neighbourLocation);
            }
         }
      }
      return neighbours.isEmpty() ? 0 : distanceSum / neighbours.size();
   }
}