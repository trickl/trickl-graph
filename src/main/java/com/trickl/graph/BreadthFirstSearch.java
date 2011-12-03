package com.trickl.graph;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import org.jgrapht.Graph;

public class BreadthFirstSearch<V, E> {

   private enum Color {
      WHITE,
      GRAY,
      BLACK;
   }

   LinkedList<V> order = new LinkedList<V>();

   private Graph<V, E> graph;

   public BreadthFirstSearch(Graph<V, E> graph) {
      this.graph = graph;
   }

   public void traverse(SpanningSearchVisitor<V, E> visitor) {
      Iterator<V> vertexIterator = graph.vertexSet().iterator();
      if (vertexIterator.hasNext()) {
         traverse(vertexIterator.next(), visitor);
      }      
   }

   public void traverse(V startVertex,
                        SpanningSearchVisitor<V, E> visitor) {
      // Initialize the color map
      Map<V, Color> colorMap = new HashMap<V, Color>(graph.vertexSet().size());
      for (V u : graph.vertexSet()) {
         colorMap.put(u, Color.WHITE);
         visitor.initializeVertex(u);
      }

      visitor.startVertex(startVertex);
      order.clear();
      traverseImpl(startVertex, visitor, colorMap);

      // The graph may be disconnected - search untouched disjoint sets
      for (V u : graph.vertexSet()) {
         Color color = colorMap.get(u);
         if (color == Color.WHITE) {
            visitor.startVertex(u);
            order.clear();
            traverseImpl(u, visitor, colorMap);
         }
      }
   }


   private void traverseImpl(V startVertex,
                             SpanningSearchVisitor<V, E> visitor,
                             Map<V, Color> colorMap) {
      V u = startVertex;

      colorMap.put(u, Color.GRAY);

      visitor.discoverVertex(u);
      order.add(u);

      while (!order.isEmpty()) {
         u = order.poll();
         Iterator<E> edgeItr = graph.edgesOf(u).iterator();

         while (edgeItr.hasNext()) {
            E e = edgeItr.next();
            V v = graph.getEdgeTarget(e).equals(u) ?
                    graph.getEdgeSource(e) :
                    graph.getEdgeTarget(e);

            // Check if a directed edge in the wrong direction
            if (!graph.containsEdge(u, v)) continue;

            visitor.examineEdge(u, v);

            Color color = colorMap.get(v);

            if (color == Color.WHITE) {
               visitor.discoverVertex(v);
               visitor.treeEdge(u, v);

               order.add(v);
               colorMap.put(v, Color.GRAY);
            } else if (color == Color.GRAY) {
               visitor.backEdge(u, v);
            } else {
               visitor.forwardOrCrossEdge(u, v);
            }
         }

         colorMap.put(u, Color.BLACK);
         visitor.finishVertex(u);
      }
   }
}
