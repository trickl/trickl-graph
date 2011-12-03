package com.trickl.graph;

public interface CopyEdgeFactory<V1, E1, E2> {
   E1 createEdge(V1 sourceVertex, V1 targetVertex, E2 edge);
}
