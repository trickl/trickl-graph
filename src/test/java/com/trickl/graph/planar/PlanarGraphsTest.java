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

import com.trickl.graph.EdgeVisitor;
import com.trickl.graph.edges.DirectedEdge;
import com.trickl.graph.edges.IntegerEdgeFactory;
import com.trickl.graph.edges.UndirectedIdEdge;
import com.trickl.graph.edges.UndirectedIdEdgeFactory;
import com.trickl.graph.ext.JComponentWindow;
import static com.trickl.graph.planar.PlanarAssert.assertEmbeddingEquals;
import com.trickl.graph.planar.faces.IdFace;
import com.trickl.graph.planar.faces.IdFaceFactory;
import com.trickl.graph.planar.generate.DelaunayGraphGenerator;
import com.trickl.graph.planar.generate.PlanarCircleGraphGenerator;
import com.trickl.graph.planar.xml.XmlDcelDocument;
import com.trickl.graph.vertices.CircleVertex;
import com.trickl.graph.vertices.IdCoordinateVertex;
import com.trickl.graph.vertices.IdCoordinateVertexFactory;
import com.trickl.graph.vertices.IntegerVertexFactory;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequenceFactory;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequenceFactory;
import java.io.*;
import java.math.MathContext;
import java.net.URL;
import java.util.*;
import javax.swing.JScrollPane;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import static org.hamcrest.CoreMatchers.is;
import org.jgraph.JGraph;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author tgee
 */
public class PlanarGraphsTest {

   public PlanarGraphsTest() {
   }


   @Test
   public void testAggregation() {
      System.out.println("aggregation");
      PlanarGraph<Integer, Integer> graph = new DoublyConnectedEdgeList<Integer, Integer, Object>(new IntegerEdgeFactory(), Object.class);
      PlanarGraph<Integer, Integer> subgraph = new DoublyConnectedEdgeList<Integer, Integer, Object>(new IntegerEdgeFactory(), Object.class);

      IntegerVertexFactory vertexFactory = new IntegerVertexFactory();
      PlanarCircleGraphGenerator generator =
              new PlanarCircleGraphGenerator<Integer, Integer>(19);
      generator.generateGraph(graph, vertexFactory, null);

      Map<Integer, Integer> aggregationGroups = new HashMap<Integer, Integer>();
      aggregationGroups.put(0, 0);
      aggregationGroups.put(1, 0);
      aggregationGroups.put(2, 1);
      aggregationGroups.put(3, 1);
      aggregationGroups.put(4, 2);
      aggregationGroups.put(5, 2);
      aggregationGroups.put(6, 3);
      aggregationGroups.put(7, 4);
      aggregationGroups.put(8, 5);
      aggregationGroups.put(9, 6);
      aggregationGroups.put(10, 7);
      aggregationGroups.put(11, 8);
      aggregationGroups.put(12, 3);
      aggregationGroups.put(13, 4);
      aggregationGroups.put(14, 5);
      aggregationGroups.put(15, 6);
      aggregationGroups.put(16, 7);
      aggregationGroups.put(17, 8);
      aggregationGroups.put(18, 9);
      
      Map<Integer, Integer> vertexMap = PlanarGraphs.aggregate(graph, subgraph, aggregationGroups);

      assertEquals(10, subgraph.vertexSet().size());
      assertEmbeddingEquals(subgraph, vertexMap.get(0), "7,6,5,2");
      assertEmbeddingEquals(subgraph, vertexMap.get(4), "6,11,10,15,2,1");
      assertEmbeddingEquals(subgraph, vertexMap.get(6), "7,18,11,5,1");
      assertEmbeddingEquals(subgraph, vertexMap.get(8), "7,2,15");
   }

   @Test
   public void testBoundaryHops() {
      System.out.println("boundaryHops");
      PlanarGraph<Integer, Integer> graph = new DoublyConnectedEdgeList<Integer, Integer, Object>(new IntegerEdgeFactory(), Object.class);

      IntegerVertexFactory vertexFactory = new IntegerVertexFactory();
      PlanarCircleGraphGenerator generator =
              new PlanarCircleGraphGenerator<Integer, Integer>(99);

      generator.generateGraph(graph, vertexFactory, null);

      Map<Integer, Integer> boundaryHops = new HashMap<Integer, Integer>();

      PlanarGraphs.boundaryHops(graph, boundaryHops);

      assertEquals(99, boundaryHops.size());
      assertEquals(5, (int) boundaryHops.get(0));
      assertEquals(0, (int) boundaryHops.get(98));
   }

   @Test
   public void testConnectedVertices() {
      System.out.println("connectedVertices");
      PlanarGraph<Integer, Integer> graph = new DoublyConnectedEdgeList<Integer, Integer, Object>(new IntegerEdgeFactory(), Object.class);

      IntegerVertexFactory vertexFactory = new IntegerVertexFactory();
      PlanarCircleGraphGenerator generator =
              new PlanarCircleGraphGenerator<Integer, Integer>(7);

      generator.generateGraph(graph, vertexFactory, null);

      assertEquals("[6, 1, 2, 3, 4, 5]", Arrays.toString(
              PlanarGraphs.getConnectedVertices(
              graph, 0).toArray()));

      assertEquals("[1, 2, 3, 4, 5, 6]", Arrays.toString(
              PlanarGraphs.getConnectedVertices(
              graph, 0,
              1).toArray()));

      assertEquals("[4, 5, 6, 1, 2, 3]", Arrays.toString(
              PlanarGraphs.getConnectedVertices(
              graph, 0,
              4).toArray()));

      assertEquals("[4, 5, 6]", Arrays.toString(
              PlanarGraphs.getConnectedVertices(
              graph, 0,
              4,
              1).toArray()));

      assertEquals("[1, 2, 3]", Arrays.toString(
              PlanarGraphs.getConnectedVertices(
              graph, 0,
              1,
              4).toArray()));
   }

   @Test
   public void testVerticesOnFace() {
      System.out.println("verticesOnFace");
      PlanarGraph<Integer, Integer> graph = new DoublyConnectedEdgeList<Integer, Integer, Object>(new IntegerEdgeFactory(), Object.class);

      IntegerVertexFactory vertexFactory = new IntegerVertexFactory();
      PlanarCircleGraphGenerator generator =
              new PlanarCircleGraphGenerator<Integer, Integer>(7);

      generator.generateGraph(graph, vertexFactory, null);

      assertEquals("[1, 6, 5, 4, 3, 2]", Arrays.toString(
              PlanarGraphs.getVerticesOnFace(
              graph, 1,
              6).toArray()));

      assertEquals("[6, 1, 0]", Arrays.toString(
              PlanarGraphs.getVerticesOnFace(
              graph, 6,
              1).toArray()));

      assertEquals("[0, 4, 5]", Arrays.toString(
              PlanarGraphs.getVerticesOnFace(
              graph, 0,
              4).toArray()));

      assertEquals("[1, 6, 5]", Arrays.toString(
              PlanarGraphs.getVerticesOnFace(
              graph, 1,
              6,
              4).toArray()));

      assertEquals("[1]", Arrays.toString(
              PlanarGraphs.getVerticesOnFace(
              graph, 1,
              6,
              6).toArray()));
   }

   @Test
   public void testSubgraphDegreeTwo() {
      System.out.println("subgraph");
      PlanarGraph<Integer, Integer> graph = new DoublyConnectedEdgeList<Integer, Integer, Object>(new IntegerEdgeFactory(), Object.class);
      PlanarGraph<Integer, Integer> subgraph = new DoublyConnectedEdgeList<Integer, Integer, Object>(new IntegerEdgeFactory(), Object.class);

      IntegerVertexFactory vertexFactory = new IntegerVertexFactory();
      PlanarCircleGraphGenerator generator =
              new PlanarCircleGraphGenerator<Integer, Integer>(7);

      generator.generateGraph(graph, vertexFactory, null);

      Set<Integer> vertices = new HashSet<Integer>();
      for (int i = 0; i <= 4; ++i) {
         vertices.add(i);
      }

      PlanarGraphs.subgraph(graph, subgraph, vertices, 2, 1);

      assertEquals(12, graph.edgeSet().size());
      assertEquals(7, subgraph.edgeSet().size());
      assertTrue(!subgraph.containsVertex(5));
      assertTrue(!subgraph.containsVertex(6));
      assertEmbeddingEquals(subgraph, 0, "4,3,2,1");
      assertEmbeddingEquals(subgraph, 1, "0,2");
      assertEmbeddingEquals(subgraph, 2, "0,3,1");
      assertEmbeddingEquals(subgraph, 3, "0,4,2");
      assertEmbeddingEquals(subgraph, 4, "3,0");
      assertEquals(5, PlanarGraphs.getBoundaryVertices(subgraph).size());
   }

   @Test
   public void testSubgraphDegreeHigherThanTwo() {
      System.out.println("subgraph");
      PlanarGraph<Integer, Integer> graph = new DoublyConnectedEdgeList<Integer, Integer, Object>(new IntegerEdgeFactory(), Object.class);
      PlanarGraph<Integer, Integer> subgraph = new DoublyConnectedEdgeList<Integer, Integer, Object>(new IntegerEdgeFactory(), Object.class);

      IntegerVertexFactory vertexFactory = new IntegerVertexFactory();
      PlanarCircleGraphGenerator generator =
              new PlanarCircleGraphGenerator<Integer, Integer>(7);

      generator.generateGraph(graph, vertexFactory, null);

      Set<Integer> vertices = new HashSet<Integer>();
      vertices.add(1);
      vertices.add(6);
      vertices.add(5);
      vertices.add(0);

      PlanarGraphs.subgraph(graph, subgraph, vertices, 1, 6);

      assertEquals(12, graph.edgeSet().size());
      assertEquals(5, subgraph.edgeSet().size());
      assertTrue(!subgraph.containsVertex(2));
      assertTrue(!subgraph.containsVertex(3));
      assertTrue(!subgraph.containsVertex(4));
      assertEmbeddingEquals(subgraph, 1, "6,0");
      assertEmbeddingEquals(subgraph, 6, "5,0,1");
      assertEmbeddingEquals(subgraph, 5, "0,6");
      assertEmbeddingEquals(subgraph, 0, "1,6,5");
      assertEquals(4, PlanarGraphs.getBoundaryVertices(subgraph).size());
   }

   @Test
   public void testDualGraph() {
      System.out.println("dualgraph");
      PlanarGraph<Integer, Integer> graph = new DoublyConnectedEdgeList<Integer, Integer, Object>(new IntegerEdgeFactory(), Object.class);
      PlanarGraph<Integer, Integer> dualGraph = new DoublyConnectedEdgeList<Integer, Integer, Object>(new IntegerEdgeFactory(), Object.class);

      IntegerVertexFactory vertexFactory = new IntegerVertexFactory();
      PlanarCircleGraphGenerator generator =
              new PlanarCircleGraphGenerator<Integer, Integer>(7);

      generator.generateGraph(graph, vertexFactory, null);

      IntegerVertexFactory dualVertexFactory = new IntegerVertexFactory();
      PlanarGraphs.dualGraph(graph, dualGraph, dualVertexFactory);

      assertEquals(12, dualGraph.edgeSet().size());
      assertEquals(3, PlanarGraphs.getBoundaryVertices(dualGraph).size());
      assertEmbeddingEquals(dualGraph, 0, "1,5,6");
      assertEmbeddingEquals(dualGraph, 1, "6,2,0");
      assertEmbeddingEquals(dualGraph, 2, "6,3,1");
      assertEmbeddingEquals(dualGraph, 3, "6,4,2");
      assertEmbeddingEquals(dualGraph, 4, "6,5,3");
      assertEmbeddingEquals(dualGraph, 5, "6,0,4");
      assertEmbeddingEquals(dualGraph, 6, "1,0,5,4,3,2");
   }

   @Test
   public void testDualGraphLarge() {
      System.out.println("dualgraphlarge");
      PlanarGraph<Integer, Integer> graph = new DoublyConnectedEdgeList<>(new IntegerEdgeFactory(), Object.class);
      PlanarGraph<Integer, Integer> dualGraph = new DoublyConnectedEdgeList<>(new IntegerEdgeFactory(), Object.class);

      IntegerVertexFactory vertexFactory = new IntegerVertexFactory();
      PlanarCircleGraphGenerator generator =
              new PlanarCircleGraphGenerator<>(37);

      generator.generateGraph(graph, vertexFactory, null);

      IntegerVertexFactory dualVertexFactory = new IntegerVertexFactory();
      PlanarGraphs.dualGraph(graph, dualGraph, dualVertexFactory);
   }

   @Test
   public void testDelaunayVoronoiMedium() throws Exception {
      System.out.println("delaunayVoronoiMedium");
      PlanarGraph<Integer, Integer> delaunayGraph = new DoublyConnectedEdgeList<>(new IntegerEdgeFactory(), Object.class);

      IntegerVertexFactory vertexFactory = new IntegerVertexFactory();
      PlanarCircleGraphGenerator<Integer, Integer> generator =
              new PlanarCircleGraphGenerator<>(37, 0.25);

      generator.generateGraph(delaunayGraph, vertexFactory, null);

      CoordinateSequenceFactory coordinateSequenceFactory = CoordinateArraySequenceFactory.instance();
      LinearRing boundary = new LinearRing(coordinateSequenceFactory.create(new Coordinate[]{
                 new Coordinate(-2, -2),
                 new Coordinate(-2, 2),
                 new Coordinate(2, 2),
                 new Coordinate(2, -2),
                 new Coordinate(-2, -2)}),
              new GeometryFactory(coordinateSequenceFactory));
      
      runDelaunayVoronoi(delaunayGraph, generator, boundary);
   }

   @Test
   public void testDelaunayVoronoiTiny() throws Exception {
      System.out.println("delaunayVoronoiTiny");
      PlanarGraph<Integer, Integer> delaunayGraph = new DoublyConnectedEdgeList<>(new IntegerEdgeFactory(), Object.class);

      IntegerVertexFactory vertexFactory = new IntegerVertexFactory();
      for (int i = 0; i < 5; ++i) {
         vertexFactory.createVertex();
      }
      delaunayGraph.addEdge(0, 1);
      delaunayGraph.addEdge(1, 2);
      delaunayGraph.addEdge(2, 0, 1, null);

      final Map<Integer, Coordinate> delaunayLocations = new HashMap<>();
      delaunayLocations.put(0, new Coordinate(-3, -2));
      delaunayLocations.put(1, new Coordinate(3, -2));
      delaunayLocations.put(2, new Coordinate(0, 2));

      PlanarLayout<Integer> delaunayLayout = (Integer vertex) -> delaunayLocations.get(vertex);
      
      CoordinateSequenceFactory coordinateSequenceFactory = CoordinateArraySequenceFactory.instance();
      LinearRing boundary = new LinearRing(coordinateSequenceFactory.create(new Coordinate[]{
                 new Coordinate(-4, -3),
                 new Coordinate(0, 3),
                 new Coordinate(4, -3),
                 new Coordinate(-4, -3)}),
              new GeometryFactory(coordinateSequenceFactory));

      MaximalPlanar<Integer, Integer> maximalPlanar = new MaximalPlanar<>();
      maximalPlanar.makeMaximalPlanar(delaunayGraph);

      runDelaunayVoronoi(delaunayGraph, delaunayLayout, boundary);
   }
   
   @Test
   public void testDelaunayVoronoiBoundaryConditions() throws Exception {
      System.out.println("delaunayVoronoiBoundaryConditions");
      PlanarGraph<Integer, Integer> delaunayGraph = new DoublyConnectedEdgeList<>(new IntegerEdgeFactory(), Object.class);

      IntegerVertexFactory  vertexFactory = new IntegerVertexFactory();
      for (int i = 0; i < 6; ++i) {         
         delaunayGraph.addVertex(vertexFactory.createVertex());
      }

      final Map<Integer, Coordinate> delaunayLocations = new HashMap<>();
      delaunayLocations.put(0, new Coordinate(-0.5, 2));
      delaunayLocations.put(1, new Coordinate(0.5, 2));
      delaunayLocations.put(2, new Coordinate(0, 1));
      delaunayLocations.put(3, new Coordinate(0, -1));
      delaunayLocations.put(4, new Coordinate(0.5, -2));
      delaunayLocations.put(5, new Coordinate(-0.5, -2));      
      
      // First generate the delaunay graph
      DelaunayGraphGenerator<Integer, Integer> delaunayGenerator =
              new DelaunayGraphGenerator<>(delaunayGraph.vertexSet(), new MapPlanarLayout(delaunayLocations));

      PlanarLayout<Integer> delaunayLayout = (Integer vertex) -> delaunayLocations.get(vertex);
      
      CoordinateSequenceFactory coordinateSequenceFactory = CoordinateArraySequenceFactory.instance();
      LinearRing boundary = new LinearRing(coordinateSequenceFactory.create(new Coordinate[]{
                 new Coordinate(3, 3),
                 new Coordinate(3, -3),
                 new Coordinate(-3, -3),
                 new Coordinate(-3, 3),
                 new Coordinate(3, 3)}),
              new GeometryFactory(coordinateSequenceFactory));

      delaunayGenerator.generateGraph(delaunayGraph, vertexFactory, null);
      runDelaunayVoronoi(delaunayGraph, delaunayLayout, boundary);
   }

   /*
   @Test
   //@Ignore("TODO: Fix")
   public void testDelaunayVoronoiLarge() throws Exception {
      System.out.println("delaunayVoronoiLarge");
      PlanarGraph<CircleVertex, UndirectedIdEdge<CircleVertex>> delaunayGraph = loadGraphFromFile("circlepacking/FilmAdjacencyGraph-100-Delaunay.xml");
      
      
      PlanarLayout<CircleVertex> delaunayLayout = new PlanarLayout<CircleVertex>() {

         @Override
         public Coordinate getCoordinate(CircleVertex vertex) {
            return vertex.getCoordinate();
         }
      };

      RadiusProvider<CircleVertex> radiusProvider = new RadiusProvider<CircleVertex>() {

         @Override
         public double getRadius(CircleVertex vertex) {
            return vertex.getRadius();
         }
      };

      List<CircleVertex> boundaryVertices = new ArrayList<CircleVertex>(PlanarGraphs.getBoundaryVertices(delaunayGraph));
      final LinearRing boundary = DelaunayVoronoiVisitor.getOffsetBoundary(boundaryVertices, delaunayLayout, radiusProvider);

      runDelaunayVoronoi(delaunayGraph, delaunayLayout, boundary);
   }
*/    
   private <V, E> Map<V, Set<DirectedEdge<IdCoordinateVertex>>>
         runDelaunayVoronoi(PlanarGraph<V, E> delaunayGraph, PlanarLayout<V> delaunayLayout, final LinearRing boundary) throws Exception {
      final DoublyConnectedEdgeList<IdCoordinateVertex, UndirectedIdEdge<IdCoordinateVertex>, IdFace> voronoiGraph
               = new DoublyConnectedEdgeList<>(new UndirectedIdEdgeFactory<>(), new IdFaceFactory());

       int coordinatePrecision = 5;
       IdCoordinateVertexFactory voronoiVertexFactory = new IdCoordinateVertexFactory(new MathContext(coordinatePrecision));

       final PlanarLayoutStore<IdCoordinateVertex> voronoiLayout = new MapPlanarLayout<>();

       Map<V, Set<DirectedEdge<IdCoordinateVertex>>> vertexToFaceMap =
               PlanarGraphs.delaunayToVoronoi(delaunayGraph, delaunayLayout, voronoiGraph, voronoiLayout, boundary, voronoiVertexFactory);
       assert (voronoiLayout.getCoordinate(voronoiVertexFactory.get(0)) != null);

       voronoiGraph.vertexSet().stream().forEach((voronoiVertex) -> {
           voronoiVertex.setCoordinate(voronoiLayout.getCoordinate(voronoiVertex));
       });

       if (Boolean.parseBoolean(System.getProperty("visualTests"))) {
           JGraph jGraph = JGraphAdaptor.getDisplayGraph(voronoiGraph, voronoiLayout);
           JComponentWindow window = new JComponentWindow(new JScrollPane(jGraph));
           window.showAndWait();
       }
       
       return vertexToFaceMap;
   }

   @Test
   public void testRemoveEdgesWithinBoundary() {
      System.out.println("removeEdgesWithinBoundary");
      PlanarGraph<Integer, Integer> graph = new DoublyConnectedEdgeList<Integer, Integer, Object>(new IntegerEdgeFactory(), Object.class);

      IntegerVertexFactory vertexFactory = new IntegerVertexFactory();
      for (int i = 0; i < 5; ++i) {
         graph.addVertex(vertexFactory.createVertex());
      }
      graph.addEdge(0, 1);
      graph.addEdge(1, 2, 0, null);
      graph.addEdge(2, 0, 1, null);
      graph.addEdge(1, 3, 0, null);
      graph.addEdge(3, 2, 1, null);
      graph.addEdge(3, 4, 1, null);
      graph.addEdge(4, 2, 3, null);

      List<Integer> boundary = new LinkedList<Integer>();
      boundary.add(0);
      boundary.add(2);
      boundary.add(4);
      boundary.add(3);
      boundary.add(1);
      EdgeVisitor<Integer> removeEdgeVisitor = null;
      PlanarGraphs.removeEdgesInsideBoundary(graph, boundary, removeEdgeVisitor);

      assertEmbeddingEquals(graph, 0, "1,2");
      assertEmbeddingEquals(graph, 1, "3,0");
      assertEmbeddingEquals(graph, 2, "0,4");
      assertEmbeddingEquals(graph, 3, "4,1");
      assertEmbeddingEquals(graph, 4, "2,3");
      assertEquals(5, graph.edgeSet().size());
   }

   @Test
   public void testRemoveEdgesWithinBoundaryCaseTwo() {
      System.out.println("removeEdgesWithinBoundaryCaseTwo");
      PlanarGraph<Integer, Integer> graph = new DoublyConnectedEdgeList<Integer, Integer, Object>(new IntegerEdgeFactory(), Object.class);

      IntegerVertexFactory vertexFactory = new IntegerVertexFactory();
      for (int i = 0; i < 8; ++i) {
         graph.addVertex(vertexFactory.createVertex());
      }
      addTriangularFace(graph, 0, 1, 7);
      addTriangularFace(graph, 7, 1, 6);
      addTriangularFace(graph, 6, 1, 5);
      addTriangularFace(graph, 5, 1, 2);
      addTriangularFace(graph, 5, 2, 4);
      addTriangularFace(graph, 4, 2, 3);

      List<Integer> boundary = new LinkedList<Integer>();
      for (int i = 0; i < 8; ++i) {
         boundary.add(i);
      }

      EdgeVisitor<Integer> removeEdgeVisitor = null;
      PlanarGraphs.removeEdgesInsideBoundary(graph, boundary, removeEdgeVisitor);

      assertEmbeddingEquals(graph, 0, "1,7");
      assertEmbeddingEquals(graph, 1, "2,0");
      assertEmbeddingEquals(graph, 2, "3,1");
      assertEmbeddingEquals(graph, 3, "4,2");
      assertEmbeddingEquals(graph, 4, "5,3");
      assertEmbeddingEquals(graph, 5, "6,4");
      assertEmbeddingEquals(graph, 6, "7,5");
      assertEmbeddingEquals(graph, 7, "0,6");
      assertEquals(8, graph.edgeSet().size());
   }

   @Test
   public void testTriangulateFace() {
      System.out.println("triangulateFace");
      PlanarGraph<Integer, Integer> graph = new DoublyConnectedEdgeList<>(new IntegerEdgeFactory(), Object.class);

      IntegerVertexFactory vertexFactory = new IntegerVertexFactory();

      // Open pentagon
      for (int i = 0; i < 5; ++i) {
         graph.addVertex(vertexFactory.createVertex());
      }
      graph.addEdge(0, 1);
      graph.addEdge(1, 2, 0, null);
      graph.addEdge(2, 3, 1, null);
      graph.addEdge(3, 4, 2, null);
      graph.addEdge(4, 0, 3, null);

      Integer source = 0;
      Integer target = 1;
      EdgeVisitor<Integer> addEdgeVisitor = null;
      PlanarGraphs.triangulateFace(graph, source, target, addEdgeVisitor);

      assertEquals(7, graph.edgeSet().size());
      assertEmbeddingEquals(graph, 0, "4,3,2,1");
      assertEmbeddingEquals(graph, 1, "2,0");
      assertEmbeddingEquals(graph, 2, "3,1,0");
      assertEmbeddingEquals(graph, 3, "4,2,0");
      assertEmbeddingEquals(graph, 4, "0,3");
   }

   @Test
   public void testTriangulateFaceCaseTwo() {
      System.out.println("triangulateFace");
      PlanarGraph<Integer, Integer> graph = new DoublyConnectedEdgeList<>(new IntegerEdgeFactory(), Object.class);

      IntegerVertexFactory vertexFactory = new IntegerVertexFactory();

      // Open pentagon
      for (int i = 0; i < 6; ++i) {
         graph.addVertex(vertexFactory.createVertex());
      }
      graph.addEdge(0, 1);
      graph.addEdge(1, 2, 0, null);
      graph.addEdge(2, 3, 1, null);
      graph.addEdge(3, 4, 2, null);
      graph.addEdge(4, 5, 3, null);
      graph.addEdge(5, 0, 4, null);
      graph.addEdge(0, 1, 5, null);
      graph.addEdge(0, 2, 1, null);

      Integer source = 0;
      Integer target = 1;
      EdgeVisitor<Integer> addEdgeVisitor = null;
      PlanarGraphs.triangulateFace(graph, source, target, addEdgeVisitor);

      assertEmbeddingEquals(graph, 0, "5,4,3,1,2");
      assertEmbeddingEquals(graph, 1, "0,3,2");
      assertEmbeddingEquals(graph, 2, "0,1,3");
      assertEmbeddingEquals(graph, 3, "2,1,0,4");
      assertEmbeddingEquals(graph, 4, "3,0,5");
      assertEmbeddingEquals(graph, 5, "4,0");
      assertEquals(10, graph.edgeSet().size());
   }
   
   @Test
   public void testInnermostVertices() {
       System.out.println("innermostVertices");
       PlanarGraph<Integer, Integer> graph = new DoublyConnectedEdgeList<>(new IntegerEdgeFactory(), Object.class);

        IntegerVertexFactory vertexFactory = new IntegerVertexFactory();
        PlanarCircleGraphGenerator generator =
                new PlanarCircleGraphGenerator<>(19);
        generator.generateGraph(graph, vertexFactory, null);
        
        List<Integer> innermostVertices = PlanarGraphs.getInnermostVertices(graph);
        assertThat(innermostVertices, is(Arrays.asList(0)));        
   }

   private <V, E> void addTriangularFace(PlanarGraph<V, E> graph, V firstVertex, V secondVertex, V thirdVertex) {
      graph.addEdge(firstVertex, secondVertex);
      graph.addEdge(secondVertex, thirdVertex, firstVertex, null);
      graph.addEdge(thirdVertex, firstVertex, secondVertex, secondVertex);
   }

   // TODO This code is duplicated in several tests - needs to be consolidated
   private <V, E> void writeGraphToFile(PlanarGraph<V, E> graph, String fileName) throws FileNotFoundException, IOException, JAXBException {
      XmlDcelDocument<V, E, IdFace> dcelDocument =
              new XmlDcelDocument<V, E, IdFace>(graph, new IdFaceFactory());

      String packagePath = this.getClass().getPackage().getName().replaceAll("\\.", "/");
      File outputFile = new File("src/test/resources/"
              + packagePath
              + "/" + fileName);
      outputFile.createNewFile();
      OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(outputFile));
      JAXBContext context = JAXBContext.newInstance(XmlDcelDocument.class,
              CircleVertex.class,
              Integer.class,
              IdCoordinateVertex.class,
              UndirectedIdEdge.class,
              UndirectedIdEdgeFactory.class,
              IdFace.class,
              IdFaceFactory.class);
      dcelDocument.write(writer, context);
   }

   private PlanarGraph<CircleVertex, UndirectedIdEdge<CircleVertex>> loadGraphFromFile(String file) throws IOException, JAXBException {
      URL controlFile = this.getClass().getResource(file);
      InputStreamReader reader = new InputStreamReader(controlFile.openStream());

      JAXBContext context = JAXBContext.newInstance(XmlDcelDocument.class,
              CircleVertex.class,
              UndirectedIdEdge.class,
              UndirectedIdEdgeFactory.class,
              IdFace.class,
              IdFaceFactory.class);
      Unmarshaller unmarshaller = context.createUnmarshaller();
      XmlDcelDocument<CircleVertex, UndirectedIdEdge<CircleVertex>, IdFace> document =
              (XmlDcelDocument<CircleVertex, UndirectedIdEdge<CircleVertex>, IdFace>) unmarshaller.unmarshal(reader);
      return document.getDoublyConnectedEdgeList();
   }
}
