/*
 * This file is part of the Trickl Open Source Libraries.
 *
 * Trickl Open Source Libraries - http://open.trickl.com/
 *
 * Copyright (C) 2007 Aaron Windsor (part of the C++ Boost Graph Library)
 * Copyright (C) 2011 Tim Gee (ported to Java).
 *
 * Trickl Open Source Libraries are free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Trickl Open Source Libraries are distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this project.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.trickl.graph.planar;

import com.trickl.graph.DepthFirstSearch;
import static com.trickl.graph.planar.FaceHandle.FaceIterator.*;

import java.util.List;
import java.util.ListIterator;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import org.jgrapht.Graph;

/*
 * NOTE : This class is still to be tested and is very likely to have errors.
 */
public class BoyerMyrvoldPlanarity<V, E> implements PlanarEmbedding<V, E> {

   private enum Case {

      NA,
      A,
      B,
      C,
      D,
      E
   };

   private static class Detail<V, E> {

      int lowPoint;
      V dfsParent;
      int dfsNumber;
      int leastAncestor;
      List<FaceHandle<V, E>> pertinentRoots = new Vector<FaceHandle<V, E>>();
      int backedgeFlag;
      int visited;
      FaceHandle<V, E> dfsChildHandle;
      List<V> separatedDfsChildren = new Vector<V>();
      V separatedNodeInParent;
      V canonicalDfsChild;
      boolean isFlipped;
      List<E> backedges = new Vector<E>();
      E dfsParentEdge; //only need for kuratowski
   }

   private static class MergeVertex<V> {

      V vertex;
      boolean upperPath;
      boolean lowerPath;

      public MergeVertex(V vertex, boolean upperPath, boolean lowerPath) {
         this.vertex = vertex;
         this.upperPath = upperPath;
         this.lowerPath = lowerPath;
      }
   }
   private Graph<V, E> graph;
   private PlanarDfsVisitor<V, E> componentVisitor;
   private DepthFirstSearch<V, E> depthFirstSearch;
   private V kuratowskiV;
   private V kuratowskiX;
   private V kuratowskiY;
   private List<V> garbage = new Vector<V>();
   // we delete items from linked lists by
   // splicing them into garbage
   private List<E> selfLoops = new Vector<E>();
   private List<V> verticesByDfsNumber = new Vector<V>();
   //only need these two for kuratowski subgraph isolation
   private List<V> currentMergePoints = new Vector<V>();
   private List<E> embeddedEdges = new Vector<E>();
   private Map<V, Detail<V, E>> vertexDetails;
   private Map<V, FaceHandle<V, E>> faceHandles;
   private Stack<MergeVertex<V>> mergeVertices = new Stack<MergeVertex<V>>();
   private FaceHandle.EmbeddingStoragePolicy<E> embeddingStorage;
   private FaceHandle.OldHandlesStoragePolicy<V, E> oldHandles;

   public BoyerMyrvoldPlanarity(Graph<V, E> graph,
           FaceHandle.EmbeddingStoragePolicy<E> embeddingStorage,
           FaceHandle.OldHandlesStoragePolicy<V, E> oldHandles) {
      this.graph = graph;
      this.embeddingStorage = embeddingStorage;
      this.oldHandles = oldHandles;
      this.currentMergePoints = new Vector<V>();
      depthFirstSearch = new DepthFirstSearch<V, E>(graph);
      vertexDetails = new HashMap<V, Detail<V, E>>();
      for (V v : graph.vertexSet()) {
         vertexDetails.put(v, new Detail<V, E>());
      }
      faceHandles = new HashMap<V, FaceHandle<V, E>>();

      embed();
   }

   public BoyerMyrvoldPlanarity(Graph<V, E> graph,
           FaceHandle.EmbeddingStoragePolicy<E> embeddingStorage) {
      this(graph, embeddingStorage, new FaceHandle.StoreOldHandles<V, E>());
   }

   public BoyerMyrvoldPlanarity(Graph<V, E> graph) {
      this(graph, new FaceHandle.TreeEmbedding<E>());
   }

   protected FaceHandle<V, E> createFaceHandle() {
      return new FaceHandle<V, E>(embeddingStorage, oldHandles);
   }

   protected FaceHandle<V, E> createFaceHandle(V vertex) {
      return new FaceHandle<V, E>(embeddingStorage, oldHandles, vertex);
   }

   protected FaceHandle<V, E> createFaceHandle(V vertex, E edge) {
      return new FaceHandle<V, E>(embeddingStorage, oldHandles, vertex, edge, graph);
   }

   private void lazyImpl() {
      if (componentVisitor == null) {
         this.componentVisitor = new PlanarDfsVisitor<V, E>(graph);
         depthFirstSearch.traverse(componentVisitor);
      }
   }

   private void embed() {
      lazyImpl();

      // Sort vertices by their lowpoint - need this later in the constructor
      List<V> verticesByLowPoint = new ArrayList<V>(graph.vertexSet());
      Collections.sort(verticesByLowPoint,
              new PlanarDfsVisitor.LowPointComparator<V, E>(componentVisitor.getVertexDetails()));

      // Sort vertices by their dfs number - need this to iterate by reverse 
      // DFS number in the main loop.      
      Collections.sort(verticesByDfsNumber,
              new PlanarDfsVisitor.DfsNumberComparator<V, E>(componentVisitor.getVertexDetails()));

      // Initialize face handles. A face handle is an abstraction that serves 
      // two uses in our implementation - it allows us to efficiently move 
      // along the outer face of embedded bicomps in a partially embedded 
      // graph, and it provides storage for the planar embedding. Face 
      // handles are implemented by a sequence of edges and are associated 
      // with a particular vertex - the sequence of edges represents the 
      // current embedding of edges around that vertex, and the first and 
      // last edges in the sequence represent the pair of edges on the outer 
      // face that are adjacent to the associated vertex. This lets us embed 
      // edges in the graph by just pushing them on the front or back of the 
      // sequence of edges held by the face handles.
      // 
      // Our algorithm starts with a DFS tree of edges (where every vertex is
      // an articulation point and every edge is a singleton bicomp) and 
      // repeatedly merges bicomps by embedding additional edges. Note that 
      // any bicomp at any point in the algorithm can be associated with a 
      // unique edge connecting the vertex of that bicomp with the lowest DFS
      // number (which we refer to as the "root" of the bicomp) with its DFS 
      // child in the bicomp: the existence of two such edges would contradict
      // the properties of a DFS tree. We refer to the DFS child of the root 
      // of a bicomp as the "canonical DFS child" of the bicomp. Note that a 
      // vertex can be the root of more than one bicomp.
      //
      // We move around the external faces of a bicomp using a few property 
      // maps, which we'll initialize presently:
      //
      // - face_handles: maps a vertex to a face handle that can be used to 
      //   move "up" a bicomp. For a vertex that isn't an articulation point, 
      //   this holds the face handles that can be used to move around that 
      //   vertex's unique bicomp. For a vertex that is an articulation point,
      //   this holds the face handles associated with the unique bicomp that 
      //   the vertex is NOT the root of. These handles can therefore be used 
      //   to move from any point on the outer face of the tree of bicomps 
      //   around the current outer face towards the root of the DFS tree.
      //
      // - dfs_child_handles: these are used to hold face handles for 
      //   vertices that are articulation points - dfs_child_handles[v] holds
      //   the face handles corresponding to vertex u in the bicomp with root
      //   u and canonical DFS child v.
      //
      // - canonical_dfs_child: this property map allows one to determine the
      //   canonical DFS child of a bicomp while traversing the outer face.
      //   This property map is only valid when applied to one of the two 
      //   vertices adjacent to the root of the bicomp on the outer face. To
      //   be more precise, if v is the canonical DFS child of a bicomp,
      //   canonical_dfs_child[dfs_child_handles[v].first_vertex()] == v and 
      //   canonical_dfs_child[dfs_child_handles[v].second_vertex()] == v.
      //
      // - pertinent_roots: given a vertex v, pertinent_roots[v] contains a
      //   list of face handles pointing to the top of bicomps that need to
      //   be visited by the current walkdown traversal (since they lead to
      //   backedges that need to be embedded). These lists are populated by
      //   the walkup and consumed by the walkdown.

      for (V v : graph.vertexSet()) {
         Detail<V, E> detail = vertexDetails.get(v);
         V parent = detail.dfsParent;

         if (!v.equals(parent)) {
            E parentEdge = detail.dfsParentEdge;
            addToEmbeddedEdges(parentEdge, new FaceHandle.StoreOldHandles<V, E>());

            faceHandles.put(v, createFaceHandle(v, parentEdge));
            detail.dfsChildHandle = createFaceHandle(parent, parentEdge);
         } else {
            faceHandles.put(v, createFaceHandle(v));
            detail.dfsChildHandle = createFaceHandle(parent);
         }

         detail.canonicalDfsChild = v;
         detail.pertinentRoots = new LinkedList<FaceHandle<V, E>>();
         detail.separatedDfsChildren = new LinkedList<V>();
      }

      // We need to create a list of not-yet-merged depth-first children for
      // each vertex that will be updated as bicomps get merged. We sort each 
      // list by ascending lowpoint, which allows the externally_active 
      // function to run in constant time, and we keep a pointer to each 
      // vertex's representation in its parent's list, which allows merging 
      //in constant time.

      for (V v : verticesByLowPoint) {
         Detail<V, E> detail = vertexDetails.get(v);
         V parent = detail.dfsParent;
         Detail<V, E> parentDetail = vertexDetails.get(parent);
         if (v.equals(parent)) {
            parentDetail.separatedDfsChildren.add(v);
            detail.separatedNodeInParent = v;
         }
      }

      // The merge stack holds path information during a walkdown iteration
      mergeVertices.ensureCapacity(graph.vertexSet().size());
   }

   public boolean isPlanar() {

      // This is the main algorithm: starting with a DFS tree of embedded 
      // edges (which, since it's a tree, is planar), iterate through all 
      // vertices by reverse DFS number, attempting to embed all backedges
      // connecting the current vertex to vertices with higher DFS numbers.
      // 
      // The walkup is a procedure that examines all such backedges and sets
      // up the required data structures so that they can be searched by the
      // walkdown in linear time. The walkdown does the actual work of
      // embedding edges and flipping bicomps, and can identify when it has
      // come across a kuratowski subgraph.
      //
      // store_old_face_handles caches face handles from the previous
      // iteration - this is used only for the kuratowski subgraph isolation,
      // and is therefore dispatched based on the StoreOldHandlesPolicy.
      //
      // clean_up_embedding does some clean-up and fills in values that have
      // to be computed lazily during the actual execution of the algorithm
      // (for instance, whether or not a bicomp is flipped in the final
      // embedding). It's dispatched on the the StoreEmbeddingPolicy, since
      // it's not needed if an embedding isn't desired.

      ListIterator<V> reverseItr = verticesByDfsNumber.listIterator(verticesByDfsNumber.size());
      while (reverseItr.hasPrevious()) {
         V v = reverseItr.previous();
         storeOldFaceHandles(oldHandles);

         walkup(v);

         if (!walkdown(v)) {
            return false;
         }

      }

      cleanUpEmbedding(embeddingStorage);

      return true;

   }

   private void walkup(V v) {

      // The point of the walkup is to follow all backedges from v to 
      // vertices with higher DFS numbers, and update pertinent_roots
      // for the bicomp roots on the path from backedge endpoints up
      // to v. This will set the stage for the walkdown to efficiently
      // traverse the graph of bicomps down from v.
      Detail<V, E> detail = vertexDetails.get(v);

      for (E e : graph.edgesOf(v)) {
         V edgeSource = graph.getEdgeSource(e);
         V edgeTarget = graph.getEdgeTarget(e);


         if (edgeSource.equals(edgeTarget)) {
            selfLoops.add(e);
            continue;
         }

         V w = edgeSource.equals(v) ? edgeTarget : edgeSource;

         //continue if not a back edge or already embedded         
         Detail<V, E> wDetail = vertexDetails.get(v);
         if (wDetail.dfsNumber < detail.dfsNumber || e.equals(wDetail.dfsParentEdge)) {
            continue;
         }

         wDetail.backedges.add(e);

         int timestamp = detail.dfsNumber;
         wDetail.backedgeFlag = timestamp;

         FaceIteratorBothSidesVertex<V, E> walkupItr =
                 new FaceIteratorBothSidesVertex<V, E>(
                 new CurrentIteration<V, E>(),
                 w, faceHandles);

         FaceIteratorBothSidesVertex<V, E> walkupEnd =
                 new FaceIteratorBothSidesVertex<V, E>();

         V leadVertex = w;

         while (true) {

            // Move to the root of the current bicomp or the first visited
            // vertex on the bicomp by going up each side in parallel

            while (!walkupItr.equals(walkupEnd)
                    && vertexDetails.get(walkupItr.getVertex()).visited != timestamp) {
               leadVertex = walkupItr.next();
               vertexDetails.get(leadVertex).visited = timestamp;
            }

            // If we've found the root of a bicomp through a path we haven't
            // seen before, update pertinent_roots with a handle to the
            // current bicomp. Otherwise, we've just seen a path we've been
            // up before, so break out of the main while loop.

            if (walkupItr.equals(walkupEnd)) {
               V dfsChild = vertexDetails.get(leadVertex).canonicalDfsChild;
               Detail<V, E> childDetail = vertexDetails.get(dfsChild);
               V parent = childDetail.dfsParent;
               Detail<V, E> parentDetail = vertexDetails.get(parent);

               FaceHandle<V, E> childHandle = childDetail.dfsChildHandle;
               vertexDetails.get(childHandle.getFirstVertex()).visited = timestamp;
               vertexDetails.get(childHandle.getSecondVertex()).visited = timestamp;

               if (childDetail.lowPoint < detail.dfsNumber
                       || childDetail.leastAncestor < detail.dfsNumber) {
                  parentDetail.pertinentRoots.add(childDetail.dfsChildHandle);
               } else {
                  parentDetail.pertinentRoots.add(0, childDetail.dfsChildHandle);
               }

               if (!parent.equals(v) & parentDetail.visited != timestamp) {
                  walkupItr = new FaceIteratorBothSidesVertex<V, E>(
                          new CurrentIteration<V, E>(),
                          parent, faceHandles);
                  leadVertex = parent;
               } else {
                  break;
               }
            } else {
               break;
            }
         }

      }

   }

   private boolean walkdown(V v) {
      // This procedure is where all of the action is - pertinent_roots
      // has already been set up by the walkup, so we just need to move
      // down bicomps from v until we find vertices that have been
      // labeled as backedge endpoints. Once we find such a vertex, we
      // embed the corresponding edge and glue together the bicomps on
      // the path connecting the two vertices in the edge. This may
      // involve flipping bicomps along the way.

      V w; //the other endpoint of the edge we're embedding
      Detail<V, E> detail = vertexDetails.get(v);

      while (!detail.pertinentRoots.isEmpty()) {
         FaceHandle<V, E> rootFaceHandle = detail.pertinentRoots.get(0);
         FaceHandle<V, E> currentFaceHandle = rootFaceHandle;
         detail.pertinentRoots.remove(0);

         mergeVertices.clear();

         while (true) {

            V firstSideVertex = null;
            V secondSideVertex = null;
            V firstTail, secondTail;

            firstTail = secondTail = currentFaceHandle.getAnchor();

            FaceIteratorSingleSideVertex<V, E> firstFaceItr =
                    new FaceIteratorSingleSideVertex<V, E>(
                    new LeadVisitor<V, E>(),
                    new CurrentIteration<V, E>(),
                    currentFaceHandle, faceHandles,
                    new FirstSide<V, E>());

            FaceIteratorSingleSideVertex<V, E> secondFaceItr =
                    new FaceIteratorSingleSideVertex<V, E>(
                    new LeadVisitor<V, E>(),
                    new CurrentIteration<V, E>(),
                    currentFaceHandle, faceHandles,
                    new SecondSide<V, E>());

            FaceIteratorSingleSideVertex<V, E> faceEnd =
                    new FaceIteratorSingleSideVertex<V, E>(
                    new LeadVisitor<V, E>(),
                    new CurrentIteration<V, E>());

            for (; firstFaceItr.equals(faceEnd); firstFaceItr.increment()) {
               V faceVertex = firstFaceItr.getVertex();
               if (isPertinent(faceVertex, v)
                       || isExternallyActive(faceVertex, v)) {
                  firstSideVertex = faceVertex;
                  secondSideVertex = faceVertex;
                  break;
               }
               firstTail = faceVertex;
            }

            if (firstSideVertex == null
                    || firstSideVertex == currentFaceHandle.getAnchor()) {
               break;
            }

            for (; secondFaceItr.equals(faceEnd); secondFaceItr.increment()) {
               V faceVertex = secondFaceItr.getVertex();
               if (isPertinent(faceVertex, v)
                       || isExternallyActive(faceVertex, v)) {
                  secondSideVertex = faceVertex;
                  break;
               }
               secondTail = faceVertex;
            }

            V chosen;
            boolean choseFirstUpperPath;
            if (isInternallyActive(firstSideVertex, v)) {
               chosen = firstSideVertex;
               choseFirstUpperPath = true;
            } else if (isInternallyActive(secondSideVertex, v)) {
               chosen = secondSideVertex;
               choseFirstUpperPath = false;
            } else if (isPertinent(firstSideVertex, v)) {
               chosen = firstSideVertex;
               choseFirstUpperPath = true;
            } else if (isPertinent(secondSideVertex, v)) {
               chosen = secondSideVertex;
               choseFirstUpperPath = false;
            } else {

               // If there's a pertinent vertex on the lower face
               // between the first_face_itr and the second_face_itr,
               // this graph isn't planar.
               for (;
                       firstFaceItr.getVertex() != secondSideVertex;
                       firstFaceItr.increment()) {
                  V p = firstFaceItr.getVertex();
                  if (isPertinent(p, v)) {
                     //Found a Kuratowski subgraph
                     kuratowskiV = v;
                     kuratowskiX = firstSideVertex;
                     kuratowskiY = secondSideVertex;
                     return false;
                  }
               }

               // Otherwise, the fact that we didn't find a pertinent
               // vertex on this face is fine - we should set the
               // short-circuit edges and break out of this loop to
               // start looking at a different pertinent root.

               if (firstSideVertex.equals(secondSideVertex)) {
                  if (!firstTail.equals(v)) {
                     V first = faceHandles.get(firstTail).getFirstVertex();
                     V second = faceHandles.get(firstTail).getSecondVertex();

                     V new_first_tail = first.equals(firstSideVertex)
                             ? second : first;
                     firstSideVertex = firstTail;
                     firstTail = new_first_tail;
                  } else if (!secondTail.equals(v)) {
                     V first = faceHandles.get(secondTail).getFirstVertex();
                     V second = faceHandles.get(secondTail).getSecondVertex();

                     V new_second_tail = first.equals(secondSideVertex)
                             ? second : first;
                     secondSideVertex = secondTail;
                     secondTail = new_second_tail;
                  } else {
                     break;
                  }
               }

               vertexDetails.get(firstSideVertex).canonicalDfsChild =
                       vertexDetails.get(rootFaceHandle.getFirstVertex()).canonicalDfsChild;
               vertexDetails.get(secondSideVertex).canonicalDfsChild =
                       vertexDetails.get(rootFaceHandle.getSecondVertex()).canonicalDfsChild;
               rootFaceHandle.setFirstVertex(firstSideVertex);
               rootFaceHandle.setSecondVertex(secondSideVertex);

               if (faceHandles.get(firstSideVertex).getFirstVertex().equals(firstTail)) {
                  faceHandles.get(firstSideVertex).setFirstVertex(v);
               } else {
                  faceHandles.get(firstSideVertex).setSecondVertex(v);
               }

               if (faceHandles.get(secondSideVertex).getFirstVertex().equals(secondTail)) {
                  faceHandles.get(secondSideVertex).setFirstVertex(v);
               } else {
                  faceHandles.get(secondSideVertex).setSecondVertex(v);
               }

               break;

            }


            // When we unwind the stack, we need to know which direction
            // we came down from on the top face handle

            boolean choseFirstLowerPath =
                    (choseFirstUpperPath
                    & faceHandles.get(chosen).getFirstVertex().equals(firstTail))
                    || (!choseFirstUpperPath
                    & faceHandles.get(chosen).getFirstVertex().equals(secondTail));

            //If there's a backedge at the chosen vertex, embed it now
            Detail<V, E> chosenDetail = vertexDetails.get(chosen);
            if (chosenDetail.backedgeFlag == detail.dfsNumber) {
               w = chosen;

               chosenDetail.backedgeFlag = graph.vertexSet().size();
               addToMergePoints(chosen, oldHandles);

               for (E e : chosenDetail.backedges) {
                  addToEmbeddedEdges(e, oldHandles);

                  if (choseFirstLowerPath) {
                     faceHandles.get(chosen).addFirst(e, graph);
                  } else {
                     faceHandles.get(chosen).addSecond(e, graph);
                  }
               }

            } else {
               mergeVertices.add(new MergeVertex<V>(chosen, choseFirstUpperPath, choseFirstLowerPath));
               currentFaceHandle = chosenDetail.pertinentRoots.get(0);
               continue;
            }

            //Unwind the merge stack to the root, merging all bicomps
            boolean nextBottomFollowsFirst = choseFirstUpperPath;

            V mergePoint = chosen;

            while (!mergeVertices.empty()) {

               boolean bottomPathFollowsFirst = nextBottomFollowsFirst;
               MergeVertex<V> merge = mergeVertices.pop();
               mergePoint = merge.vertex;
               Detail<V, E> mergeDetail = vertexDetails.get(mergePoint);
               nextBottomFollowsFirst = merge.upperPath;
               boolean topPathFollowsFirst = merge.lowerPath;

               FaceHandle<V, E> topHandle = faceHandles.get(mergePoint);
               FaceHandle<V, E> bottomHandle = mergeDetail.pertinentRoots.get(0);

               V bottom_dfs_child =
                       vertexDetails.get(bottomHandle.getFirstVertex()).canonicalDfsChild;
               Detail<V, E> bottomChildDetail = vertexDetails.get(bottom_dfs_child);

               removeVertexFromSeparatedDfsChildren(bottom_dfs_child);

               mergeDetail.pertinentRoots.remove(0);

               addToMergePoints(topHandle.getAnchor(), oldHandles);

               if (topPathFollowsFirst & bottomPathFollowsFirst) {
                  bottomHandle.flip();
                  topHandle.glueFirstToSecond(bottomHandle);
               } else if (!topPathFollowsFirst
                       & bottomPathFollowsFirst) {
                  bottomChildDetail.isFlipped = true;
                  topHandle.glueSecondToFirst(bottomHandle);
               } else if (topPathFollowsFirst
                       & !bottomPathFollowsFirst) {
                  bottomChildDetail.isFlipped = true;
                  topHandle.glueFirstToSecond(bottomHandle);
               } else //!top_path_follows_first & !bottom_path_follows_first
               {
                  bottomHandle.flip();
                  topHandle.glueSecondToFirst(bottomHandle);
               }

            }

            //Finally, embed all edges (v,w) at their upper end points
            vertexDetails.get(w).canonicalDfsChild =
                    vertexDetails.get(rootFaceHandle.getFirstVertex()).canonicalDfsChild;

            addToMergePoints(rootFaceHandle.getAnchor(), oldHandles);

            for (E e : chosenDetail.backedges) {
               if (nextBottomFollowsFirst) {
                  rootFaceHandle.addFirst(e, graph);
               } else {
                  rootFaceHandle.addSecond(e, graph);
               }
            }

            chosenDetail.backedges.clear();
            currentFaceHandle = rootFaceHandle;

         }//while(true)

      }//while(!pertinent_roots[v]->empty())

      return true;

   }

   private void storeOldFaceHandles(FaceHandle.OldHandlesStoragePolicy<V, E> handlesStoragePolicy) {
      if (handlesStoragePolicy instanceof FaceHandle.StoreOldHandles) {
         for (V v : currentMergePoints) {
            faceHandles.get(v).storeOldFaceHandles();
         }
         currentMergePoints.clear();
      }
   }

   private void addToMergePoints(V v,
           FaceHandle.OldHandlesStoragePolicy<V, E> handlesStoragePolicy) {
      if (handlesStoragePolicy instanceof FaceHandle.StoreOldHandles) {
         currentMergePoints.add(v);
      }
   }

   private void addToEmbeddedEdges(E e, FaceHandle.OldHandlesStoragePolicy<V, E> handlesStoragePolicy) {
      if (handlesStoragePolicy instanceof FaceHandle.StoreOldHandles) {
         embeddedEdges.add(e);
      }
   }

   private void cleanUpEmbedding(FaceHandle.EmbeddingStoragePolicy<E> embeddingStoragePolicy) {
      if (!(embeddingStoragePolicy instanceof FaceHandle.NoEmbedding)) {
         // If the graph isn't biconnected, we'll still have entries
         // in the separated_dfs_child_list for some vertices. Since
         // these represent articulation points, we can obtain a
         // planar embedding no matter what order we embed them in.

         for (V x : graph.vertexSet()) {
            Detail<V, E> detail = vertexDetails.get(x);
            if (!detail.separatedDfsChildren.isEmpty()) {
               for (V y : detail.separatedDfsChildren) {
                  Detail<V, E> yDetail = vertexDetails.get(y);
                  yDetail.dfsChildHandle.flip();
                  faceHandles.get(x).glueFirstToSecond(yDetail.dfsChildHandle);
               }
            }
         }

         // Up until this point, we've flipped bicomps lazily by setting
         // flipped[v] to true if the bicomp rooted at v was flipped (the
         // lazy aspect of this flip is that all descendents of that vertex
         // need to have their orientations reversed as well). Now, we
         // traverse the DFS tree by DFS number and perform the actual
         // flipping as needed

         for (V v : verticesByDfsNumber) {
            Detail<V, E> detail = vertexDetails.get(v);
            boolean vFlipped = detail.isFlipped;
            boolean pFlipped = vertexDetails.get(detail.dfsParent).isFlipped;
            if (vFlipped & !pFlipped) {
               faceHandles.get(v).flip();
            } else if (pFlipped & !vFlipped) {
               faceHandles.get(v).flip();
               detail.isFlipped = true;
            } else {
               detail.isFlipped = false;
            }
         }

         // If there are any self-loops in the graph, they were flagged
         // during the walkup, and we should add them to the embedding now.
         // Adding a self loop anywhere in the embedding could never
         // invalidate the embedding, but they would complicate the traversal
         // if they were added during the walkup/walkdown.
         for (E e : selfLoops) {
            faceHandles.get(graph.getEdgeSource(e)).addSecond(e, graph);
         }
      }
   }

   private boolean isPertinent(V w, V v) {
      // w is pertinent with respect to v if there is a backedge (v,w) or if
      // w is the root of a bicomp that contains a pertinent vertex.
      Detail<V, E> vDetail = vertexDetails.get(v);
      Detail<V, E> wDetail = vertexDetails.get(w);
      return wDetail.backedgeFlag == vDetail.dfsNumber || !wDetail.pertinentRoots.isEmpty();
   }

   private boolean isExternallyActive(V w, V v) {
      // Let a be any proper depth-first search ancestor of v. w is externally
      // active with respect to v if there exists a backedge (a,w) or a 
      // backedge (a,w_0) for some w_0 in a descendent bicomp of w.
      Detail<V, E> vDetail = vertexDetails.get(v);
      Detail<V, E> wDetail = vertexDetails.get(w);

      int vDfsNumber = vDetail.dfsNumber;
      return (wDetail.leastAncestor < vDfsNumber)
              || (!wDetail.separatedDfsChildren.isEmpty()
              && vertexDetails.get(wDetail.separatedDfsChildren.get(0)).lowPoint < vDfsNumber);
   }

   private boolean isInternallyActive(V w, V v) {
      return isPertinent(w, v) && !isExternallyActive(w, v);
   }

   private void removeVertexFromSeparatedDfsChildren(V v) {
      Detail<V, E> detail = vertexDetails.get(v);
      V parent = detail.dfsParent;
      Detail<V, E> parentDetail = vertexDetails.get(parent);

      garbage.add(detail.separatedNodeInParent);
      parentDetail.separatedDfsChildren.remove(detail.separatedNodeInParent);
      detail.separatedNodeInParent = null;
   }

   // End of the implementation of the basic Boyer-Myrvold Algorithm. The rest
   // of the code below implements the isolation of a Kuratowski subgraph in
   // the case that the input graph is not planar. This is by far the most
   // complicated part of the implementation.
   private V kuratowskiWalkup(V v,
           Map<E, Boolean> isforbiddenEdge,
           Map<E, Boolean> isGoalEdge,
           Map<E, Boolean> isEmbedded,
           List<E> pathEdges) {
      V currentEndPoint = null;
      boolean seenGoalEdge = false;

      for (E e : graph.edgesOf(v)) {
         isforbiddenEdge.put(e, true);
      }
      for (E e : graph.edgesOf(v)) {
         pathEdges.clear();

         currentEndPoint = graph.getEdgeTarget(e).equals(v)
                 ? graph.getEdgeSource(e)
                 : graph.getEdgeTarget(e);

         if (vertexDetails.get(currentEndPoint).dfsNumber < vertexDetails.get(v).dfsNumber
                 || isEmbedded.get(e)
                 || v.equals(currentEndPoint) //self-loop
                 ) {
            //Not a backedge
            continue;
         }

         pathEdges.add(e);
         if (isGoalEdge.get(e)) {
            return currentEndPoint;
         }

         FaceIteratorSingleSideEdge<V, E> walkupItr =
                 new FaceIteratorSingleSideEdge<V, E>(
                 new LeadVisitor<V, E>(),
                 new CurrentIteration<V, E>(),
                 currentEndPoint, faceHandles,
                 new FirstSide<V, E>());

         FaceIteratorSingleSideEdge<V, E> walkup_end =
                 new FaceIteratorSingleSideEdge<V, E>(
                 new LeadVisitor<V, E>(),
                 new CurrentIteration<V, E>());

         seenGoalEdge = false;

         while (true) {

            if (!walkupItr.equals(walkup_end) && isforbiddenEdge.get(walkupItr.getEdge())) {
               break;
            }

            while (!walkupItr.equals(walkup_end)
                    && !isGoalEdge.get(walkupItr.getEdge())
                    && !isforbiddenEdge.get(walkupItr.getEdge())) {
               E f = walkupItr.getEdge();

               isforbiddenEdge.put(f, true);
               pathEdges.add(f);
               currentEndPoint =
                       graph.getEdgeSource(f).equals(currentEndPoint)
                       ? graph.getEdgeTarget(f)
                       : graph.getEdgeSource(f);
               walkupItr.increment();
            }

            if (!walkupItr.equals(walkup_end) & isGoalEdge.get(walkupItr.getEdge())) {
               pathEdges.add(walkupItr.getEdge());
               seenGoalEdge = true;
               break;
            }

            walkupItr =
                    new FaceIteratorSingleSideEdge<V, E>(
                    new LeadVisitor<V, E>(),
                    new CurrentIteration<V, E>(),
                    currentEndPoint, faceHandles,
                    new FirstSide<V, E>());
         }

         if (seenGoalEdge) {
            break;
         }

      }

      if (seenGoalEdge) {
         return currentEndPoint;
      } else {
         return null;
      }

   }

   public void getKuratowskiSubgraph(List<E> output) {

      // If the main algorithm has failed to embed one of the back-edges from
      // a vertex v, we can use the current state of the algorithm to isolate
      // a Kuratowksi subgraph. The isolation process breaks down into five
      // cases, A - E. The general configuration of all five cases is shown in
      //                  figure 1. There is a vertex v from which the planar
      //         v        embedding process could not proceed. This means that
      //         |        there exists some bicomp containing three vertices
      //       -----      x,y, and z as shown such that x and y are externally
      //      |     |     active with respect to v (which means that there are
      //      x     y     two vertices x_0 and y_0 such that (1) both x_0 and  
      //      |     |     y_0 are proper depth-first search ancestors of v and 
      //       --z--      (2) there are two disjoint paths, one connecting x 
      //                  and x_0 and one connecting y and y_0, both consisting
      //       fig. 1     entirely of unembedded edges). Furthermore, there
      //                  exists a vertex z_0 such that z is a depth-first
      // search ancestor of z_0 and (v,z_0) is an unembedded back-edge from v.
      // x,y and z all exist on the same bicomp, which consists entirely of
      // embedded edges. The five subcases break down as follows, and are
      // handled by the algorithm logically in the order A-E: First, if v is
      // not on the same bicomp as x,y, and z, a K_3_3 can be isolated - this
      // is case A. So, we'll assume that v is on the same bicomp as x,y, and
      // z. If z_0 is on a different bicomp than x,y, and z, a K_3_3 can also
      // be isolated - this is a case B - so we'll assume from now on that v
      // is on the same bicomp as x, y, and z=z_0. In this case, one can use
      // properties of the Boyer-Myrvold algorithm to show the existence of an
      // "x-y path" connecting some vertex on the "left side" of the x,y,z
      // bicomp with some vertex on the "right side" of the bicomp (where the
      // left and right are split by a line drawn through v and z.If either of 
      // the endpoints of the x-y path is above x or y on the bicomp, a K_3_3 
      // can be isolated - this is a case C. Otherwise, both endpoints are at 
      // or below x and y on the bicomp. If there is a vertex alpha on the x-y 
      // path such that alpha is not x or y and there's a path from alpha to v
      // that's disjoint from any of the edges on the bicomp and the x-y path,
      // a K_3_3 can be isolated - this is a case D. Otherwise, properties of
      // the Boyer-Myrvold algorithm can be used to show that another vertex
      // w exists on the lower half of the bicomp such that w is externally
      // active with respect to v. w can then be used to isolate a K_5 - this
      // is the configuration of case E.


      // Clear the short-circuit edges - these are needed for the planar 
      // testing/embedding algorithm to run in linear time, but they'll 
      // complicate the kuratowski subgraph isolation
      for (V v : graph.vertexSet()) {
         faceHandles.get(v).resetVertexCache();
         vertexDetails.get(v).dfsChildHandle.resetVertexCache();
      }

      V v = kuratowskiV;
      V x = kuratowskiX;
      V y = kuratowskiY;



      Map<E, Boolean> isInSubgraph = new HashMap<E, Boolean>();
      Map<E, Boolean> isEmbedded = new HashMap<E, Boolean>();

      for (E edge : graph.edgeSet()) {
         isInSubgraph.put(edge, false);
         isEmbedded.put(edge, false);
      }

      for (E edge : embeddedEdges) {
         isEmbedded.put(edge, true);
      }

      // upper_face_vertex is true for x,y, and all vertices above x and y in 
      // the bicomp
      Map<V, Boolean> isUpperFaceVertex = new HashMap<V, Boolean>();
      Map<V, Boolean> isLowerFaceVertex = new HashMap<V, Boolean>();

      for (V vertex : graph.vertexSet()) {
         isUpperFaceVertex.put(vertex, false);
         isLowerFaceVertex.put(vertex, false);
      }

      // These next few variable declarations are all things that we need
      // to find.
      V z = null;
      V biCompRoot = null;
      V w = null;
      FaceHandle<V, E> wHandle = null;
      FaceHandle<V, E> vDfsChildHandle;
      V firstXYPathEndpoint = null;
      V secondXYPathEndpoint = null;
      V wAncestor = v;

      Case chosenCase = Case.NA;

      List<E> xExternalPath = new LinkedList<E>();
      List<E> yExternalPath = new LinkedList<E>();
      List<E> caseDEdges = new LinkedList<E>();

      Stack<E> zvPath = new Stack<E>();
      Stack<E> wPath = new Stack<E>();

      //first, use a walkup to find a path from V that starts with a
      //backedge from V, then goes up until it hits either X or Y
      //(but doesn't find X or Y as the root of a bicomp)

      FaceIteratorSingleSideVertex<V, E> xLowerItr =
              new FaceIteratorSingleSideVertex<V, E>(
              new LeadVisitor<V, E>(),
              new CurrentIteration<V, E>(),
              x, faceHandles,
              new SecondSide<V, E>());
      {
         FaceIteratorSingleSideVertex<V, E> xUpperItr =
                 new FaceIteratorSingleSideVertex<V, E>(
                 new LeadVisitor<V, E>(),
                 new CurrentIteration<V, E>(),
                 x, faceHandles,
                 new FirstSide<V, E>());

         FaceIteratorSingleSideVertex<V, E> faceEnd =
                 new FaceIteratorSingleSideVertex<V, E>(
                 new LeadVisitor<V, E>(),
                 new CurrentIteration<V, E>());

         // Don't know which path from x is the upper or lower path -
         // we'll find out here
         for (FaceIteratorSingleSideVertex<V, E> faceItr = xUpperItr; faceItr.equals(faceEnd); faceItr.increment()) {
            if (faceItr.getVertex().equals(y)) {
               FaceIteratorSingleSideVertex<V, E> tmp_itr = xUpperItr;
               xUpperItr = xLowerItr;
               xLowerItr = tmp_itr;
               break;
            }
         }

         isUpperFaceVertex.put(x, true);

         V currentVertex = x;
         V previousVertex = null;
         for (FaceIteratorSingleSideVertex<V, E> faceItr = xUpperItr; faceItr.equals(faceEnd); faceItr.increment()) {
            previousVertex = currentVertex;
            currentVertex = faceItr.getVertex();
            isUpperFaceVertex.put(currentVertex, true);
         }

         vDfsChildHandle = vertexDetails.get(vertexDetails.get(previousVertex).canonicalDfsChild).dfsChildHandle;

         for (FaceIteratorSingleSideVertex<V, E> faceItr = xLowerItr; faceItr.equals(faceEnd);) {
            for (; !faceItr.getVertex().equals(y); faceItr.increment()) {
               currentVertex = faceItr.getVertex();
               isLowerFaceVertex.put(currentVertex, true);

               if (w == null) //haven't found a w yet
               {
                  for (FaceHandle<V, E> root : vertexDetails.get(currentVertex).pertinentRoots) {
                     if (vertexDetails.get(vertexDetails.get(root.getFirstVertex()).canonicalDfsChild).lowPoint
                             < vertexDetails.get(v).dfsNumber) {
                        w = currentVertex;
                        wHandle = root;
                        break;
                     }
                  }
               }

            }

            for (; faceItr.equals(faceEnd); faceItr.increment()) {
               currentVertex = faceItr.getVertex();
               isUpperFaceVertex.put(currentVertex, true);
               biCompRoot = currentVertex;
            }
         }
      }

      Map<E, Boolean> isOuterFaceEdge = new HashMap<E, Boolean>();
      for (E e : graph.edgeSet()) {
         isOuterFaceEdge.put(e, false);
      }

      FaceIteratorSingleSideEdge<V, E> walkupItr =
              new FaceIteratorSingleSideEdge<V, E>(
              new LeadVisitor<V, E>(),
              new CurrentIteration<V, E>(),
              x, faceHandles,
              new FirstSide<V, E>());

      FaceIteratorSingleSideEdge<V, E> walkupEnd =
              new FaceIteratorSingleSideEdge<V, E>(
              new LeadVisitor<V, E>(),
              new CurrentIteration<V, E>());

      for (; !walkupItr.equals(walkupEnd); walkupItr.increment()) {
         isOuterFaceEdge.put(walkupItr.getEdge(), true);
         isInSubgraph.put(walkupItr.getEdge(), true);
      }

      walkupItr =
              new FaceIteratorSingleSideEdge<V, E>(
              new LeadVisitor<V, E>(),
              new CurrentIteration<V, E>(),
              x, faceHandles,
              new SecondSide<V, E>());

      for (; !walkupItr.equals(walkupEnd); walkupItr.increment()) {
         isOuterFaceEdge.put(walkupItr.getEdge(), true);
         isInSubgraph.put(walkupItr.getEdge(), true);
      }

      Map<E, Boolean> isForbiddenEdge = new HashMap<E, Boolean>();
      Map<E, Boolean> isGoalEdge = new HashMap<E, Boolean>();

      //Find external path to x and to y
      for (E e : graph.edgeSet()) {
         isGoalEdge.put(e,
                 !isOuterFaceEdge.get(e)
                 && (graph.getEdgeSource(e).equals(x) || graph.getEdgeTarget(e).equals(x)));
         isForbiddenEdge.put(e, isOuterFaceEdge.get(e));
      }

      V xAncestor = v;
      V xEndPoint = null;

      while (xEndPoint == null) {
         xAncestor = vertexDetails.get(xAncestor).dfsParent;
         xEndPoint = kuratowskiWalkup(xAncestor,
                 isForbiddenEdge,
                 isGoalEdge,
                 isEmbedded,
                 xExternalPath);

      }


      for (E e : graph.edgeSet()) {
         isGoalEdge.put(e,
                 !isOuterFaceEdge.get(e)
                 && (graph.getEdgeSource(e).equals(y) || graph.getEdgeTarget(e).equals(y)));
         isForbiddenEdge.put(e, isOuterFaceEdge.get(e));
      }

      V yAncestor = v;
      V yEndPoint = null;

      while (yEndPoint == null) {
         xAncestor = vertexDetails.get(yAncestor).dfsParent;
         yEndPoint = kuratowskiWalkup(yAncestor,
                 isForbiddenEdge,
                 isGoalEdge,
                 isEmbedded,
                 yExternalPath);

      }


      V parent, child;

      //If v isn't on the same bicomp as x and y, it's a case A
      if (biCompRoot.equals(v)) {
         chosenCase = Case.A;

         for (V vertex : graph.vertexSet()) {
            if (isLowerFaceVertex.get(vertex)) {
               for (E e : graph.edgesOf(vertex)) {
                  if (!isOuterFaceEdge.get(e)) {
                     isGoalEdge.put(e, true);
                  }
               }
            }
         }

         for (E e : graph.edgeSet()) {
            isForbiddenEdge.put(e, isOuterFaceEdge.get(e));
         }

         z = kuratowskiWalkup(v, isForbiddenEdge, isGoalEdge, isEmbedded, zvPath);

      } else if (w != null) {
         chosenCase = Case.B;

         for (E e : graph.edgeSet()) {
            isGoalEdge.put(e, false);
            isForbiddenEdge.put(e, isOuterFaceEdge.get(e));
         }

         isGoalEdge.put(wHandle.getFirstEdge(), true);
         isGoalEdge.put(wHandle.getSecondEdge(), true);

         z = kuratowskiWalkup(v,
                 isForbiddenEdge,
                 isGoalEdge,
                 isEmbedded,
                 zvPath);

         for (E e : graph.edgeSet()) {
            isForbiddenEdge.put(e, isOuterFaceEdge.get(e));
         }

         for (E e : zvPath) {
            isGoalEdge.put(e, true);
         }

         wAncestor = v;
         V wEndPoint = null;

         while (wEndPoint == null) {
            wAncestor = vertexDetails.get(wAncestor).dfsParent;
            wEndPoint = kuratowskiWalkup(wAncestor,
                    isForbiddenEdge,
                    isGoalEdge,
                    isEmbedded,
                    wPath);
         }

         // We really want both the w walkup and the z walkup to finish on
         // exactly the same edge, but for convenience (since we don't have
         // control over which side of a bicomp a walkup moves up) we've
         // defined the walkup to either end at w_handle.first_edge() or
         // w_handle.second_edge(). If both walkups ended at different edges,
         // we'll do a little surgery on the w walkup path to make it follow
         // the other side of the final bicomp.

         if ((wPath.peek().equals(wHandle.getFirstEdge())
                 && zvPath.peek().equals(wHandle.getSecondEdge()))
                 || (wPath.peek().equals(wHandle.getSecondEdge())
                 && zvPath.peek().equals(wHandle.getFirstEdge()))) {
            FaceIteratorSingleSideEdge<V, E> wItr = null;
            FaceIteratorSingleSideEdge<V, E> wItrEnd =
                    new FaceIteratorSingleSideEdge<V, E>(
                    new LeadVisitor<V, E>(),
                    new CurrentIteration<V, E>());

            E finalEdge = wPath.peek();
            V anchor = graph.getEdgeSource(finalEdge).equals(wHandle.getAnchor())
                    ? graph.getEdgeTarget(finalEdge)
                    : graph.getEdgeSource(finalEdge);
            if (faceHandles.get(anchor).getFirstEdge().equals(finalEdge)) {
               wItr =
                       new FaceIteratorSingleSideEdge<V, E>(
                       new LeadVisitor<V, E>(),
                       new CurrentIteration<V, E>(),
                       anchor, faceHandles,
                       new SecondSide<V, E>());
            } else {
               wItr =
                       new FaceIteratorSingleSideEdge<V, E>(
                       new LeadVisitor<V, E>(),
                       new CurrentIteration<V, E>(),
                       anchor, faceHandles,
                       new FirstSide<V, E>());
            }

            wPath.pop();

            for (; wItr.equals(wItrEnd); wItr.increment()) {
               E e = wItr.getEdge();
               if (wPath.peek().equals(e)) {
                  wPath.pop();
               } else {
                  wPath.add(e);
               }
            }
         }


      } else {

         //We need to find a valid z, since the x-y path re-defines the lower
         //face, and the z we found earlier may now be on the upper face.

         chosenCase = Case.E;

         // The z we've used so far is just an externally active vertex on the
         // lower face path, but may not be the z we need for a case C, D, or
         // E subgraph. the z we need now is any externally active vertex on
         // the lower face path with both old_face_handles edges on the outer
         // face. Since we know an x-y path exists, such a z must also exist.

         //TODO: find this z in the first place.

         //find the new z
         for (FaceIteratorSingleSideVertex<V, E> face_itr = xLowerItr; !face_itr.getVertex().equals(y); face_itr.increment()) {
            V possible_z = face_itr.getVertex();
            if (isPertinent(possible_z, v)
                    && isOuterFaceEdge.get(faceHandles.get(possible_z).getOldFirstEdge())
                    && isOuterFaceEdge.get(faceHandles.get(possible_z).getOldSecondEdge())) {
               z = possible_z;
               break;
            }
         }

         //find x-y path, and a w if one exists.

         if (isExternallyActive(z, v)) {
            w = z;
         }

         FaceIteratorSingleSideEdge<V, E> firstOldFaceItr =
                 new FaceIteratorSingleSideEdge<V, E>(
                 new LeadVisitor<V, E>(),
                 new PreviousIteration<V, E>(),
                 x, faceHandles,
                 new FirstSide<V, E>());

         FaceIteratorSingleSideEdge<V, E> secondOldFaceItr =
                 new FaceIteratorSingleSideEdge<V, E>(
                 new LeadVisitor<V, E>(),
                 new PreviousIteration<V, E>(),
                 x, faceHandles,
                 new FirstSide<V, E>());

         FaceIteratorSingleSideEdge<V, E> oldFaceEnd =
                 new FaceIteratorSingleSideEdge<V, E>(
                 new LeadVisitor<V, E>(),
                 new PreviousIteration<V, E>());

         List<FaceIteratorSingleSideEdge<V, E>> oldFaceIterators = new ArrayList<FaceIteratorSingleSideEdge<V, E>>();
         oldFaceIterators.add(firstOldFaceItr);
         oldFaceIterators.add(secondOldFaceItr);

         Map<V, Boolean> isXYPathVertex = new HashMap<V, Boolean>();
         for (V vertex : graph.vertexSet()) {
            isXYPathVertex.put(vertex, false);
         }

         for (FaceIteratorSingleSideEdge<V, E> oldFaceItr : oldFaceIterators) {
            V previousVertex;
            boolean seenXOrY = false;
            V currentVertex = z;
            for (; !oldFaceItr.equals(oldFaceEnd); oldFaceItr.increment()) {
               E e = oldFaceItr.getEdge();
               previousVertex = currentVertex;
               currentVertex = graph.getEdgeSource(e).equals(currentVertex)
                       ? graph.getEdgeTarget(e) : graph.getEdgeSource(e);

               if (currentVertex.equals(x) || currentVertex.equals(y)) {
                  seenXOrY = true;
               }

               FaceIteratorSingleSideEdge<V, E> nextItr = oldFaceItr.clone();
               nextItr.next();
               if (w == null
                       && isExternallyActive(currentVertex, v)
                       && isOuterFaceEdge.get(e)
                       && isOuterFaceEdge.get(nextItr.getEdge())
                       && !seenXOrY) {
                  w = currentVertex;
               }

               if (!isOuterFaceEdge.get(e)) {
                  if (!isUpperFaceVertex.get(currentVertex)
                          && !isLowerFaceVertex.get(currentVertex)) {
                     isXYPathVertex.put(currentVertex, true);
                  }

                  isInSubgraph.put(e, true);
                  if (isUpperFaceVertex.get(graph.getEdgeSource(e))
                          || isLowerFaceVertex.get(graph.getEdgeSource(e))) {
                     if (firstXYPathEndpoint == null) {
                        firstXYPathEndpoint = graph.getEdgeSource(e);
                     } else {
                        secondXYPathEndpoint = graph.getEdgeSource(e);
                     }
                  }
                  if (isUpperFaceVertex.get(graph.getEdgeTarget(e))
                          || isLowerFaceVertex.get(graph.getEdgeTarget(e))) {
                     if (firstXYPathEndpoint == null) {
                        firstXYPathEndpoint = graph.getEdgeTarget(e);
                     } else {
                        secondXYPathEndpoint = graph.getEdgeTarget(e);
                     }
                  }


               } else if (previousVertex.equals(x) || previousVertex.equals(y)) {
                  chosenCase = Case.C;
               }
            }
         }

         // Look for a case D - one of v's embedded edges will connect to the
         // x-y path along an inner face path.

         //First, get a list of all of v's embedded child edges
         for (E embeddedEdge : graph.edgesOf(v)) {
            if (!isEmbedded.get(embeddedEdge)
                    || embeddedEdge.equals(vertexDetails.get(v).dfsParentEdge)) {
               continue;
            }

            caseDEdges.add(embeddedEdge);

            V currentVertex = graph.getEdgeSource(embeddedEdge).equals(v)
                    ? graph.getEdgeTarget(embeddedEdge)
                    : graph.getEdgeSource(embeddedEdge);

            FaceIteratorSingleSideEdge<V, E> internalFaceItr;

            FaceIteratorSingleSideEdge<V, E> internalFaceEnd =
                    new FaceIteratorSingleSideEdge<V, E>(
                    new LeadVisitor<V, E>(),
                    new CurrentIteration<V, E>());

            if (faceHandles.get(currentVertex).getFirstVertex().equals(v)) {
               internalFaceItr = new FaceIteratorSingleSideEdge<V, E>(
                       new LeadVisitor<V, E>(),
                       new CurrentIteration<V, E>(),
                       currentVertex, faceHandles,
                       new SecondSide<V, E>());
            } else {
               internalFaceItr = new FaceIteratorSingleSideEdge<V, E>(
                       new LeadVisitor<V, E>(),
                       new CurrentIteration<V, E>(),
                       currentVertex, faceHandles,
                       new FirstSide<V, E>());
            }

            while (internalFaceItr.equals(internalFaceEnd)
                    && !isOuterFaceEdge.get(internalFaceItr.getEdge())
                    && !isXYPathVertex.get(currentVertex)) {
               E e = internalFaceItr.getEdge();
               caseDEdges.add(e);
               currentVertex = graph.getEdgeSource(e).equals(currentVertex)
                       ? graph.getEdgeTarget(e)
                       : graph.getEdgeSource(e);

               internalFaceItr.increment();
            }

            if (isXYPathVertex.get(currentVertex)) {
               chosenCase = Case.D;
               break;
            } else {
               caseDEdges.clear();
            }
         }
      }

      if (chosenCase != Case.B & chosenCase != Case.A) {

         //Finding z and w.
         for (E e : graph.edgeSet()) {
            isGoalEdge.put(e, !isOuterFaceEdge.get(e)
                    && (graph.getEdgeSource(e).equals(z)
                    || graph.getEdgeTarget(e).equals(z)));
            isForbiddenEdge.put(e, isOuterFaceEdge.get(e));
         }

         kuratowskiWalkup(v,
                 isForbiddenEdge,
                 isGoalEdge,
                 isEmbedded,
                 zvPath);

         if (chosenCase == Case.E) {
            for (E e : graph.edgeSet()) {
               isForbiddenEdge.put(e, isOuterFaceEdge.get(e));
               isGoalEdge.put(e, !isOuterFaceEdge.get(e)
                       && (graph.getEdgeSource(e).equals(w)
                       || graph.getEdgeTarget(e).equals(w)));
            }

            for (E e : graph.edgesOf(w)) {
               if (!isOuterFaceEdge.get(e)) {
                  isGoalEdge.put(e, true);
               }
            }

            for (E e : zvPath) {
               isGoalEdge.put(e, true);
            }

            wAncestor = v;
            V wEndpoint = null;

            while (wEndpoint == null) {
               wAncestor = vertexDetails.get(wAncestor).dfsParent;
               wEndpoint = kuratowskiWalkup(wAncestor,
                       isForbiddenEdge,
                       isGoalEdge,
                       isEmbedded,
                       wPath);

            }
         }
      }

      //We're done isolating the Kuratowski subgraph at this point -
      //but there's still some cleaning up to do.

      //Update is_in_subgraph with the paths we just found

      for (E e : xExternalPath) {
         isInSubgraph.put(e, true);
      }

      for (E e : yExternalPath) {
         isInSubgraph.put(e, true);
      }

      for (E e : zvPath) {
         isInSubgraph.put(e, true);
      }

      for (E e : caseDEdges) {
         isInSubgraph.put(e, true);
      }

      for (E e : wPath) {
         isInSubgraph.put(e, true);
      }

      child = biCompRoot;
      parent = vertexDetails.get(child).dfsParent;
      while (child != parent) {
         isInSubgraph.put(vertexDetails.get(child).dfsParentEdge, true);
         V tmp = parent;
         parent = vertexDetails.get(parent).dfsParent;
         child = parent;
      }

      // At this point, we've already isolated the Kuratowski subgraph and 
      // collected all of the edges that compose it in the is_in_subgraph 
      // property map. But we want the verification of such a subgraph to be 
      // a deterministic process, and we can simplify the function 
      // is_kuratowski_subgraph by cleaning up some edges here.

      if (chosenCase == Case.B) {
         isInSubgraph.put(vertexDetails.get(v).dfsParentEdge, false);
      } else if (chosenCase == Case.C) {
         // In a case C subgraph, at least one of the x-y path endpoints
         // (call it alpha) is above either x or y on the outer face. The
         // other endpoint may be attached at x or y OR above OR below. In
         // any of these three cases, we can form a K_3_3 by removing the
         // edge attached to v on the outer face that is NOT on the path to
         // alpha.

         FaceIteratorSingleSideVertex<V, E> faceItr;

         if (faceHandles.get(vDfsChildHandle.getFirstVertex()).getFirstEdge().equals(
                 vDfsChildHandle.getFirstEdge())) {
            faceItr =
                    new FaceIteratorSingleSideVertex<V, E>(
                    new FollowVisitor<V, E>(),
                    new CurrentIteration<V, E>(),
                    vDfsChildHandle.getFirstVertex(), faceHandles,
                    new SecondSide<V, E>());
         } else {
            faceItr =
                    new FaceIteratorSingleSideVertex<V, E>(
                    new FollowVisitor<V, E>(),
                    new CurrentIteration<V, E>(),
                    vDfsChildHandle.getFirstVertex(), faceHandles,
                    new FirstSide<V, E>());
         }

         for (; true; faceItr.increment()) {
            V currentVertex = faceItr.getVertex();
            if (currentVertex.equals(x) || currentVertex.equals(y)) {
               isInSubgraph.put(vDfsChildHandle.getFirstEdge(), false);
               break;
            } else if (currentVertex.equals(firstXYPathEndpoint)
                    || currentVertex.equals(secondXYPathEndpoint)) {
               isInSubgraph.put(vDfsChildHandle.getSecondEdge(), false);
               break;
            }
         }

      } else if (chosenCase == Case.D) {
         // Need to remove both of the edges adjacent to v on the outer face.
         // remove the connecting edges from v to bicomp, then
         // is_kuratowski_subgraph will shrink vertices of degree 1
         // automatically...

         isInSubgraph.put(vDfsChildHandle.getFirstEdge(), false);
         isInSubgraph.put(vDfsChildHandle.getSecondEdge(), false);

      } else if (chosenCase == Case.E) {
         // Similarly to case C, if the endpoints of the x-y path are both
         // below x and y, we should remove an edge to allow the subgraph to
         // contract to a K_3_3.

         if ((!firstXYPathEndpoint.equals(x) && !firstXYPathEndpoint.equals(y))
                 || (!secondXYPathEndpoint.equals(x) && !secondXYPathEndpoint.equals(y))) {
            isInSubgraph.put(vertexDetails.get(v).dfsParentEdge, false);

            V deletionEndPoint, otherEndPoint;
            if (isLowerFaceVertex.get(firstXYPathEndpoint)) {
               deletionEndPoint = secondXYPathEndpoint;
               otherEndPoint = firstXYPathEndpoint;
            } else {
               deletionEndPoint = firstXYPathEndpoint;
               otherEndPoint = secondXYPathEndpoint;
            }


            FaceIteratorSingleSideEdge<V, E> faceItr =
                    new FaceIteratorSingleSideEdge<V, E>(
                    new LeadVisitor<V, E>(),
                    new CurrentIteration<V, E>(),
                    deletionEndPoint, faceHandles,
                    new FirstSide<V, E>());

            FaceIteratorSingleSideEdge<V, E> faceEnd =
                    new FaceIteratorSingleSideEdge<V, E>(
                    new LeadVisitor<V, E>(),
                    new CurrentIteration<V, E>());

            boolean foundOtherEndpoint = false;
            for (; faceItr.equals(faceEnd); faceItr.increment()) {
               E e = faceItr.getEdge();
               if (graph.getEdgeSource(e).equals(otherEndPoint)
                       || graph.getEdgeTarget(e).equals(otherEndPoint)) {
                  foundOtherEndpoint = true;
                  break;
               }
            }

            if (foundOtherEndpoint) {
               isInSubgraph.put(faceHandles.get(deletionEndPoint).getFirstEdge(), false);
            } else {
               isInSubgraph.put(faceHandles.get(deletionEndPoint).getSecondEdge(), false);
            }
         }

      }

      for (E e : graph.edgeSet()) {
         if (isInSubgraph.get(e)) {
            output.add(e);
         }
      }
   }

   @Override
   public Set<E> edgesOf(V vertex) {
      List<E> vertexEmbedding = new LinkedList<E>();
      FaceHandle<V, E> faceHandle = faceHandles.get(vertex);
      if (faceHandle != null) {
         faceHandle.getList(vertexEmbedding);
      }
      return new LinkedHashSet<E>(vertexEmbedding);
   }
};
