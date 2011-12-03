//                 Copyright Trickl 2010
// Distributed under the Boost Software License, Version 1.0.
//    (See accompanying file LICENSE_1_0.txt or copy at
//          http://www.boost.org/LICENSE_1_0.txt)
//
// Implementation based on the BOOST C++ library
//
// A "face handle" is an optimization meant to serve two purposes in
// the implementation of the Boyer-Myrvold planarity test: (1) it holds
// the partial planar embedding of a particular vertex as it's being
// constructed, and (2) it allows for efficient traversal around the
// outer face of the partial embedding at that particular vertex. A face
// handle is lightweight, just a shared pointer to the actual implementation,
// since it is passed around/copied liberally in the algorithm. It consists
// of an "anchor" (the actual vertex it's associated with) as well as a
// sequence of edges. The functions first_vertex/second_vertex and
// first_edge/second_edge allow fast access to the beginning and end of the
// stored sequence, which allows one to traverse the outer face of the partial
// planar embedding as it's being created. 
//
// There are some policies below that define the contents of the face handles:
// in the case no embedding is needed (for example, if one just wants to use
// the Boyer-Myrvold algorithm as a true/false test for planarity, the
// no_embedding class can be passed as the StoreEmbedding policy. Otherwise,
// either std_list (which uses as std::list) or recursive_lazy_list can be
// passed as this policy. recursive_lazy_list has the best theoretical
// performance (O(n) for a sequence of interleaved concatenations and reversals
// of the underlying list), but I've noticed little difference between std_list
// and recursive_lazy_list in my tests, even though using std_list changes
// the worst-case complexity of the planarity test to O(n^2)
//
// Another policy is StoreOldHandlesPolicy, which specifies whether or not
// to keep a record of the previous first/second vertex/edge - this is needed
// if a Kuratowski subgraph needs to be isolated.
package com.trickl.graph.planar;

import java.util.List;
import java.util.LinkedList;
import java.util.Collections;
import java.util.Map;
import java.util.Iterator;

import org.jgrapht.Graph;

public class FaceHandle<V, E> {

   public interface OldHandlesStoragePolicy<V, E> {

      void initializeOldVertices();

      void storeOldFaceHandles(V firstVertex,
              V secondVertex,
              E firstEdge,
              E secondEdge);

      V getFirstVertex();

      V getSecondVertex();

      E getFirstEdge();

      E getSecondEdge();
   };

   public static class StoreOldHandles<V, E> implements OldHandlesStoragePolicy<V, E> {

      private V firstVertex;
      private V secondVertex;
      private E firstEdge;
      private E secondEdge;

      public void initializeOldVertices() {
         firstVertex = null;
         secondVertex = null;
      }

      public void storeOldFaceHandles(V firstVertex,
              V secondVertex,
              E firstEdge,
              E secondEdge) {
         this.firstVertex = firstVertex;
         this.secondVertex = secondVertex;
         this.firstEdge = firstEdge;
         this.secondEdge = secondEdge;
      }

      public V getFirstVertex() {
         return firstVertex;
      }

      public V getSecondVertex() {
         return secondVertex;
      }

      public E getFirstEdge() {
         return firstEdge;
      }

      public E getSecondEdge() {
         return secondEdge;
      }
   };

   public static class NoOldHandles<V, E> implements OldHandlesStoragePolicy<V, E> {

      public void initializeOldVertices() {
      }

      public void storeOldFaceHandles(V firstVertex,
              V secondVertex,
              E firstEdge,
              E secondEdge) {
      }

      public V getFirstVertex() {
         throw new UnsupportedOperationException("Using a storage policy that does not store handles.");
      }

      public V getSecondVertex() {
         throw new UnsupportedOperationException("Using a storage policy that does not store handles.");
      }

      public E getFirstEdge() {
         throw new UnsupportedOperationException("Using a storage policy that does not store handles.");
      }

      public E getSecondEdge() {
         throw new UnsupportedOperationException("Using a storage policy that does not store handles.");
      }
   };

   public interface EmbeddingStoragePolicy<E> {

      void addLast(E edge);

      void addFront(E edge);

      void reverse();

      void addFront(EmbeddingStoragePolicy<E> policy);

      void addLast(EmbeddingStoragePolicy<E> policy);

      void getList(List<E> out);
   };

   public static class NoEmbedding<E> implements EmbeddingStoragePolicy<E> {

      public void addLast(E edge) {
      }

      public void addFront(E edge) {
      }

      public void reverse() {
      }

      public void addFront(EmbeddingStoragePolicy<E> policy) {
      }

      public void addLast(EmbeddingStoragePolicy<E> policy) {
      }

      public void getList(List<E> out) {
      }
   };

   public static class TreeEmbedding<E> implements EmbeddingStoragePolicy<E> {

      public static class Node<E> {

         private boolean isReversed = false;
         private E data;
         private boolean hasData;
         private Node<E> left;
         private Node<E> right;

         public Node(E data) {
            this.data = data;
            this.hasData = true;
         }

         public Node(Node<E> left_child, Node<E> right_child) {
            hasData = false;
            left = left_child;
            right = right_child;
         }
      };
      private Node<E> value;

      public void addLast(E e) {
         value = new Node<E>(value, new Node<E>(e));
      }

      public void addFront(E e) {
         value = new Node<E>(new Node<E>(e), value);
      }

      public void reverse() {
         value.isReversed = !value.isReversed;
      }

      public void addFront(EmbeddingStoragePolicy<E> other) {
         value = new Node<E>(((TreeEmbedding<E>) other).value, value);
      }

      public void addLast(EmbeddingStoragePolicy<E> other) {
         value = new Node<E>(value, ((TreeEmbedding<E>) other).value);
      }

      public void getList(List<E> out) {
         getList(out, value);
      }

      private void getList(List<E> o_itr,
              Node<E> root) {
         getList(o_itr, root, false);
      }

      private void getList(List<E> o_itr,
              Node<E> root,
              boolean flipped) {
         if (root == null) {
            return;
         }

         if (root.hasData) {
            o_itr.add(root.data);
         }

         if ((flipped && !root.isReversed)
                 || (!flipped && root.isReversed)) {
            getList(o_itr, root.right, true);
            getList(o_itr, root.left, true);
         } else {
            getList(o_itr, root.left, false);
            getList(o_itr, root.right, false);
         }

      }
   };

   public static class ListEmbedding<E> implements EmbeddingStoragePolicy<E> {

      private LinkedList<E> value;

      public void addLast(E e) {
         value.addLast(e);
      }

      public void addFront(E e) {
         value.addFirst(e);
      }

      public void reverse() {
         Collections.reverse(value);
      }

      public void addFront(EmbeddingStoragePolicy<E> other) {
         value.addAll(0, ((ListEmbedding<E>) other).value);
      }

      public void addLast(EmbeddingStoragePolicy<E> other) {
         value.addAll(((ListEmbedding<E>) other).value);
      }

      public void getList(List<E> out) {
         Collections.copy(value, out);
      }
   };

   protected static class FaceHandleImpl<V, E> {

      private V cachedFirstVertex;
      private V cachedSecondVertex;
      private V trueFirstVertex;
      private V trueSecondVertex;
      private V anchor;
      private E cachedFirstEdge;
      private E cachedSecondEdge;
      private EmbeddingStoragePolicy<E> embeddingStorage;
      private OldHandlesStoragePolicy<V, E> oldHandles;

      public FaceHandleImpl(EmbeddingStoragePolicy<E> edge_list,
              OldHandlesStoragePolicy<V, E> oldHandles) {
         this.embeddingStorage = edge_list;
         this.oldHandles = oldHandles;
         oldHandles.initializeOldVertices();
      }

      public void storeOldFaceHandles()
      {
         oldHandles.storeOldFaceHandles(trueFirstVertex,
                 trueSecondVertex,
                 cachedFirstEdge,
                 cachedSecondEdge);
      }
   };
   
   private FaceHandleImpl<V, E> impl;

   public FaceHandle(EmbeddingStoragePolicy<E> embeddingStorage,
           OldHandlesStoragePolicy<V, E> oldHandles,
           V anchor) {
      impl = new FaceHandleImpl<V, E>(embeddingStorage, oldHandles);
      impl.anchor = null;
   }

   public FaceHandle(EmbeddingStoragePolicy<E> embeddingStorage,
           OldHandlesStoragePolicy<V, E> oldHandles) {
      impl = new FaceHandleImpl<V, E>(embeddingStorage, oldHandles);
      impl.anchor = null;
   }

   public FaceHandle(EmbeddingStoragePolicy<E> embeddingStorage,
           OldHandlesStoragePolicy<V, E> oldHandles,
           V anchor,
           E initialEdge,
           Graph<V, E> graph) {
      impl = new FaceHandleImpl<V, E>(embeddingStorage, oldHandles);
      impl.anchor = null;
      V source = graph.getEdgeSource(initialEdge);
      V target = graph.getEdgeTarget(initialEdge);
      V otherVertex = source.equals(anchor) ? target : source;
      impl.anchor = anchor;
      impl.cachedFirstEdge = initialEdge;
      impl.cachedSecondEdge = initialEdge;
      impl.cachedFirstVertex = otherVertex;
      impl.cachedSecondVertex = otherVertex;
      impl.trueFirstVertex = otherVertex;
      impl.trueSecondVertex = otherVertex;
      impl.embeddingStorage.addLast(initialEdge);

      impl.oldHandles.storeOldFaceHandles(impl.trueFirstVertex,
              impl.trueSecondVertex,
              impl.cachedFirstEdge,
              impl.cachedSecondEdge);
   }

   public void storeOldFaceHandles()
   {
      impl.storeOldFaceHandles();
   }

   // default copy construction, assignment okay.
   public void addFirst(E edge, Graph<V, E> graph) {
      impl.embeddingStorage.addFront(edge);
      impl.cachedFirstVertex = impl.trueFirstVertex =
              graph.getEdgeSource(edge).equals(impl.anchor)
              ? graph.getEdgeTarget(edge)
              : graph.getEdgeSource(edge);
      impl.cachedFirstEdge = edge;
   }

   public void addSecond(E edge, Graph<V, E> graph) {
      impl.embeddingStorage.addLast(edge);
      impl.cachedSecondVertex = impl.trueSecondVertex =
              graph.getEdgeSource(edge).equals(impl.anchor)
              ? graph.getEdgeTarget(edge)
              : graph.getEdgeSource(edge);
      impl.cachedSecondEdge = edge;
   }

   public V getFirstVertex() {
      return impl.cachedFirstVertex;
   }

   public V getSecondVertex() {
      return impl.cachedSecondVertex;
   }

   public V getTrueFirstVertex() {
      return impl.trueFirstVertex;
   }

   public V getTrueSecondVertex() {
      return impl.trueSecondVertex;
   }

   public V getOldFirstVertex() {
      return impl.oldHandles.getFirstVertex();
   }

   public V getOldSecondVertex() {
      return impl.oldHandles.getSecondVertex();
   }

   public E getOldFirstEdge() {
      return impl.oldHandles.getFirstEdge();
   }

   public E getOldSecondEdge() {
      return impl.oldHandles.getSecondEdge();
   }

   public E getFirstEdge() {
      return impl.cachedFirstEdge;
   }

   public E getSecondEdge() {
      return impl.cachedSecondEdge;
   }

   public V getAnchor() {
      return impl.anchor;
   }

   public void glueFirstToSecond(FaceHandle<V, E> bottom) {
      impl.embeddingStorage.addFront(bottom.impl.embeddingStorage);
      impl.trueFirstVertex = bottom.impl.trueFirstVertex;
      impl.cachedFirstVertex = bottom.impl.cachedFirstVertex;
      impl.cachedFirstEdge = bottom.impl.cachedFirstEdge;
   }

   public void glueSecondToFirst(FaceHandle<V, E> bottom) {
      impl.embeddingStorage.addLast(bottom.impl.embeddingStorage);
      impl.trueSecondVertex = bottom.impl.trueSecondVertex;
      impl.cachedSecondVertex = bottom.impl.cachedSecondVertex;
      impl.cachedSecondEdge = bottom.impl.cachedSecondEdge;
   }

   public void flip() {
      impl.embeddingStorage.reverse();
      V tmpVertex = impl.trueFirstVertex;
      impl.trueFirstVertex = impl.trueSecondVertex;
      impl.trueSecondVertex = tmpVertex;

      tmpVertex = impl.cachedFirstVertex;
      impl.cachedFirstVertex = impl.cachedSecondVertex;
      impl.cachedSecondVertex = tmpVertex;

      E tmpEdge = impl.cachedFirstEdge;
      impl.cachedFirstEdge = impl.cachedSecondEdge;
      impl.cachedSecondEdge = tmpEdge;
   }

   public void getList(List<E> o_itr) {
      impl.embeddingStorage.getList(o_itr);
   }

   public void resetVertexCache() {
      impl.cachedFirstVertex = impl.trueFirstVertex;
      impl.cachedSecondVertex = impl.trueSecondVertex;
   }

   public void setFirstVertex(V v) {
      impl.cachedFirstVertex = v;
   }

   public void setSecondVertex(V v) {
      impl.cachedSecondVertex = v;
   }

   public static class FaceIterator<V, E> {
      // Why TraversalType AND TraversalSubType? TraversalSubType is a function
      // template parameter passed in to the constructor of the face iterator,
      // whereas TraversalType is a class template parameter. This lets us decide
      // at runtime whether to move along the first or second side of a bicomp (by
      // assigning a face_iterator that has been constructed with TraversalSubType
      // = first_side or second_side to a face_iterator variable) without any of
      // the virtual function overhead that comes with implementing this
      // functionality as a more structured form of type erasure. It also allows
      // a single face_iterator to be the end iterator of two iterators traversing
      // both sides of a bicomp.

      public interface EdgeStoragePolicy<E> {

         E getValue();

         void setValue(E value);
      }

      public static class StoreEdge<E> implements EdgeStoragePolicy<E> {

         private E value;

         public E getValue() {
            return value;
         }

         public void setValue(E value) {
            this.value = value;
         }
      }

      public static class NoEdge<E> implements EdgeStoragePolicy<E> {

         public E getValue() {
            throw new UnsupportedOperationException("Edges not stored");
         }

         public void setValue(E value) {
            throw new UnsupportedOperationException("Edges not stored");
         }
      }

      // Time
      protected interface TimePolicy<V, E> {

         void setEoFirst(FaceHandle<V, E> anchorHandle, EdgeStoragePolicy<E> edgeStorage);

         void setEoSecond(FaceHandle<V, E> anchorHandle, EdgeStoragePolicy<E> edgeStorage);

         V getFirstVertex(FaceHandle<V, E> anchorHandle);

         V getSecondVertex(FaceHandle<V, E> anchorHandle);
      }

      protected static class CurrentIteration<V, E> implements TimePolicy<V, E> {

         public V getFirstVertex(FaceHandle<V, E> anchorHandle) {
            return anchorHandle.getFirstVertex();
         }

         public V getSecondVertex(FaceHandle<V, E> anchorHandle) {
            return anchorHandle.getSecondVertex();
         }

         public void setEoFirst(FaceHandle<V, E> anchorHandle, EdgeStoragePolicy<E> edgeStorage) {
            edgeStorage.setValue(anchorHandle.getFirstEdge());
         }

         public void setEoSecond(FaceHandle<V, E> anchorHandle, EdgeStoragePolicy<E> edgeStorage) {
            edgeStorage.setValue(anchorHandle.getSecondEdge());
         }
      };

      protected static class PreviousIteration<V, E> implements TimePolicy<V, E> {

         public V getFirstVertex(FaceHandle<V, E> anchorHandle) {
            return anchorHandle.getOldFirstVertex();
         }

         public V getSecondVertex(FaceHandle<V, E> anchorHandle) {
            return anchorHandle.getOldSecondVertex();
         }

         public void setEoFirst(FaceHandle<V, E> anchorHandle, EdgeStoragePolicy<E> edgeStorage) {
            edgeStorage.setValue(anchorHandle.getOldFirstEdge());
         }

         public void setEoSecond(FaceHandle<V, E> anchorHandle, EdgeStoragePolicy<E> edgeStorage) {
            edgeStorage.setValue(anchorHandle.getOldSecondEdge());
         }
      };

      // Policies for defining traversal properties
      // VisitorType
      protected interface VisitorPolicy<V, E> {

         V getVertex(FaceIteratorSingleSide<V, E> faceIteratorSingleSide);

         E getEdge(FaceIteratorSingleSide<V, E> faceIteratorSingleSide);
      }

      protected static class LeadVisitor<V, E> implements VisitorPolicy<V, E> {

         public V getVertex(FaceIteratorSingleSide<V, E> faceIteratorSingleSide) {
            return faceIteratorSingleSide.lead;
         }

         public E getEdge(FaceIteratorSingleSide<V, E> faceIteratorSingleSide) {
            return faceIteratorSingleSide.edgeStorage.getValue();
         }
      };

      protected static class FollowVisitor<V, E> implements VisitorPolicy<V, E> {

         public V getVertex(FaceIteratorSingleSide<V, E> faceIteratorSingleSide) {
            return faceIteratorSingleSide.follow;
         }

         public E getEdge(FaceIteratorSingleSide<V, E> faceIteratorSingleSide) {
            return faceIteratorSingleSide.edgeStorage.getValue();
         }
      };

      // TraversalSubType
      protected interface TraversalSubTypePolicy<V, E> {

         void setLead(FaceIteratorSingleSide<V, E> faceIteratorSingleSide, FaceHandle<V, E> anchorHandle);
      }

      protected static class FirstSide<V, E> implements TraversalSubTypePolicy<V, E> {

         public void setLead(FaceIteratorSingleSide<V, E> faceIteratorSingleSide, FaceHandle<V, E> anchorHandle) {
            faceIteratorSingleSide.lead = faceIteratorSingleSide.getFirstVertex(anchorHandle);
            faceIteratorSingleSide.setEoFirst(anchorHandle);
         }
      }; //for single_side

      protected static class SecondSide<V, E> implements TraversalSubTypePolicy<V, E> {

         public void setLead(FaceIteratorSingleSide<V, E> faceIteratorSingleSide, FaceHandle<V, E> anchorHandle) {
            faceIteratorSingleSide.lead = faceIteratorSingleSide.getSecondVertex(anchorHandle);
            faceIteratorSingleSide.setEoSecond(anchorHandle);
         }
      }; //for single_side

      //specialization for TraversalType = traverse_vertices
      protected abstract static class FaceIteratorSingleSide<V, E> {

         protected VisitorPolicy<V, E> visitorPolicy;
         protected TimePolicy<V, E> timePolicy;
         protected V lead;
         protected V follow;
         protected EdgeStoragePolicy<E> edgeStorage;
         protected Map<V, FaceHandle<V, E>> faceHandleMap;

         protected  FaceIteratorSingleSide(VisitorPolicy<V, E> visitor,
                 TimePolicy<V, E> timePolicy) {
            this.visitorPolicy = visitor;
            this.timePolicy = timePolicy;            
            this.lead = null;
            this.follow = null;
         }

         protected  FaceIteratorSingleSide(VisitorPolicy<V, E> visitorPolicy,
                 TimePolicy<V, E> timePolicy,                 
                 FaceHandle<V, E> anchorHandle,
                 Map<V, FaceHandle<V, E>> faceHandleMap,
                 TraversalSubTypePolicy<V, E> traversalSubType) {
            this.visitorPolicy = visitorPolicy;
            this.timePolicy = timePolicy;            
            this.follow = anchorHandle.getAnchor();
            this.faceHandleMap = faceHandleMap;
            traversalSubType.setLead(this, anchorHandle);
         }

         protected  FaceIteratorSingleSide(VisitorPolicy<V, E> visitorPolicy,
                 TimePolicy<V, E> timePolicy,                 
                 V anchor,
                 Map<V, FaceHandle<V, E>> faceHandleMap,
                 TraversalSubTypePolicy<V, E> traversalSubType) {
            this.visitorPolicy = visitorPolicy;
            this.timePolicy = timePolicy;            
            this.follow = anchor;
            this.faceHandleMap = faceHandleMap;
            traversalSubType.setLead(this, faceHandleMap.get(anchor));
         }
         
         @Override
         public abstract FaceIteratorSingleSide<V, E> clone();

         protected V getFirstVertex(FaceHandle<V, E> anchorHandle) {
            return timePolicy.getFirstVertex(anchorHandle);
         }

         protected V getSecondVertex(FaceHandle<V, E> anchorHandle) {
            return timePolicy.getSecondVertex(anchorHandle);
         }

         abstract protected void setEoFirst(FaceHandle<V, E> anchorHandle);

         abstract protected void setEoSecond(FaceHandle<V, E> anchorHandle);

         public void increment() {
            FaceHandle<V, E> currentFaceHandle = faceHandleMap.get(lead);
            V first = getFirstVertex(currentFaceHandle);
            V second = getSecondVertex(currentFaceHandle);
            if (first == follow) {
               follow = lead;
               setEoSecond(currentFaceHandle);
               lead = second;
            } else if (second == follow) {
               follow = lead;
               setEoFirst(currentFaceHandle);
               lead = first;
            } else {
               lead = follow = null;
            }
         }

         public boolean equals(FaceIteratorSingleSide other) {
            return lead.equals(other.lead) && follow.equals(other.follow);
         }

         public E getEdge() {
            return visitorPolicy.getEdge(this);
         }

         public V getVertex() {
            return visitorPolicy.getVertex(this);
         }

         public boolean hasNext() {
            return lead != null || follow != null;
         }

         public void remove() {
            throw new UnsupportedOperationException("Not supported yet.");
         }
      };

      protected abstract static class FaceIteratorBothSides<V, E> {

         protected FaceIteratorSingleSide<V, E> firstItr;
         protected FaceIteratorSingleSide<V, E> secondItr;
         protected boolean isFirstActive;
         protected boolean firstIncrement;

         FaceIteratorBothSides() {
         }

         public void increment() {
            if (firstIncrement) {
               firstItr.increment();
               secondItr.increment();
               firstIncrement = false;
            } else if (isFirstActive) {
               firstItr.increment();
            } else {
               secondItr.increment();
            }
            isFirstActive = !isFirstActive;
         }

         public boolean equals(FaceIteratorBothSides other) {
            //Want this iterator to be equal to the "end" iterator when at least
            //one of the iterators has reached the root of the current bicomp.
            //This isn't ideal, but it works.

            return (firstItr.equals(other.firstItr) || secondItr.equals(other.secondItr));
         }

         protected E getEdge() {
            return isFirstActive ? firstItr.getEdge() : secondItr.getEdge();
         }

         protected V getVertex() {
            return isFirstActive ? firstItr.getVertex() : secondItr.getVertex();
         }

         public boolean hasNext() {
            return (firstItr == null || !firstItr.hasNext())
                    && (secondItr == null || !secondItr.hasNext());
         }

         public void remove() {
            throw new UnsupportedOperationException("Not supported yet.");
         }
      };

      public static class FaceIteratorSingleSideVertex<V, E> extends FaceIteratorSingleSide<V, E> implements Iterator<V> {

         public FaceIteratorSingleSideVertex(VisitorPolicy<V, E> visitorPolicy,
                 TimePolicy<V, E> timePolicy) {
            super(visitorPolicy, timePolicy);
            this.edgeStorage = new NoEdge<E>();
         }

         public FaceIteratorSingleSideVertex(VisitorPolicy<V, E> visitorPolicy,
                 TimePolicy<V, E> timePolicy,
                 FaceHandle<V, E> anchorHandle,
                 Map<V, FaceHandle<V, E>> faceHandleMap,
                 TraversalSubTypePolicy<V, E> traversalSubType) {
            super(visitorPolicy, timePolicy, anchorHandle, faceHandleMap, traversalSubType);
            this.edgeStorage = new NoEdge<E>();
         }

         public FaceIteratorSingleSideVertex(VisitorPolicy<V, E> visitorPolicy,
                 TimePolicy<V, E> timePolicy,
                 V anchor,
                 Map<V, FaceHandle<V, E>> faceHandleMap,
                 TraversalSubTypePolicy<V, E> traversalSubType) {
            super(visitorPolicy, timePolicy, anchor, faceHandleMap, traversalSubType);
            this.edgeStorage = new NoEdge<E>();
         }

         protected void setEoFirst(FaceHandle<V, E> anchorHandle) {
         }

         protected void setEoSecond(FaceHandle<V, E> anchorHandle) {
         }

         @Override
         public FaceIteratorSingleSideVertex<V, E> clone()
         {
            FaceIteratorSingleSideVertex<V, E> clone = new FaceIteratorSingleSideVertex<V, E>(
                    visitorPolicy, timePolicy);
            clone.faceHandleMap = this.faceHandleMap;
            clone.follow = this.follow;
            clone.lead = this.lead;
            clone.edgeStorage = this.edgeStorage;
            return clone;
         }

         public V next() {
            V value = getVertex();
            increment();
            return value;
         }
      }

      public static class FaceIteratorSingleSideEdge<V, E> extends FaceIteratorSingleSide<V, E> implements Iterator<E> {

         public FaceIteratorSingleSideEdge(VisitorPolicy<V, E> visitorPolicy,
                 TimePolicy<V, E> timePolicy) {
            super(visitorPolicy, timePolicy);
            this.edgeStorage = new StoreEdge<E>();
         }

         public FaceIteratorSingleSideEdge(VisitorPolicy<V, E> visitorPolicy,
                 TimePolicy<V, E> timePolicy,
                 FaceHandle<V, E> anchorHandle,
                 Map<V, FaceHandle<V, E>> faceHandleMap,
                 TraversalSubTypePolicy<V, E> traversalSubType) {
            super(visitorPolicy, timePolicy, anchorHandle, faceHandleMap, traversalSubType);
            this.edgeStorage = new StoreEdge<E>();
         }

         public FaceIteratorSingleSideEdge(VisitorPolicy<V, E> visitorPolicy,
                 TimePolicy<V, E> timePolicy,                 
                 V anchor,
                 Map<V, FaceHandle<V, E>> faceHandleMap,
                 TraversalSubTypePolicy<V, E> traversalSubType) {
            super(visitorPolicy, timePolicy, anchor, faceHandleMap, traversalSubType);
            this.edgeStorage = new StoreEdge<E>();
         }

         protected void setEoFirst(FaceHandle<V, E> anchorHandle) {
            timePolicy.setEoFirst(anchorHandle, edgeStorage);
         }

         protected void setEoSecond(FaceHandle<V, E> anchorHandle) {
            timePolicy.setEoSecond(anchorHandle, edgeStorage);
         }

         @Override
         public FaceIteratorSingleSideEdge<V, E> clone()
         {
            FaceIteratorSingleSideEdge<V, E> clone = new FaceIteratorSingleSideEdge<V, E>(
                    visitorPolicy, timePolicy);
            clone.faceHandleMap = this.faceHandleMap;
            clone.follow = this.follow;
            clone.lead = this.lead;
            clone.edgeStorage = this.edgeStorage;
            return clone;
         }

         public E next() {
            E value = getEdge();
            increment();
            return value;
         }
      }

      public static class FaceIteratorBothSidesVertex<V, E> extends FaceIteratorBothSides<V, E> implements Iterator<V> {

         protected FaceIteratorBothSidesVertex() {
         }

         protected FaceIteratorBothSidesVertex(TimePolicy<V, E> timePolicy, FaceHandle<V, E> anchorHandle, Map<V, FaceHandle<V, E>> faceHandleMap) {
            firstItr = new FaceIteratorSingleSideVertex<V, E>(new FollowVisitor<V, E>(), timePolicy, anchorHandle, faceHandleMap, new FirstSide<V, E>());
            secondItr = new FaceIteratorSingleSideVertex<V, E>(new FollowVisitor<V, E>(), timePolicy, anchorHandle, faceHandleMap, new SecondSide<V, E>());
            isFirstActive = true;
            firstIncrement = true;
         }

         protected FaceIteratorBothSidesVertex(TimePolicy<V, E> timePolicy, V anchor, Map<V, FaceHandle<V, E>> faceHandleMap) {
            firstItr = new FaceIteratorSingleSideVertex<V, E>(new FollowVisitor<V, E>(), timePolicy, faceHandleMap.get(anchor), faceHandleMap, new FirstSide<V, E>());
            secondItr = new FaceIteratorSingleSideVertex<V, E>(new FollowVisitor<V, E>(), timePolicy, faceHandleMap.get(anchor), faceHandleMap, new SecondSide<V, E>());
            isFirstActive = true;
            firstIncrement = true;
         }


         @Override
         public FaceIteratorBothSidesVertex<V, E> clone()
         {
            FaceIteratorBothSidesVertex<V, E> clone = new FaceIteratorBothSidesVertex<V, E>();
            clone.firstItr = firstItr.clone();
            clone.secondItr = secondItr.clone();
            clone.isFirstActive = isFirstActive;
            clone.firstIncrement = firstIncrement;
            return clone;
         }

         public V next() {            
            V value = getVertex();
            increment();
            return value;
         }
      }

      public static class FaceIteratorBothSidesEdge<V, E> extends FaceIteratorBothSides<V, E> implements Iterator<E> {

         protected FaceIteratorBothSidesEdge() {
         }

         protected FaceIteratorBothSidesEdge(TimePolicy<V, E> timePolicy, FaceHandle<V, E> anchorHandle, Map<V, FaceHandle<V, E>> faceHandleMap) {
            firstItr = new FaceIteratorSingleSideEdge<V, E>(new FollowVisitor<V, E>(), timePolicy, anchorHandle, faceHandleMap, new FirstSide<V, E>());
            secondItr = new FaceIteratorSingleSideEdge<V, E>(new FollowVisitor<V, E>(), timePolicy, anchorHandle, faceHandleMap, new SecondSide<V, E>());
            isFirstActive = true;
            firstIncrement = true;
         }

         protected FaceIteratorBothSidesEdge(TimePolicy<V, E> timePolicy, V anchor, Map<V, FaceHandle<V, E>> faceHandleMap) {
            firstItr = new FaceIteratorSingleSideEdge<V, E>(new FollowVisitor<V, E>(), timePolicy, faceHandleMap.get(anchor), faceHandleMap, new FirstSide<V, E>());
            secondItr = new FaceIteratorSingleSideEdge<V, E>(new FollowVisitor<V, E>(), timePolicy, faceHandleMap.get(anchor), faceHandleMap, new SecondSide<V, E>());
            isFirstActive = true;
            firstIncrement = true;
         }

         @Override
         public FaceIteratorBothSidesEdge<V, E> clone()
         {
            FaceIteratorBothSidesEdge<V, E> clone = new FaceIteratorBothSidesEdge<V, E>();
            clone.firstItr = firstItr.clone();
            clone.secondItr = secondItr.clone();
            clone.isFirstActive = isFirstActive;
            clone.firstIncrement = firstIncrement;
            return clone;
         }

         public E next() {
            E value = getEdge();
            increment();
            return value;
         }
      }
   }
}

