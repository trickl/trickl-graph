package com.trickl.graph.planar;

public interface PlanarFaceTraversal<V, E> {
   void traverse(PlanarFaceTraversalVisitor<V, E> visitor);
}
