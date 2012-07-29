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

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import org.jgrapht.Graphs;
import static org.junit.Assert.assertEquals;

public class PlanarAssert {
   static public <V, E> void assertEmbeddingEquals(PlanarGraph<V, E> graph,
                                       V vertex,
                                       String ids) {
      String[] idArray = ids.split(",");

      LinkedList<String> embeddingIds = new LinkedList<String>();
      for (E edge : graph.edgesOf(vertex))
      {         
         V other = Graphs.getOppositeVertex(graph, edge, vertex);
         embeddingIds.add(other.toString());
      }
      
      if (idArray.length > 0)
      {
         int firstIndex = embeddingIds.indexOf(idArray[0]);
         if (firstIndex < 0) {
            assertEquals(Arrays.deepToString(idArray), Arrays.deepToString(embeddingIds.toArray()));
         }
         else {
            Collections.rotate(embeddingIds, -firstIndex);
            assertEquals(Arrays.deepToString(idArray), Arrays.deepToString(embeddingIds.toArray()));
         }
      }
      else {
         assertEquals(Arrays.deepToString(idArray), Arrays.deepToString(embeddingIds.toArray()));
      }
   }
}
