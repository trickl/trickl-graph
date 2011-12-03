package com.trickl.graph.planar;

import com.trickl.graph.edges.DirectedEdge;
import org.jgrapht.Graph;

public interface PlanarGraph<V, E> extends Graph<V, E>, PlanarEmbedding<V, E> {
   // Usually the after vertex is not necessary and can be determined, however
   // sometimes it is ambiguous.
   E addEdge(V sourceVertex, V targetVertex, V beforeVertex, V afterVertex);
   boolean addEdge(V sourceVertex, V targetVertex, V beforeVertex, V afterVertex, E e);

   V getNextVertex(V source, V target);
   V getPrevVertex(V source, V target);

   DirectedEdge<V> getBoundary();
   boolean isBoundary(V source, V target);
   void setBoundary(V source, V target);
}
