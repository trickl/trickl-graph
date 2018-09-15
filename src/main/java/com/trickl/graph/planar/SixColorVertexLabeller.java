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

import org.jgrapht.Graphs;

/**
 * A simple six-color planar graph vertex labeller detailed in: TWO LINEAR-TIME
 * ALGORITHMS FOR FIVE-COLORING A PLANAR GRAPH by David Matula, Yossi Shiloach,
 * Robert Tarjan O(n) time complexity
 *
 * @author tgee
 */
public class SixColorVertexLabeller<V, E> extends SixColorAdjacencyLabeller<V, E> {
   
   public SixColorVertexLabeller(PlanarGraph<V, E> graph) {
      super(graph.vertexSet(),
              (vertex) -> graph.edgesOf(vertex),
              (edge, vertex) -> Graphs.getOppositeVertex(graph, edge, vertex));
   }
}