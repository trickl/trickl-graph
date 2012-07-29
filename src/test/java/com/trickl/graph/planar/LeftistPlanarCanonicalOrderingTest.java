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

import com.trickl.graph.edges.IntegerEdgeFactory;
import com.trickl.graph.vertices.IntegerVertexFactory;
import java.util.List;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;

public class LeftistPlanarCanonicalOrderingTest {

   public LeftistPlanarCanonicalOrderingTest() {
   }

   @BeforeClass
   public static void setUpClass() throws Exception {
   }

   @AfterClass
   public static void tearDownClass() throws Exception {
   }

   @Test
   public void testPaperExample() {
      IntegerVertexFactory vertexFactory = new IntegerVertexFactory();
      PlanarGraph<Integer,Integer> graph = new DoublyConnectedEdgeList<Integer,Integer, Object>(new IntegerEdgeFactory(), Object.class);

      for (int i = 0; i < 16; ++i) {
         graph.addVertex(vertexFactory.createVertex());
      }

      // From Figure 1b) in the paper.
      graph.addEdge(1, 2);
      graph.addEdge(2, 5, 1, null);
      graph.addEdge(5, 4, 2, null);
      graph.addEdge(4, 3, 5, null);
      graph.addEdge(3, 1, 4, null);
      graph.addEdge(3, 6, 1, null);
      graph.addEdge(6, 1, 3, null);
      graph.addEdge(2, 13, 5, null);
      graph.addEdge(13, 12, 2, null);
      graph.addEdge(12, 5, 13, null);
      graph.addEdge(13, 11, 12, null);
      graph.addEdge(11, 12, 13, null);
      graph.addEdge(11, 9, 12, null);
      graph.addEdge(9, 8, 11, null);
      graph.addEdge(8, 5, 9, null);
      graph.addEdge(8, 7, 5, null);
      graph.addEdge(7, 4, 8, null);
      graph.addEdge(9, 7, 8, null);
      graph.addEdge(2, 14, 13, null);
      graph.addEdge(14, 13, 2, null);
      graph.addEdge(14, 10, 13, null);
      graph.addEdge(10, 9, 14, null);
      graph.addEdge(10, 7, 9, null);
      graph.addEdge(14, 15, 10, null);
      graph.addEdge(15, 10, 14, null);
      graph.addEdge(15, 6, 10, null);
      graph.addEdge(15, 1, 6, null);

      PlanarCanonicalOrdering<Integer,Integer> planarCanonicalOrder = new LeftistPlanarCanonicalOrdering<Integer,Integer>();

      List<Integer> ordering = planarCanonicalOrder.getOrder(graph, 1);

      assertList(ordering, "1,2,3,4,5,6,7,8,9,10,11,12,13,14,15");
   }

   static private void assertList(List<Integer> list, String str) {
      StringBuilder idString = new StringBuilder();
      for (int i = 0; i < list.size(); ++i) {
         if (i > 0) {
            idString.append(',');
         }
         idString.append(list.get(i).toString());
      }
      assertEquals(str, idString.toString());
   }
}
