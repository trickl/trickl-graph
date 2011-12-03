package com.trickl.graph;

public interface EdgeVisitor<E> {
   void onEdge(E edge);
}
