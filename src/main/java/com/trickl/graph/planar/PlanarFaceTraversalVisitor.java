package com.trickl.graph.planar;

public interface PlanarFaceTraversalVisitor<V, E> {
    void beginTraversal();
    void beginFace(V source, V target);
    void nextEdge(V source, V target);
    void nextVertex(V vertex);
    void endFace(V source, V target);
    void endTraversal();
}
