package com.trickl.graph;

import org.jgrapht.Graph;

public class Volume<V, E> {

   private Graph<V, E> graph;
   private Double volume;

   public Volume(Graph<V, E> graph) {
      this.graph = graph;
   }

   public double getVolume() {
      if (volume == null) {
         volume = 0.;
         for (V vertex : graph.vertexSet()) {
            for (E edge : graph.edgesOf(vertex)) {
               volume += graph.getEdgeWeight(edge);
            }
         }        
      }

      return volume;
   }
}
