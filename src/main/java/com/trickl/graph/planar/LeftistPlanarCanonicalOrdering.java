package com.trickl.graph.planar;

import com.trickl.graph.edges.DirectedEdge;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/* This is an implemention of
 * "Leftist Canonical Ordering"
 * Melanie Badent, Micael Baur, Ulrik Brandes, Sabine Cornelsen
 * Department of Computer and Information Science
 * University of Konstanz (2009)
 * http://www.informatik.uni-konstanz.de/~cornelse/Papers/bbbc-lco-gd09.pdf
 */
public class LeftistPlanarCanonicalOrdering<V, E> implements PlanarCanonicalOrdering<V, E> {

   private static class BeltCutFace<V> {

      public BeltCutFace(LinkedList<DirectedEdge<V>> edges) {
         this(edges, null);
      }

      public BeltCutFace(LinkedList<DirectedEdge<V>> edgeChain, V rightmostStopper) {
         this.edgeChain = edgeChain;
         this.rightmostStopper = rightmostStopper;
      }

      @Override
      public String toString() {
         return edgeChain.toString();
      }

      LinkedList<DirectedEdge<V>> edgeChain;
      V rightmostStopper;
   }

   PlanarGraph<V, E> graph;
   List<BeltCutFace<V>> belt;
   Set<DirectedEdge<V>> markedEdges;
   Map<V, Integer>  vertexCutFaces;
   Map<V, Integer>  vertexCutEdges;
   int candidateIndex;

   /**
    * Create an instance of the canonical ordering solver
    * @param graph A triconnected graph
    */
   public LeftistPlanarCanonicalOrdering() {

   }

   /**
    * Get the canonical ordering of the graph vertices
    * (See Algorithm 2 in the cited paper.)
    * @param graph A triconnected graph
    * @param first The first vertex in the ordering (must be on the outside of the graph)
    * @return The vertices in leftist canonical order
    */
   @Override
   public List<V> getOrder(PlanarGraph<V, E> graph, V first) {
      if (graph == null || first == null) {
         throw new NullPointerException();
      }
      List<V> ordering = new ArrayList<V>(graph.vertexSet().size());

      this.graph = graph;
      belt = new LinkedList<BeltCutFace<V>>();
      markedEdges = new HashSet<DirectedEdge<V>>();
      vertexCutFaces = new HashMap<V, Integer>();
      vertexCutEdges = new HashMap<V, Integer>();

      V second = PlanarGraphs.getPrevVertexOnBoundary(graph, first);
      V last   = PlanarGraphs.getNextVertexOnBoundary(graph, first);
      
      for (V vertex : graph.vertexSet()) {
         vertexCutFaces.put(vertex, 0);
         vertexCutEdges.put(vertex, 0);
      }
      vertexCutFaces.put(last, 1);

      markedEdges.add(new DirectedEdge<V>(first, second));
      markedEdges.add(new DirectedEdge<V>(second, first));
      LinkedList<DirectedEdge<V>> initialBeltEdgeChain = new LinkedList<DirectedEdge<V>>();
      initialBeltEdgeChain.add(new DirectedEdge<V>(second, first));
      initialBeltEdgeChain.add(new DirectedEdge<V>(first, second));
      initialBeltEdgeChain.add(new DirectedEdge<V>(second, first));
      belt.add(new BeltCutFace<V>(initialBeltEdgeChain, null));

      candidateIndex = 0;
      while (!belt.isEmpty()) {
         ordering.addAll(getLeftmostFeasibleCandidate());
         updateBelt();
      }

      return ordering;
   }

   /**
    * If the vertex appears non-consecutively in the belt
    * @param vertex
    * @return
    */
   private boolean isForbidden(V vertex) {
      return vertexCutFaces.get(vertex) > (vertexCutEdges.get(vertex) + 1);
   }

   /**
    * If the vertex is consecutive and it appears more than twice in the belt
    * @param vertex
    * @return
    */
   private boolean isSingular(V vertex) {
      return 2 < vertexCutFaces.get(vertex)
              && vertexCutFaces.get(vertex) == vertexCutEdges.get(vertex) + 1;
   }

   /**
    *  (See Algorithm 3 in the cited paper.)
    */
   private List<V> getLeftmostFeasibleCandidate() {
      boolean found = false;
      BeltCutFace<V> candidate = null;

      do {
         candidate = belt.get(candidateIndex);
         List<DirectedEdge<V>> edgeChain = candidate.edgeChain;
         int j = edgeChain.size() - 1;
         if (!edgeChain.get(0).getSource().equals(
             edgeChain.get(j).getTarget()))
         {
            // Look from right to left until a stopper is found
            while (j > 0 &&
                   !(isForbidden(edgeChain.get(j).getSource())
                  || isSingular(edgeChain.get(j).getSource()))) {
               --j;
            }

            if (j > 0) {
               candidate.rightmostStopper = edgeChain.get(j).getSource();
            }

            // If the candidate contains no stopper or it is a singular singleton
            // then it is the next locally feasible candidate
            if (j == 0 
            || (isSingular(candidate.rightmostStopper)
                && edgeChain.size() == 2)) {
               found = true;
               for (DirectedEdge<V> edge : candidate.edgeChain) {                  
                  markedEdges.add(new DirectedEdge<V>(edge.getTarget(),
                                                      edge.getSource()));
               }
            }
         }

         if (!found) {
            candidateIndex++;
            if (candidateIndex == belt.size()) {
               throw new IllegalArgumentException("Supplied graph must be planar and triconnected.");
            }
         }
      }
      while (!found);

      List<V> path = new LinkedList<V>();

      for (int i = 0; i < candidate.edgeChain.size() - 1; ++i) {
         path.add(candidate.edgeChain.get(i).getTarget());
      }      

      return path;
   }

   /**
    * (See Algorithm 4 in the cited paper.)
    *
    */
   private List<BeltCutFace<V>> getBeltExtension(BeltCutFace<V> candidate) {
      List<BeltCutFace<V>> extension = new LinkedList<BeltCutFace<V>>();

      for (int j = 1; j < candidate.edgeChain.size(); ++j) {
         DirectedEdge<V> first = candidate.edgeChain.get(j);
         V start = first.getSource();
         V end   = first.getTarget();
         do {
            first = new DirectedEdge<V>(first.getSource(),
                                        graph.getNextVertex(first.getTarget(),
                                                            first.getSource()));
            vertexCutEdges.put(first.getTarget(),
                    vertexCutEdges.get(first.getTarget()) + 1);
            if (!markedEdges.contains(first)) {
               // New cut face
               LinkedList<DirectedEdge<V>> chain = new LinkedList<DirectedEdge<V>>();
               DirectedEdge<V> edge = first;
               do {
                  markedEdges.add(edge);
                  chain.add(edge);
                  vertexCutFaces.put(edge.getTarget(),
                          vertexCutFaces.get(edge.getTarget()) + 1);
                  edge = new DirectedEdge<V>(edge.getTarget(),
                                        graph.getPrevVertex(edge.getTarget(),
                                                            edge.getSource()));
               }
               while (!edge.getTarget().equals(start) &&
                      !edge.getTarget().equals(end));
               markedEdges.add(edge);
               chain.add(edge);
               extension.add(new BeltCutFace(chain, null));
            }
         }
         while(!first.getTarget().equals(end));
      }

      return extension;
   }

   /**
    * (See Algorithm 5 in the cited paper.)
    *
    */
   private void updateBelt() {
      BeltCutFace<V> candidate = belt.get(candidateIndex);
      if (candidate.rightmostStopper != null &&
          isSingular(candidate.rightmostStopper)) {
         // Remove neighbouring items with the same singleton
         for (int j = candidateIndex - 1; j >= 0; belt.remove(j), j--, candidateIndex--)
         {
            BeltCutFace<V> neighbour = belt.get(j);
            if (neighbour.edgeChain.size() != 2) {
               break;
            }
         }
         for (int j = candidateIndex + 1; j < belt.size(); belt.remove(j))
         {
            BeltCutFace<V> neighbour = belt.get(j);
            if (neighbour.edgeChain.size() != 2) {
               break;
            }
         }
      }

      BeltCutFace<V> predecessor = candidateIndex > 0 ?
                                   belt.get(candidateIndex - 1) : null;
      BeltCutFace<V> successor = candidateIndex < (belt.size() - 1) ?
                                   belt.get(candidateIndex + 1) : null;

      if (successor != null) {
         successor.edgeChain.remove(0);
         /*
         if (successor.edgeChain.isEmpty()) {
            belt.remove(candidateIndex + 1);
         }
          * 
          */
      }

      List<BeltCutFace<V>> extension = getBeltExtension(candidate);
      belt.remove(candidateIndex);
      belt.addAll(candidateIndex, extension);

      if (predecessor != null) {
         DirectedEdge<V> firstEdge = predecessor.edgeChain.get(0);
         DirectedEdge<V> lastEdge = predecessor.edgeChain.remove(predecessor.edgeChain.size() - 1);
         if (lastEdge.getSource().equals(predecessor.rightmostStopper)
          || lastEdge.getTarget().equals(firstEdge.getSource())) {
            predecessor.rightmostStopper = null;
            --candidateIndex;
         }
      }
   }
}
