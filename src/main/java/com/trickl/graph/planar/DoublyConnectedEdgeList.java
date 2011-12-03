package com.trickl.graph.planar;

import com.trickl.graph.edges.DirectedEdge;
import com.trickl.graph.planar.xml.XmlDoublyConnectedEdgeListAdapter;
import java.io.Serializable;
import java.util.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.jgrapht.EdgeFactory;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.ClassBasedEdgeFactory;

@XmlJavaTypeAdapter(value = XmlDoublyConnectedEdgeListAdapter.class)
public class DoublyConnectedEdgeList<V, E, F>
        implements PlanarFaceGraph<V, E, F>,
        UndirectedGraph<V, E>,
        Serializable {

   private EdgeFactory<V, E> edgeFactory;
   private FaceFactory<V, F> faceFactory;
   private F boundaryFace;
   private Map<E, DcelHalfEdge<V, E, F>> edgeMap;
   private Map<V, DcelVertex<V, E, F>> vertexMap;
   private Map<F, DcelFace<V, E, F>> faceMap;

   private DoublyConnectedEdgeList() {
   }

   public DoublyConnectedEdgeList(PlanarGraph<V, E> graph, Class<? extends F> faceClass) {
      this(graph, new ClassBasedFaceFactory<V, F>(faceClass));
   }

   public DoublyConnectedEdgeList(PlanarGraph<V, E> graph, FaceFactory<V, F> faceFactory) {
      this(graph.getEdgeFactory(), faceFactory);
      PlanarGraphs.copy(graph, this, null, null);
   }

   public DoublyConnectedEdgeList(Class<? extends E> edgeClass, Class<? extends F> faceClass) {
      this(new ClassBasedEdgeFactory<V, E>(edgeClass), faceClass);
   }

   public DoublyConnectedEdgeList(EdgeFactory<V, E> edgeFactory, Class<? extends F> faceClass) {
      this(edgeFactory, new ClassBasedFaceFactory<V, F>(faceClass));
   }

   public DoublyConnectedEdgeList(EdgeFactory<V, E> edgeFactory, FaceFactory<V, F> faceFactory) {
      this.edgeFactory = edgeFactory;
      this.faceFactory = faceFactory;
      edgeMap = new Hashtable<E, DcelHalfEdge<V, E, F>>();
      vertexMap = new Hashtable<V, DcelVertex<V, E, F>>();
      faceMap = new Hashtable<F, DcelFace<V, E, F>>();

      if (faceFactory != null) {
         boundaryFace = faceFactory.createFace(null, null, true);
         DcelFace<V, E, F> dcelFace = new DcelFace<V, E, F>(boundaryFace, true);
         faceMap.put(boundaryFace, dcelFace);
      }
   }

   @Override
   public EdgeFactory<V, E> getEdgeFactory() {
      return edgeFactory;
   }

   @Override
   public boolean containsEdge(final V source, final V target) {
      return getHalfEdge(source, target) != null;
   }

   @Override
   public boolean containsVertex(V vertex) {
      return vertexMap.containsKey(vertex);
   }

   @Override
   public Set<E> edgesOf(V vertex) {      
      DcelVertex<V, E, F> dcelVertex = vertexMap.get(vertex);
      return (dcelVertex == null ? new HashSet<E>() : dcelVertex.getEdges());
   }

   protected DcelHalfEdge<V, E, F> getHalfEdge(final V source, final V target) {

      DcelVertex<V, E, F> sourceVertex = vertexMap.get(source);
      if (sourceVertex == null) {
         return null;
      }

      return sourceVertex.getHalfEdge(target);
   }

   @Override
   public int degreeOf(V vertex) {
      DcelVertex<V, E, F> dcelVertex = vertexMap.get(vertex);
      if (dcelVertex == null) {
         throw new NoSuchElementException("Vertex not found.");
      }
      return dcelVertex.getEdgeCount();
   }

   @Override
   public Set<E> getAllEdges(V source, V target) {
      HashSet<E> edges = new HashSet<E>();
      E edge = getEdge(source, target);
      if (edge != null) {
         edges.add(getEdge(source, target));
      }
      return edges;
   }

   @Override
   public E getEdge(V source, V target) {
      DcelHalfEdge<V, E, F> dcelHalfEdge = getHalfEdge(source, target);
      if (dcelHalfEdge == null) {
         return null;
      }
      return dcelHalfEdge.getEdge();
   }

   @Override
   public boolean containsEdge(E edge) {
      return edgeMap.containsKey(edge);
   }

   @Override
   public Set<E> edgeSet() {
      return edgeMap.keySet();
   }

   @Override
   public Set<V> vertexSet() {
      return vertexMap.keySet();
   }

   @Override
   public Set<F> faceSet() {
      return faceMap.keySet();
   }

   @Override
   public V getEdgeSource(E edge) {
      DcelHalfEdge<V, E, F> dcelHalfEdge = edgeMap.get(edge);
      if (dcelHalfEdge == null) {
         throw new NoSuchElementException("Edge not found.");
      }
      return dcelHalfEdge.getOrigin().getVertex();
   }

   @Override
   public V getEdgeTarget(E edge) {
      DcelHalfEdge<V, E, F> dcelHalfEdge = edgeMap.get(edge);
      if (dcelHalfEdge == null) {
         throw new NoSuchElementException("Edge not found.");
      }
      return dcelHalfEdge.getNext().getOrigin().getVertex();
   }

   @Override
   public double getEdgeWeight(E e) {
      return 1.0;
   }

   @Override
   public DirectedEdge<V> getBoundary() {
      DcelFace<V, E, F> dcelFace = faceMap.get(boundaryFace);
      if (dcelFace.getAdjacent() == null) {
         // Graph contains zero edges
         return null;
      }
      return new DirectedEdge<V>(dcelFace.getAdjacent().getOrigin().getVertex(),
                                 dcelFace.getAdjacent().getNext().getOrigin().getVertex());
   }
  
   public V getNextVertexOnBoundary(V vertex) {
      DcelFace<V, E, F> dcelFace = faceMap.get(boundaryFace);
      DcelHalfEdge<V, E, F> dcelHalfEdge = dcelFace.getHalfEdge(vertex);
      if (dcelHalfEdge == null) {
         throw new NoSuchElementException("Vertex not found on boundary.");
      }
      return dcelHalfEdge.getNext().getOrigin().getVertex();
   }
   
   public V getPrevVertexOnBoundary(V vertex) {
      DcelFace<V, E, F> dcelFace = faceMap.get(boundaryFace);
      DcelHalfEdge<V, E, F> dcelHalfEdge = dcelFace.getHalfEdge(vertex);
      if (dcelHalfEdge == null) {
         throw new NoSuchElementException("Vertex not found on boundary.");
      }
      return dcelHalfEdge.getPrev().getOrigin().getVertex();
   }

   @Override
   public V getNextVertex(V source, V target) {
      DcelHalfEdge<V, E, F> dcelHalfEdge = getHalfEdge(source, target);
      if (dcelHalfEdge == null) {
         throw new NoSuchElementException("Edge not found.");
      }
      return dcelHalfEdge.getNext().getNext().getOrigin().getVertex();
   }

   @Override
   public V getPrevVertex(V source, V target) {
      DcelHalfEdge<V, E, F> dcelHalfEdge = getHalfEdge(source, target);
      if (dcelHalfEdge == null) {
         throw new NoSuchElementException("Edge not found.");
      }
      return dcelHalfEdge.getPrev().getOrigin().getVertex();
   }

   @Override
   public boolean isBoundary(V source, V target) {
      DcelHalfEdge<V, E, F> dcelHalfEdge = getHalfEdge(source, target);
      if (dcelHalfEdge == null) {
         throw new NoSuchElementException("Edge not found.");
      }
      return dcelHalfEdge.isBoundary();
   }
   
   public boolean isVertexBoundary(V vertex) {
      DcelVertex<V, E, F> dcelVertex = vertexMap.get(vertex);
      if (dcelVertex == null) {
         throw new NoSuchElementException("Vertex not found.");
      }
      return dcelVertex.isBoundary();
   }

   @Override
   public F getFace(V source, V target) {
      DcelHalfEdge<V, E, F> halfEdge = getHalfEdge(source, target);
      if (halfEdge == null) {
         return null;
      }
      return halfEdge.getFace().getFace();
   }

   @Override
   public E addEdge(V source, V target) {
      return addEdge(source, target, (V) null, (V) null);
   }

   @Override
   public boolean addEdge(V source, V target, E edge) {
      return addEdge(source, target, null, null, edge);
   }

   @Override
   public E addEdge(V sourceVertex, V targetVertex, V beforeVertex, V afterVertex) {
      E edge = edgeFactory.createEdge(sourceVertex, targetVertex);
      addEdge(sourceVertex, targetVertex, beforeVertex, afterVertex, edge);
      return edge;
   }

    /**
    * An an edge to the DCEL.
    * Note that the DCEL is undirected, so this will add two half-edges and
    * attempt to do so in a way that leaves the DCEL in a safe-traversable state.
    * @param sourceVertex
    * @param targetVertex
    * @param beforeVertex
    * @param e
    * @return
    */
   @Override
   public boolean addEdge(V sourceVertex, V targetVertex, V beforeVertex, V afterVertex, E e) {

      if (containsEdge(sourceVertex, targetVertex)) {
         return false;
      }

      addVertex(sourceVertex);
      addVertex(targetVertex);

      DcelVertex<V, E, F> source = vertexMap.get(sourceVertex);
      DcelVertex<V, E, F> target = vertexMap.get(targetVertex);
      DcelHalfEdge<V, E, F> beforeEdge = null;
      if (beforeVertex != null) {
         DcelVertex<V, E, F> before = vertexMap.get(beforeVertex);
         if (before != null) {
            beforeEdge = before.getHalfEdge(sourceVertex);
         }
         if (beforeEdge == null) {
            throw new NoSuchElementException("Before ("
                                             + beforeVertex.toString()
                                             + ") to ("
                                             + sourceVertex.toString()
                                             + ") source edge not found");
         }   
      }

      DcelHalfEdge<V, E, F> afterEdge = null;
      if (afterVertex != null) {
         afterEdge = target.getHalfEdge(afterVertex);
         if (afterEdge == null) {
            throw new NoSuchElementException("Target ("
                                             + targetVertex.toString()
                                             + ") to ("
                                             + afterVertex.toString()
                                             + ") after edge not found");
         }

         if (beforeEdge != null 
          &&!beforeEdge.getFace().equals(afterEdge.getFace())) {
            throw new NoSuchElementException("Before ("
                                             + beforeEdge.toString()
                                             + ") and ("
                                             + afterEdge.toString()
                                             + ") after edges must share a face");
         }
      }

      DcelFace<V, E, F> boundary = faceMap.get(boundaryFace);
      DcelHalfEdge<V, E, F> createdEdge =
              source.addEdge(target, beforeEdge, afterEdge, boundary, getFaceFactory(), e);

      edgeMap.put(e, createdEdge);

      faceMap.put(createdEdge.getFace().getFace(), createdEdge.getFace());
      faceMap.put(createdEdge.getTwin().getFace().getFace(), createdEdge.getTwin().getFace());

      assert(isFaceConsistent(createdEdge)) : "Face mismatch, edge: " + createdEdge;
      assert(isFaceConsistent(createdEdge.getTwin())) : "Face mismatch, edge: " + createdEdge.getTwin();

      return true;
   }

   private boolean isFaceConsistent(DcelHalfEdge<V, E, F> start) {
      DcelFace<V, E, F> face = null;
      for (DcelHalfEdge<V, E, F>  halfEdge : start.edges()) {
         if (face == null) face = halfEdge.getFace();
         else if (!face.equals(halfEdge.getFace())) {
           return false;
         }
      }

      return true;
   }

   @Override
   public boolean addVertex(V vertex) {
      if (!vertexMap.containsKey(vertex)) {
         vertexMap.put(vertex, new DcelVertex<V, E, F>(vertex));
         return true;
      }
      return false;
   }

   @Override
   public boolean removeAllEdges(Collection<? extends E> edges) {
      boolean changed = false;
      for (E edge : edges) {
         changed = changed || removeEdge(edge);
      }
      return changed;
   }

   @Override
   public Set<E> removeAllEdges(V source, V target) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public boolean removeAllVertices(Collection<? extends V> vertices) {
      boolean changed = false;
      for (V vertex : vertices) {
         changed = changed || removeVertex(vertex);
      }
      return changed;
   }

   @Override
   public E removeEdge(V source, V target) {


      E edge = getEdge(source, target);
      if (edge != null) {
         removeEdge(getEdge(source, target));
      }

      return edge;
   }

   @Override
   public boolean removeEdge(E edge) {
      DcelHalfEdge<V, E, F> halfEdge = edgeMap.get(edge);
      if (halfEdge == null) {
         return false;
      }

      DcelVertex<V, E, F> source = halfEdge.getOrigin();
      DcelVertex<V, E, F> target = halfEdge.getNext().getOrigin();
      DcelFace<V, E, F> face = halfEdge.getFace();
      DcelFace<V, E, F> opposingFace = halfEdge.getTwin().getFace();

      DcelHalfEdge<V, E, F> nextEdge = halfEdge.getNext();

      if (face.isBoundary()) {
         // Maintain the same boundary face
         halfEdge.getTwin().remove();
      }
      else {
         halfEdge.remove();
      }
      edgeMap.remove(edge);

      // Remove any disconnected vertices
      if (source.getLeaving() == null) {
         vertexMap.remove(source.getVertex());
      }
      if (target.getLeaving() == null) {
         vertexMap.remove(target.getVertex());
      }

      // Remove any disconnected faces
      if (face.getAdjacent() == null
          && !face.isBoundary()) {
         faceMap.remove(face.getFace());
      }

      if (opposingFace.getAdjacent() == null
         && !opposingFace.isBoundary()) {
         faceMap.remove(opposingFace.getFace());
      }

      assert(isFaceConsistent(nextEdge)) : "Face mismatch, edge: " + nextEdge;

      return true;
   }

   @Override
   public boolean removeVertex(V vertex) {
      DcelVertex<V, E, F> dcelVertex = vertexMap.get(vertex);
      if (dcelVertex == null) {
         return false;
      }

      Set<E> edges = new LinkedHashSet<E>();
      // Get a copy of the edges as the iterator will be invalidated
      for (E edge : edgesOf(vertex)) {
         edges.add(edge);
      }
      
      for (E edge : edges) {
         removeEdge(edge);
         edgeMap.remove(edge);
      }

      dcelVertex.remove();
      vertexMap.remove(vertex);

      return true;
   }

   @Override
   public void setBoundary(V source, V target) {
      DcelHalfEdge<V, E, F> dcelHalfEdge = getHalfEdge(source, target);
      if (dcelHalfEdge == null) {
         throw new NoSuchElementException("Edge not found.");
      }

      DcelFace<V, E, F> oldBoundaryFace = faceMap.get(boundaryFace);
      oldBoundaryFace.setBoundary(false);
      DcelFace<V, E, F> newBoundaryFace = dcelHalfEdge.getFace();
      newBoundaryFace.setBoundary(true);

      boundaryFace = newBoundaryFace.getFace();
   }

   @Override
   public DirectedEdge<V> getAdjacentEdge(F face) {
      DcelFace<V, E, F> dcelFace = faceMap.get(face);
      if (dcelFace == null) {   
         throw new NoSuchElementException("Face not found.");
      }
      
      V source = dcelFace.getAdjacent().getOrigin().getVertex();
      V target = dcelFace.getAdjacent().getNext().getOrigin().getVertex();

      return new DirectedEdge<V>(source, target);
   }

   @Override
   public boolean replace(F oldFace, F newFace) {
      DcelFace<V, E, F> dcelFace = faceMap.get(oldFace);
      if (dcelFace == null) {
         return false;
      }
      faceMap.remove(oldFace);
      faceMap.put(newFace, dcelFace);
      dcelFace.setFace(newFace);

      if (oldFace.equals(boundaryFace)) {
         boundaryFace = newFace;
      }

      return true;
   }

   /**
    * A bit uncomfortable about this. Not sure if we should allow people to alter the
    * face factory after construction. The reason this was added was due to the conceptual
    * problem of copying a PlanarFaceGraph. Using the PlanarGraph algorithm (which
    * knows nothing about faces), a way is required to map the faces from the original to the copy.
    * This is achieved with the CopyFaceFactory. However, using that, necessitates
    * that we can change the factory to something else after it is used.
    * I *think* it should be okay to allow the factories to be changed after construction.
    * @param faceFactory
    */
   public void setFaceFactory(FaceFactory<V, F> faceFactory) {
      this.faceFactory = faceFactory;
   }

   @Override
   public FaceFactory<V, F> getFaceFactory() {
      return faceFactory;
   }

   public Map<E, DcelHalfEdge<V, E, F>> getEdgeMap() {
      return edgeMap;
   }

   public Map<V, DcelVertex<V, E, F>> getVertexMap() {
      return vertexMap;
   }

   public Map<F, DcelFace<V, E, F>> getFaceMap() {
      return faceMap;
   }

   public F getBoundaryFace() {
      return boundaryFace;
   }

   public void setBoundaryFace(F boundaryFace) {
      this.boundaryFace = boundaryFace;
   }
}
 
