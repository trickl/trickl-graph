package com.trickl.graph.generate;

import com.trickl.graph.planar.PlanarGraph;
import org.jgrapht.VertexFactory;

public interface PlanarGraphGenerator<V, E, T> {
   void generateGraph(PlanarGraph<V,E> target, VertexFactory<V> vertexFactory, java.util.Map<java.lang.String,T> resultMap);
}
