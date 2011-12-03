package com.trickl.graph.planar;

import com.trickl.graph.edges.DirectedEdge;
import java.util.Set;

/**
 * A planar graph that associates a face class instance to every logical
 * face.
 * @author tgee
 * @param <V> Vertex type
 * @param <E> Edge type
 * @param <F> Face type
 */
public interface PlanarFaceGraph <V, E, F> extends PlanarGraph<V, E> {
   public Set<F> faceSet();
   public F getFace(V source, V target);
   public FaceFactory<V, F> getFaceFactory();
   public DirectedEdge<V> getAdjacentEdge(F face);
   public boolean replace(F oldFace, F newFace);
}
