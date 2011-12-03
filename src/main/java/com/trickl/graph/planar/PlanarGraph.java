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
import org.jgrapht.Graph;

public interface PlanarGraph<V, E> extends Graph<V, E>, PlanarEmbedding<V, E> {
   // Usually the after vertex is not necessary and can be determined, however
   // sometimes it is ambiguous.
   E addEdge(V sourceVertex, V targetVertex, V beforeVertex, V afterVertex);
   boolean addEdge(V sourceVertex, V targetVertex, V beforeVertex, V afterVertex, E e);

   V getNextVertex(V source, V target);
   V getPrevVertex(V source, V target);

   DirectedEdge<V> getBoundary();
   boolean isBoundary(V source, V target);
   void setBoundary(V source, V target);
}
