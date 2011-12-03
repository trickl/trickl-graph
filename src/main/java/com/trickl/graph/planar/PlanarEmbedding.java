package com.trickl.graph.planar;

import java.util.Set;

/**
 * Marker interface for planar graphs, stipulates that edges
 * are returned in a consistent order.
 * @author tgee
 * @param <V>
 * @param <E>
 */
public interface PlanarEmbedding<V, E> {
   /**
    * Return an ordered set of edges connected to a vertex
    * @param vertex
    * @return An ordered edge list
    */
   Set<E> edgesOf(V vertex);
}
