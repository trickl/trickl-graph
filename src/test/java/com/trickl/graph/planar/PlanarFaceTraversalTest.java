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

import com.trickl.graph.edges.DirectedEdge;
import com.trickl.graph.edges.IntegerEdgeFactory;
import com.trickl.graph.planar.generate.PlanarCircleGraphGenerator;
import com.trickl.graph.vertices.IntegerVertexFactory;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;

public class PlanarFaceTraversalTest {

   private class HistoryVisitor<V, E> implements PlanarFaceTraversalVisitor<V, E> {

      private List<DirectedEdge<V>> visitedFaces;
      private List<V>               visitedVertices;
      private List<DirectedEdge<V>> visitedEdges;

      public HistoryVisitor() {
         visitedFaces = new LinkedList<DirectedEdge<V>>();
         visitedVertices = new LinkedList<V>();
         visitedEdges = new LinkedList<DirectedEdge<V>>();
      }

      @Override
      public void beginTraversal() {
      }

      @Override
      public void beginFace(V source, V target) {
         visitedFaces.add(new DirectedEdge<V>(source, target));
      }

      @Override
      public void nextEdge(V source, V target) {
         visitedEdges.add(new DirectedEdge<V>(source, target));
      }

      @Override
      public void nextVertex(V vertex) {
         visitedVertices.add(vertex);
      }

      @Override
      public void endFace(V source, V target) {
      }

      @Override
      public void endTraversal() {
      }

      public List<DirectedEdge<V>> getVisitedFaces() {
         return visitedFaces;
      }

      public List<V> getVisitedVertices() {
         return visitedVertices;
      }

      public List<DirectedEdge<V>> getVisitedEdges() {
         return visitedEdges;
      }
   }

   public PlanarFaceTraversalTest() {
   }

   @BeforeClass
   public static void setUpClass() throws Exception {
   }

   @AfterClass
   public static void tearDownClass() throws Exception {
   }

   @Test
   public void testBreadthFirstTraverse() {
      System.out.println("breadthFirstTraverse");
      PlanarGraph<Integer, Integer> graph
              = new DoublyConnectedEdgeList<Integer,
              Integer,
              Object>(new IntegerEdgeFactory(), Object.class);

      IntegerVertexFactory vertexFactory = new IntegerVertexFactory();
      PlanarCircleGraphGenerator generator =
              new PlanarCircleGraphGenerator<Integer, Integer>(7);

      generator.generateGraph(graph, vertexFactory, null);

      HistoryVisitor<Integer, Integer> historyVisitor
              = new HistoryVisitor<Integer, Integer>();
      PlanarFaceTraversal<Integer, Integer> planarFaceTraversal = new BreadthFirstPlanarFaceTraversal<Integer, Integer>(graph);
      planarFaceTraversal.traverse(historyVisitor);

      assertEquals("[1-0, 0-6, 6-1, 0-1, 1-2, 2-0, 6-0, 0-5, 5-6, 0-2, 2-3, 3-0, 5-0, 0-4, 4-5, 0-3, 3-4, 4-0, 2-1, 1-6, 6-5, 5-4, 4-3, 3-2]", Arrays.toString(historyVisitor.getVisitedEdges().toArray()));
      assertEquals("[1-0, 0-1, 6-0, 0-2, 5-0, 0-3, 2-1]", Arrays.toString(historyVisitor.getVisitedFaces().toArray()));
      assertEquals("[1, 0, 6, 0, 1, 2, 6, 0, 5, 0, 2, 3, 5, 0, 4, 0, 3, 4, 2, 1, 6, 5, 4, 3]", Arrays.toString(historyVisitor.getVisitedVertices().toArray()));
   }

   @Test
   public void testCanonicalTraverse() {
      System.out.println("canonicalTraverse");
      PlanarGraph<Integer, Integer> graph
              = new DoublyConnectedEdgeList<Integer, Integer, Object>(new IntegerEdgeFactory(), Object.class);

      IntegerVertexFactory vertexFactory = new IntegerVertexFactory();
      PlanarCircleGraphGenerator generator =
              new PlanarCircleGraphGenerator<Integer, Integer>(7);

      generator.generateGraph(graph, vertexFactory, null);

      HistoryVisitor<Integer, Integer> historyVisitor
              = new HistoryVisitor<Integer, Integer>();
      PlanarFaceTraversal<Integer, Integer> planarFaceTraversal = new CanonicalPlanarFaceTraversal<Integer, Integer>(graph);
      planarFaceTraversal.traverse(historyVisitor);

      assertEquals("[0-6, 6-1, 1-0, 5-6, 6-0, 0-5, 4-5, 5-0, 0-4, 3-4, 4-0, 0-3, 2-3, 3-0, 0-2, 2-0, 0-1, 1-2, 6-5, 5-4, 4-3, 3-2, 2-1, 1-6]", Arrays.toString(historyVisitor.getVisitedEdges().toArray()));
      assertEquals("[0-6, 5-6, 4-5, 3-4, 2-3, 2-0, 6-5]", Arrays.toString(historyVisitor.getVisitedFaces().toArray()));
      assertEquals("[0, 6, 1, 5, 6, 0, 4, 5, 0, 3, 4, 0, 2, 3, 0, 2, 0, 1, 6, 5, 4, 3, 2, 1]", Arrays.toString(historyVisitor.getVisitedVertices().toArray()));
   }
}