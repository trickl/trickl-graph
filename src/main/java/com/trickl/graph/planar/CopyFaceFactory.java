package com.trickl.graph.planar;

import com.trickl.graph.edges.DirectedEdge;

public class CopyFaceFactory<V, E, F> implements FaceFactory<V, F> {
   
   private PlanarFaceGraph<V, E, F> planarFaceGraph;

   public CopyFaceFactory(PlanarFaceGraph<V, E, F> planarFaceGraph) {
      this.planarFaceGraph = planarFaceGraph;
   }

   @Override
   public F createFace(V source, V target, boolean isBoundary) {
      // TODO: If copy graph visitor maintained a map between the original graph
      // vertices and edges and the copied vertices and edges, this map should
      // be used here. Currently, the mechanism only works when the same vertices
      // are passed through -> as the lookup is performed on the copied graph.
      if (source == null && target == null) {
         DirectedEdge<V> boundaryEdge = planarFaceGraph.getBoundary();
         return planarFaceGraph.getFace(boundaryEdge.getSource(), boundaryEdge.getTarget());
      }
      return planarFaceGraph.getFace(source, target);
   }
}
