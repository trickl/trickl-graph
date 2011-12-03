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
