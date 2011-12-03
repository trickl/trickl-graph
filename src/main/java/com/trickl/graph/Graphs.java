package com.trickl.graph;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import org.jgrapht.Graph;
import org.jgrapht.UndirectedGraph;

public final class Graphs {

   private Graphs() {
   }

   static public <V, E> boolean isEdgeDirected(Graph<V, E> graph, E edge) {
      V source = graph.getEdgeSource(edge);
      V target = graph.getEdgeTarget(edge);

      E oppositeEdge = graph.getEdge(target, source);
      return !oppositeEdge.equals(edge);
   }

   static public <V1, E1, V2, E2> Map<V1, V2> aggregate(Graph<V1, E1> source,
           Graph<V2, E2> target,
           Map<V1, Integer> aggregateGroups,
           CopyVertexFactory<V2, V1> vertexFactory,
           CopyEdgeFactory<V2, E2, E1> edgeFactory) {

      CopyGraphVisitor<V1, E1, V2, E2> copyGraphVisitor = new CopyGraphVisitor(
              source, target, vertexFactory, edgeFactory);
      copyGraphVisitor.setAggregationGroups(aggregateGroups);
      DepthFirstSearch<V1, E1> depthFirstTraversal = new DepthFirstSearch<V1, E1>(source);
      depthFirstTraversal.traverse(copyGraphVisitor);
      return copyGraphVisitor.getVertexMap();
   }

   static public <V1, E1, V2, E2> Map<V1, V2> aggregate(Graph<V1, E1> source,
           Graph<V2, E2> target,
           Map<V1, Integer> aggregateGroups) {
      return aggregate(source, target, aggregateGroups, null, null);
   }

   static public <V1, E1, V2, E2> Map<V1, V2> copy(Graph<V1, E1> source,
           Graph<V2, E2> target,
           CopyVertexFactory<V2, V1> vertexFactory,
           CopyEdgeFactory<V2, E2, E1> edgeFactory) {
      return aggregate(source, target, null, vertexFactory, edgeFactory);
   }

   public static <V, E> Map<V, Double> getNeighbourhoodWeights(UndirectedGraph<V, E> graph,
           V vertex,
           NeighbourhoodFunction neighbourhoodFunction,
           double weightThreshold) {
      final Map<V, Double> distances = new HashMap<V, Double>();
      Map<V, Double> weights = new HashMap<V, Double>();

      // Use Dijkstra's algorithm to calculate distances from this vertex
      TreeSet<V> queue = new TreeSet<V>(new Comparator<V>() {
         @Override
         public int compare(V lhs, V rhs) {
            int result = 0;
            Double lhsDistance = distances.get(lhs);
            Double rhsDistance = distances.get(rhs);
            if (lhsDistance != null && rhsDistance != null) {
               result = lhsDistance.compareTo(rhsDistance);
            }
            return result;
         }
      });
      distances.put(vertex, 0.0);
      weights.put(vertex, neighbourhoodFunction.evaluate(0.));
      queue.add(vertex);

      while (!queue.isEmpty()) {
         V u = queue.first();
         queue.remove(u);

         for (E edge : graph.edgesOf(u)) {
            V v = (u == graph.getEdgeSource(edge)) ? graph.getEdgeTarget(edge) : graph.getEdgeSource(edge);
            double altDistance = distances.get(u) + graph.getEdgeWeight(graph.getEdge(u, v));
            double altWeight = neighbourhoodFunction.evaluate(altDistance);

            // Only consider vertices within a distance that gives a sufficient weight
            if (altWeight > weightThreshold) {
               if (!distances.containsKey(v)
                       || altDistance < distances.get(v)) {
                  queue.remove(v);
                  distances.put(v, altDistance);
                  weights.put(v, altWeight);
                  queue.add(v);
               }
            }
         }
      }
      return weights;
   }
}
