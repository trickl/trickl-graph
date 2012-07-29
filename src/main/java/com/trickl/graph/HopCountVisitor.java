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
package com.trickl.graph;

import java.util.Map;

public class HopCountVisitor<V, E> extends AbstractSpanningSearchVisitor<V, E> {

   int maxHopCount = 0;
   private Map<V, Integer> vertexHopCounts;

   public HopCountVisitor(Map<V, Integer> vertexHopCounts) {
      if (vertexHopCounts == null) {
         throw new NullPointerException();
      }
      this.vertexHopCounts = vertexHopCounts;
   }

   @Override
   public void startVertex(V u) {
      maxHopCount = 0;
      vertexHopCounts.put(u, 0);
   }

   @Override
   public void examineEdge(V source, V target) {
      Integer targetHopCount = vertexHopCounts.get(target);
      if (targetHopCount == null) {
         maxHopCount = vertexHopCounts.get(source) + 1;
         getVertexHopCounts().put(target, maxHopCount);
      }
   }

   public int getMaxHopCount() {
      return maxHopCount;
   }

   public Map<V, Integer> getVertexHopCounts() {
      return vertexHopCounts;
   }
}
