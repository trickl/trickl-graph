package com.trickl.graph.planar;

public interface FaceFactory<V, F> {
   /**
    * Create a face object to be associated with the planar graph face.
    * Note that the parameters passed into the constructor may be invalidated
    * due to later manipulations of the graph. As such, it's normally unwise
    * to store this information with the face. However, the context can be useful
    * for some algorithms (like copying other graphs with existing faces).
    * @return The new face object to be asssociated with the graph face.
    */
   F createFace(V source, V target, boolean isBoundary);
}
