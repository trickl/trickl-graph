package com.trickl.graph.planar;

import java.util.List;

public interface PlanarCanonicalOrdering<V, E> {
   List<V> getOrder(PlanarGraph<V, E> graph, V first);
}
