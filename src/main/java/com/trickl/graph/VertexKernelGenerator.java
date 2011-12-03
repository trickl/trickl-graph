package com.trickl.graph;

import cern.colt.matrix.DoubleMatrix2D;
import org.jgrapht.Graph;

public interface VertexKernelGenerator<V, E> {
   DoubleMatrix2D getKernel(Graph<V, E> graph);
   Integer getIndex(V vertex);
   V getVertex(int index);
}
