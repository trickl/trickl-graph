// Distributed under the Boost Software License, Version 1.0.
//    (See accompanying file LICENSE_1_0.txt or copy at
//          http://www.boost.org/LICENSE_1_0.txt)
// The algorithm is based on:
// Robert E. Tarjan
// Depth first search and linear graph algorithms.
// SIAM Journal on Computing, 1(2):146-160, 1972
// Also, see:
// http://www.seas.gwu.edu/~ayoussef/cs212/graphsearch.html#biconnectivity
package com.trickl.graph;

import java.util.*;
import org.jgrapht.EdgeFactory;
import org.jgrapht.Graph;

public class Biconnectivity<V, E> {

   private static class BiconnectedComponentIndexer<V, E> implements SpanningSearchVisitor<V, E> {

      private static class Detail<V> {

         int discoverTime;
         int lowPoint;
         V predecessor;
      }
      private int index = 0;
      private int dfsTime = 0;
      private Graph<V, E> graph;
      private Map<V, Detail<V> > vertexDetails;
      private Map<E, Integer> edgeComponent;
      private List<V> articulationPoints;
      private Stack<E> stack;

      BiconnectedComponentIndexer(Graph<V, E> graph) {
         this.graph = graph;
         this.edgeComponent = new HashMap<E, Integer>();
         this.vertexDetails = new HashMap<V, Detail<V> >();
         this.articulationPoints = new LinkedList<V>();
         this.stack = new Stack<E>();
      }

      @Override
      public void startVertex(V u) {
         Detail<V> detail = vertexDetails.get(u);
         if (detail == null)
         {
            detail = new Detail<V>();
            vertexDetails.put(u, detail);
         }
         detail.predecessor = u;
      }

      @Override
      public void discoverVertex(V u) {
         Detail<V> detail = vertexDetails.get(u);
         if (detail == null)
         {
            detail = new Detail<V>();
            vertexDetails.put(u, detail);
         }
         detail.discoverTime = ++dfsTime;
         detail.lowPoint = detail.discoverTime;
      }

      @Override
      public void treeEdge(V source, V target) {
         stack.push(graph.getEdge(source, target));
         Detail<V> targetDetail = vertexDetails.get(target);
         targetDetail.predecessor = source;
      }

      @Override
      public void backEdge(V source, V target) {         
         Detail<V> sourceDetail = vertexDetails.get(source);
         Detail<V> targetDetail = vertexDetails.get(target);         
         if (!target.equals(sourceDetail.predecessor)) {
            E edge = graph.getEdge(source, target);
            stack.push(edge);
            sourceDetail.lowPoint =
                    Math.min(sourceDetail.lowPoint,
                    targetDetail.discoverTime);
         }
      }

      @Override
      public void finishVertex(V u) {
         Detail<V> detail = vertexDetails.get(u);
         V parent = detail.predecessor;
         Detail<V> parentDetail = vertexDetails.get(parent);

         int dtmOfDubiousParent = parentDetail.discoverTime;
         boolean isArticulationPoint = false;
         if (dtmOfDubiousParent > detail.discoverTime) {
            parent = parentDetail.predecessor;
            isArticulationPoint = true;
            vertexDetails.get(detail.predecessor).predecessor = u;
            detail.predecessor = u;
         }

         if (parent.equals(u)) { // at top
            if (detail.discoverTime + 1 == dtmOfDubiousParent) {
               isArticulationPoint = false;
            }
         } else {
            parentDetail.lowPoint =
                    Math.min(parentDetail.lowPoint, detail.lowPoint);

            if (detail.lowPoint >= parentDetail.discoverTime) {
               if (parentDetail.discoverTime > vertexDetails.get(parentDetail.predecessor).discoverTime) {
                  detail.predecessor = parentDetail.predecessor;
                  parentDetail.predecessor = u;
               }

               while (vertexDetails.get(graph.getEdgeSource(stack.peek())).discoverTime >= detail.discoverTime) {
                  edgeComponent.put(stack.peek(), index);
                  stack.pop();
               }
               edgeComponent.put(stack.peek(), index);
               stack.pop();
               ++index;
               
               if (stack.empty()) {
                  detail.predecessor = parent;
                  parentDetail.predecessor = u;
               }
            }
         }
         if (isArticulationPoint) {
            articulationPoints.add(u);
         }
      }

      @Override
      public void initializeVertex(V u) {
      }

      @Override
      public void examineEdge(V source, V target) {
      }

      @Override
      public void forwardOrCrossEdge(V source, V target) {
      }
   };
   private Graph<V, E> graph;
   private BiconnectedComponentIndexer<V, E> componentVisitor;
   private DepthFirstSearch<V, E> depthFirstSearch;

   public Biconnectivity(Graph<V, E> graph) {
      this.graph = graph;
      depthFirstSearch = new DepthFirstSearch<V, E>(graph);
   }

   private void lazyImpl() {
      if (componentVisitor == null) {
         this.componentVisitor = new BiconnectedComponentIndexer<V, E>(graph);
         depthFirstSearch.traverse(componentVisitor);
      }
   }

   public int getComponents() {
      if (graph.vertexSet().isEmpty()) {
         return 0;
      }

      lazyImpl();

      return componentVisitor.index;
   }

   public int getComponent(E edge) {
      lazyImpl();
      return componentVisitor.edgeComponent.get(edge);
   }

   public boolean isBiconnected()
   {
      return getComponents() == 1;
   }

   public List<V> getArticulationPoints() {
      return componentVisitor.articulationPoints;
   }

   public void makeBiconnectedPlanar(EdgeFactory<V, E> edgeFactory) {
      int componentCount = getComponents();

      if (componentCount < 2) {
         return;
      }

      for (V articulationPoint : getArticulationPoints()) {
         int previous_component = Integer.MAX_VALUE;
         V previous_vertex = null;

         for (E e : graph.edgesOf(articulationPoint)) {

            V e_source = graph.getEdgeSource(e);
            V e_target = graph.getEdgeTarget(e);

            //Skip self-loops and parallel edges
            if (e_source.equals(e_target) || previous_vertex.equals(e_target)) {
               continue;
            }

            V current_vertex = e_source.equals(articulationPoint) ? e_target : e_source;
            int current_component = getComponent(e);
            if (previous_vertex != null
                    && current_component != previous_component) {
               edgeFactory.createEdge(current_vertex, previous_vertex);
            }

            previous_vertex = current_vertex;
            previous_component = current_component;
         }
      }
   }

   public void makeBiconnectedPlanar() {
      makeBiconnectedPlanar(graph.getEdgeFactory());
   }
} 
