package com.trickl.graph.planar;

import com.trickl.graph.edges.DirectedEdge;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import org.jgrapht.EdgeFactory;
import org.jgrapht.Graphs;

/**
 * Define the planar subgraph of another planar graph.
 * WARNING: In it's current implementation, the boundary is not maintained
 * if edges are added or removed to the subgraph. 
 * @author tgee
 * @param <V>
 * @param <E>
 */
public class PlanarSubGraph<V, E> implements PlanarGraph<V, E> {

   PlanarGraph<V, E> graph;
   Set<V> vertices;
   DirectedEdge<V> boundary;

   public PlanarSubGraph(PlanarGraph<V, E> graph, Set<V> vertices, V boundarySource, V boundaryTarget) {
      this.graph = graph;
      this.vertices = vertices;
      this.boundary = new DirectedEdge<V>(boundarySource, boundaryTarget);
   }

   @Override
   public E addEdge(V sourceVertex, V targetVertex, V beforeVertex, V afterVertex) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public boolean addEdge(V sourceVertex, V targetVertex, V beforeVertex, V afterVertex, E e) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public V getNextVertex(V source, V target) {
      if (!containsEdge(source, target)) {
         throw new NoSuchElementException("Edge not found.");
      }
      V next = graph.getNextVertex(source, target);
      while (!vertices.contains(next)) {
         next = graph.getNextVertex(next, target);
      }
      return next;
   }

   @Override
   public V getPrevVertex(V source, V target) {
      if (!containsEdge(source, target)) {
         throw new NoSuchElementException("Edge not found.");
      }
      V prev = graph.getPrevVertex(source, target);
      while (!vertices.contains(prev)) {
         prev = graph.getPrevVertex(source, prev);
      }
      return prev;
   }

   @Override
   public boolean isBoundary(V source, V target) {
      List<V> boundaryVertices = PlanarGraphs.getBoundaryVertices(this);
      int sourceIndex = boundaryVertices.indexOf(source);
      if (sourceIndex < 0) return false;

      int targetIndex = (sourceIndex + 1) % boundaryVertices.size();
      return boundaryVertices.get(targetIndex).equals(target);
   }

   @Override
   public Set<E> getAllEdges(V source, V target) {
      Set<E> edges = new HashSet<E>();
      if (vertices.contains(source)
        && vertices.contains(target)) {
         edges.addAll(graph.getAllEdges(source, target));
      }
      return edges;
   }

   @Override
   public E getEdge(V source, V target) {
      E edge = null;
      if (containsEdge(source, target)) {
         edge = graph.getEdge(source, target);
      }
      return edge;
   }

   @Override
   public EdgeFactory<V, E> getEdgeFactory() {
      return graph.getEdgeFactory();
   }

   @Override
   public E addEdge(V source, V target) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public boolean addEdge(V source, V target, E edge) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public boolean addVertex(V vertex) {
      return graph.addVertex(vertex) && vertices.add(vertex);
   }

   @Override
   public boolean containsEdge(V source, V target) {
      return (vertices.contains(source)
           && vertices.contains(target)
           && graph.containsEdge(source, target));
   }

   @Override
   public boolean containsEdge(E edge) {
      return (vertices.contains(graph.getEdgeSource(edge))
           && vertices.contains(graph.getEdgeTarget(edge))
           && graph.containsEdge(edge));
   }

   @Override
   public boolean containsVertex(V vertex) {
      return vertices.contains(vertex);
   }

   @Override
   public Set<E> edgeSet() {
      Set<E> edges = new HashSet<E>();
      for (V vertex : vertices) {
         edges.addAll(edgesOf(vertex));
      }
      return edges;
   }

   @Override
   public Set<E> edgesOf(V v) {
      Set<E> edges = new LinkedHashSet<E>();
      if (vertices.contains(v)) {
         for (E edge : graph.edgesOf(v)) {
            V target = Graphs.getOppositeVertex(graph, edge, v);
            if (vertices.contains(target)) {
               edges.add(edge);
            }
         }
      }
      return edges;
   }

   @Override
   public boolean removeAllEdges(Collection<? extends E> edges) {
      boolean removed = false;
      for (E edge : edges) {
         removed = removed && removeEdge(edge);
      }
      return removed;
   }

   @Override
   public Set<E> removeAllEdges(V source, V target) {
      Set<E> edges = new HashSet<E>();
      for (E edge : getAllEdges(source, target)) {
         edges.add(edge);
         removeEdge(edge);
      }
      return edges;
   }

   @Override
   public boolean removeAllVertices(Collection<? extends V> vertices) {
      boolean removed = false;
      for (V vertex : vertices) {
         removed = removed && removeVertex(vertex);
      }
      return removed;
   }

   @Override
   public E removeEdge(V source, V target) {
      E edge = getEdge(source, target);
      if (edge != null) graph.removeEdge(source, target);
      return edge;
   }

   @Override
   public boolean removeEdge(E edge) {
      return containsEdge(edge) && graph.removeEdge(edge);
   }

   @Override
   public boolean removeVertex(V vertex) {
      return vertices.remove(vertex) && graph.removeVertex(vertex);
   }

   @Override
   public Set<V> vertexSet() {
      return vertices;
   }

   @Override
   public V getEdgeSource(E edge) {
      return containsEdge(edge) ? graph.getEdgeSource(edge) : null;
   }

   @Override
   public V getEdgeTarget(E edge) {
      return containsEdge(edge) ? graph.getEdgeTarget(edge) : null;
   }

   @Override
   public double getEdgeWeight(E edge) {
      return containsEdge(edge) ? graph.getEdgeWeight(edge) : null;
   }

   @Override
   public DirectedEdge<V> getBoundary() {
      return boundary;
   }

   @Override
   public void setBoundary(V source, V target) {
      if (!containsEdge(source, target)) {
         throw new NoSuchElementException("Edge not in graph.");
      }
      this.boundary = new DirectedEdge<V>(source, target);
   }
}
