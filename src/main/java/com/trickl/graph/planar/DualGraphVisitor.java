package com.trickl.graph.planar;

import com.trickl.graph.edges.DirectedEdge;
import java.util.HashMap;
import java.util.Map;
import org.jgrapht.EdgeFactory;
import org.jgrapht.VertexFactory;

public class DualGraphVisitor<V1, E1, V2, E2> implements PlanarFaceTraversalVisitor<V1, E1> {

   protected PlanarGraph<V1, E1> inputGraph;
   protected PlanarGraph<V2, E2> dualGraph;
   protected VertexFactory<V2> vertexFactory;
   protected EdgeFactory<V2, E2> edgeFactory;
   protected V2 dualTarget;
   private Map<DirectedEdge<V1>, V2> edgeToVertexMap;

   public DualGraphVisitor(PlanarGraph<V1, E1> inputGraph,
           PlanarGraph<V2, E2> dualGraph,
           VertexFactory<V2> vertexFactory) {
      this(inputGraph, dualGraph, vertexFactory, null);
   }

   public DualGraphVisitor(PlanarGraph<V1, E1> inputGraph,
           PlanarGraph<V2, E2> dualGraph,
           VertexFactory<V2> vertexFactory,
           EdgeFactory<V2, E2> edgeFactory) {
      this.inputGraph = inputGraph;
      this.dualGraph = dualGraph;
      this.vertexFactory = vertexFactory;
      this.edgeFactory = edgeFactory == null
              ? dualGraph.getEdgeFactory() : edgeFactory;
      this.edgeToVertexMap = new HashMap<DirectedEdge<V1>, V2>();
   }

   @Override
   public void beginFace(V1 source, V1 target) {
      dualTarget = vertexFactory.createVertex();
      dualGraph.addVertex(dualTarget);
   }

   @Override
   public void nextEdge(V1 inputSource, V1 inputTarget) {
      DirectedEdge edge = new DirectedEdge<V1>(inputSource, inputTarget);
      edgeToVertexMap.put(edge, dualTarget);

      if (edgeToVertexMap.containsKey(new DirectedEdge<V1>(inputTarget, inputSource))) {
         V2 dualSource = edgeToVertexMap.get(new DirectedEdge<V1>(inputTarget, inputSource));
         V2 dualBefore = null;
         V1 inputBefore = inputGraph.getNextVertex(inputTarget, inputSource);
         V1 inputLast = inputSource;
         V1 inputVertex = inputBefore;
         do {
            V2 dualVertex = edgeToVertexMap.get(new DirectedEdge<V1>(inputVertex, inputLast));
            if (dualVertex != null && dualGraph.containsEdge(dualVertex, dualSource)) {
               // First encountered so break on success
               dualBefore = dualVertex;
               break;
            }

            V1 temp = inputVertex;
            inputVertex = inputGraph.getNextVertex(inputLast, inputVertex);
            inputLast = temp;
         } while (!inputVertex.equals(inputBefore));

         V2 dualAfter = null;
         V1 inputAfter = inputTarget;
         inputLast = inputSource;
         inputVertex = inputAfter;
         do {
            V2 dualVertex = edgeToVertexMap.get(new DirectedEdge<V1>(inputVertex, inputLast));
            if (dualVertex != null && dualGraph.containsEdge(dualVertex, dualTarget)) {
               // Last encountered, so overwrite on success
               dualAfter = dualVertex;
            }

            V1 temp = inputVertex;
            inputVertex = inputGraph.getNextVertex(inputLast, inputVertex);
            inputLast = temp;
         } while (!inputVertex.equals(inputAfter));
         
         E2 dualEdge = edgeFactory.createEdge(dualSource, dualTarget);
         dualGraph.addEdge(dualSource, dualTarget, dualBefore, dualAfter, dualEdge);       
      }
   }

   @Override
   public void beginTraversal() {
   }

   @Override
   public void nextVertex(V1 vertex) {
   }

   @Override
   public void endFace(V1 source, V1 target) {
   }

   @Override
   public void endTraversal() {
   }

   public Map<DirectedEdge<V1>, V2> getEdgeToVertexMap() {
      return edgeToVertexMap;
   }
}
