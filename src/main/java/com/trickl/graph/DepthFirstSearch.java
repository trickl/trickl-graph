package com.trickl.graph;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

import org.jgrapht.Graph;

public class DepthFirstSearch<V, E> {

   private static class Context<V, E> {
      protected V vertex;
      protected Iterator<E> edgeItr;

      public Context(V vertex, Iterator<E> edgeItr) {
         this.vertex = vertex;
         this.edgeItr = edgeItr;
      }
   }

   private enum Color {
      WHITE,
      GRAY,
      BLACK;
   }

   Stack<Context> order = new Stack<Context>();

   private Graph<V, E> graph;

   public DepthFirstSearch(Graph<V, E> graph) {
      this.graph = graph;
   }

   public void traverse(SpanningSearchVisitor<V, E> visitor) {
      Iterator<V> vertexIterator = graph.vertexSet().iterator();
      if (!vertexIterator.hasNext()) return;
      else traverse(vertexIterator.next(), visitor);
   }

   public void traverse(V startVertex,
                        SpanningSearchVisitor<V, E> visitor) {
      // Initialize the color map
      Map<V, Color> colorMap = new Hashtable<V, Color>(graph.vertexSet().size());
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
      order.add(new Context<V, E>(u, graph.edgesOf(u).iterator()));

      while (!order.isEmpty()) {
         Context<V, E> context = order.pop();
         u = context.vertex;
         Iterator<E> edgeItr = context.edgeItr;

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

               order.add(new Context<V, E>(u, edgeItr));                              
               u = v;

               edgeItr = graph.edgesOf(u).iterator();               
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

