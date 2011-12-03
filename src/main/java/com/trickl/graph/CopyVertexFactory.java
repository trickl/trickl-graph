package com.trickl.graph;

public interface CopyVertexFactory<V1, V2> {
   V1 createVertex(V2 vertex);
}
