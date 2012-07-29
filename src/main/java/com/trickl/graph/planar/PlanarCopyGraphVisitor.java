/*
 * This file is part of the Trickl Open Source Libraries.
 *
 * Trickl Open Source Libraries - http://open.trickl.com/
 *
 * Copyright (C) 2011 Tim Gee.
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

import com.trickl.graph.CopyEdgeFactory;
import com.trickl.graph.CopyVertexFactory;
import com.trickl.graph.edges.DirectedEdge;
import java.util.HashMap;
import java.util.Map;

public class PlanarCopyGraphVisitor<V1, E1, V2, E2> extends AbstractPlanarFaceTraversalVisitor<V1, E1> {

   protected PlanarGraph<V1, E1> inputGraph;
   protected PlanarGraph<V2, E2> outputGraph;
   protected CopyVertexFactory<V2, V1> vertexFactory;
   protected CopyEdgeFactory<V2, E2, E1> edgeFactory;
   private Map<V1, V2> vertexMap;
   private Map<V1, Integer> aggregationGroups;
   private Map<Integer, V2> aggregationVertices;

   public PlanarCopyGraphVisitor(PlanarGraph<V1, E1> inputGraph,
           PlanarGraph<V2, E2> dualGraph) {
      this(inputGraph, dualGraph, null);
   }

   public PlanarCopyGraphVisitor(PlanarGraph<V1, E1> inputGraph,
           PlanarGraph<V2, E2> dualGraph,
           CopyVertexFactory<V2, V1> vertexFactory) {
      this(inputGraph, dualGraph, vertexFactory, null);
   }

   public PlanarCopyGraphVisitor(PlanarGraph<V1, E1> inputGraph,
           PlanarGraph<V2, E2> outputGraph,
           CopyVertexFactory<V2, V1> vertexFactory,
           CopyEdgeFactory<V2, E2, E1> edgeFactory) {
      this.inputGraph = inputGraph;
      this.outputGraph = outputGraph;
      this.vertexFactory = vertexFactory;
      this.edgeFactory = edgeFactory;
      this.vertexMap = new HashMap<V1, V2>();
      this.aggregationVertices= new HashMap<Integer, V2>();
   }

   @Override
   public void nextEdge(V1 inputSource, V1 inputTarget) {
      V2 outputSource = vertexMap.get(inputSource);
      V2 outputTarget = vertexMap.get(inputTarget);

      // Find the before and after vertices in the planar empedding (if this is not
      // a self-loop).
      V2 outputBefore = null;
      V2 outputAfter = null;
      if (!outputSource.equals(outputTarget))
      {
         for (V1 vertex : PlanarGraphs.getConnectedVertices(inputGraph, inputSource, inputGraph.getPrevVertex(inputSource, inputTarget), inputTarget)) {
            V2 outputVertex = vertexMap.get(vertex);
            if (outputVertex != null && outputGraph.containsEdge(outputVertex, outputSource)) {
               // First encountered so break on success
               outputBefore = outputVertex;
               break;
            }
         }

         for (V1 vertex : PlanarGraphs.getConnectedVertices(inputGraph, inputTarget, inputSource)) {
            V2 outputVertex = vertexMap.get(vertex);
            if (outputVertex != null && outputGraph.containsEdge(outputTarget, outputVertex)) {
               // Last encountered, so overwrite on success
               outputAfter = outputVertex;
            }
         }
      }

      // Don't create output self-loops, unless the self-loop existed
      // in the input graph. This allows us to create aggregate graphs without
      // self-loops for aggregated nodes.
      if (!outputSource.equals(outputTarget) || inputSource.equals(inputTarget)) {
         E2 edge = null;
         E1 inputEdge = inputGraph.getEdge(inputSource, inputTarget);
         if (edgeFactory != null) {
            edge = edgeFactory.createEdge(outputSource, outputTarget, inputEdge);
         } else {
            edge = (E2) inputEdge;
         }

         outputGraph.addEdge(outputSource, outputTarget, outputBefore, outputAfter, edge);
      }
   }

   @Override
   public void nextVertex(V1 inputVertex) {
      if (!vertexMap.containsKey(inputVertex)) {
         V2 outputVertex = null;
         Integer group = aggregationGroups == null
                 ? null : aggregationGroups.get(inputVertex);
         if (group != null) {
            outputVertex = aggregationVertices.get(group);
         }

         if (outputVertex == null) {
            // A new vertex in the output graph
            if (vertexFactory != null) {
               // Use the vertex factory to create new vertices
               outputVertex = vertexFactory.createVertex(inputVertex);
            } else {
               // Can copy vertex straight over, but required V1 == V2
               outputVertex = (V2) inputVertex;
            }

            outputGraph.addVertex(outputVertex);

            if (group != null) {
               // This vertex will now represent the group
               aggregationVertices.put(group, outputVertex);
            }
         }

         vertexMap.put(inputVertex, outputVertex);
      }
   }

   @Override
   public void endTraversal() {
      // Depending on the face traversal method, it cannot be guaranteed
      // that the boundary face was maintained after the edge copying
      // so we set it here.
      DirectedEdge<V1> inputBoundary = inputGraph.getBoundary();
      // Need to test for null as inputGraph could have zero vertices.
      if (inputBoundary != null) {
         V2 outputBoundarySource = vertexMap.get(inputBoundary.getSource());
         V2 outputBoundaryTarget = vertexMap.get(inputBoundary.getTarget());
         if (outputGraph.containsEdge(outputBoundarySource, outputBoundaryTarget)) {
            outputGraph.setBoundary(outputBoundarySource, outputBoundaryTarget);
         }
      }
   }

   public Map<V1, V2> getVertexMap() {
      return vertexMap;
   }

   /**
    * Optional aggregation of input graph vertices. Putting multiple input
    * graph vertices in the same group will mean they are represented by
    * a single vertex in the output graph
    * @return
    */
   public Map<V1, Integer> getAggregationGroups() {
      return aggregationGroups;
   }

   public void setAggregationGroups(Map<V1, Integer> aggregationGroups) {
      this.aggregationGroups = aggregationGroups;
   }
}
