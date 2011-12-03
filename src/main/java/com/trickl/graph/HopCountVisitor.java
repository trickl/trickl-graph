package com.trickl.graph;

import java.util.Map;
import org.jgrapht.Graph;

public class HopCountVisitor<V, E> implements SpanningSearchVisitor<V, E> {

   int maxHopCount = 0;
   private Map<V, Integer> vertexHopCounts;

   public HopCountVisitor(Map<V, Integer> vertexHopCounts) {
      if (vertexHopCounts == null) {
         throw new NullPointerException();
      }
      this.vertexHopCounts = vertexHopCounts;
   }

   @Override
   public void initializeVertex(V u) {
   }

   @Override
   public void startVertex(V u) {
      maxHopCount = 0;
      vertexHopCounts.put(u, 0);
   }

   @Override
   public void discoverVertex(V u) {
   }

   @Override
   public void examineEdge(V source, V target) {
      Integer targetHopCount = vertexHopCounts.get(target);
      if (targetHopCount == null) {
         maxHopCount = vertexHopCounts.get(source) + 1;
         getVertexHopCounts().put(target, maxHopCount);
      }
   }

   @Override
   public void treeEdge(V source, V target) {
   }

   @Override
   public void backEdge(V source, V target) {
   }

   @Override
   public void forwardOrCrossEdge(V source, V target) {
   }

   @Override
   public void finishVertex(V u) {
   }

   public int getMaxHopCount() {
      return maxHopCount;
   }

   public Map<V, Integer> getVertexHopCounts() {
      return vertexHopCounts;
   }
}
