/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.trickl.graph.planar;

import com.trickl.graph.EdgeVisitor;
import com.trickl.graph.edges.UndirectedIdEdge;
import com.trickl.graph.edges.UndirectedIdEdgeFactory;
import com.trickl.graph.planar.DoublyConnectedEdgeList;
import com.trickl.graph.planar.MaximalPlanar;
import com.trickl.graph.planar.PlanarGraph;
import com.trickl.graph.planar.PlanarGraphs;
import com.trickl.graph.planar.PlanarLayout;
import com.trickl.graph.generate.PlanarCircleGraphGenerator;
import static com.trickl.graph.planar.PlanarAssert.assertEmbeddingEquals;
import com.trickl.graph.planar.faces.IdFace;
import com.trickl.graph.planar.faces.IdFaceFactory;
import com.trickl.graph.planar.xml.XmlDcelDocument;
import com.trickl.graph.vertices.*;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequenceFactory;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequenceFactory;
import java.io.*;
import java.math.MathContext;
import java.net.URL;
import java.util.*;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
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
      PlanarGraph<IdVertex, UndirectedIdEdge<IdVertex>> graph = new DoublyConnectedEdgeList<IdVertex, UndirectedIdEdge<IdVertex>, Object>(new UndirectedIdEdgeFactory<IdVertex>(), Object.class);
      PlanarGraph<IdVertex, UndirectedIdEdge<IdVertex>> subgraph = new DoublyConnectedEdgeList<IdVertex, UndirectedIdEdge<IdVertex>, Object>(new UndirectedIdEdgeFactory<IdVertex>(), Object.class);

      IdVertexFactory vertexFactory = new IdVertexFactory();
      PlanarCircleGraphGenerator generator =
              new PlanarCircleGraphGenerator<IdVertex, UndirectedIdEdge<IdVertex>>(19);
      generator.generateGraph(graph, vertexFactory, null);

      Map<IdVertex, Integer> aggregationGroups = new HashMap<IdVertex, Integer>();
      aggregationGroups.put(vertexFactory.get(0), 0);
      aggregationGroups.put(vertexFactory.get(1), 0);
      aggregationGroups.put(vertexFactory.get(2), 1);
      aggregationGroups.put(vertexFactory.get(3), 1);
      aggregationGroups.put(vertexFactory.get(4), 2);
      aggregationGroups.put(vertexFactory.get(5), 2);
      aggregationGroups.put(vertexFactory.get(6), 3);
      aggregationGroups.put(vertexFactory.get(7), 3);
      aggregationGroups.put(vertexFactory.get(8), 4);
      aggregationGroups.put(vertexFactory.get(9), 5);
      aggregationGroups.put(vertexFactory.get(10), 6);
      aggregationGroups.put(vertexFactory.get(11), 7);
      aggregationGroups.put(vertexFactory.get(12), 8);
      aggregationGroups.put(vertexFactory.get(13), 9);
      aggregationGroups.put(vertexFactory.get(14), 4);
      aggregationGroups.put(vertexFactory.get(15), 5);
      aggregationGroups.put(vertexFactory.get(16), 6);
      aggregationGroups.put(vertexFactory.get(17), 7);
      aggregationGroups.put(vertexFactory.get(18), 8);
      
      Map<IdVertex, IdVertex> vertexMap = PlanarGraphs.aggregate(graph, subgraph, aggregationGroups);

      assertEquals(10, subgraph.vertexSet().size());
      assertEmbeddingEquals(subgraph, vertexMap.get(vertexFactory.get(0)), "5,2,8,13,7");
      assertEmbeddingEquals(subgraph, vertexMap.get(vertexFactory.get(4)), "10,2,1,7,18,17");
      assertEmbeddingEquals(subgraph, vertexMap.get(vertexFactory.get(6)), "1,13,18,5");
      assertEmbeddingEquals(subgraph, vertexMap.get(vertexFactory.get(8)), "13,1,2,9");
   }

   @Test
   public void testBoundaryHops() {
      System.out.println("boundaryHops");
      PlanarGraph<IdVertex, UndirectedIdEdge<IdVertex>> graph = new DoublyConnectedEdgeList<IdVertex, UndirectedIdEdge<IdVertex>, Object>(new UndirectedIdEdgeFactory<IdVertex>(), Object.class);

      IdVertexFactory vertexFactory = new IdVertexFactory();
      PlanarCircleGraphGenerator generator =
              new PlanarCircleGraphGenerator<IdVertex, UndirectedIdEdge<IdVertex>>(99);

      generator.generateGraph(graph, vertexFactory, null);

      Map<IdVertex, Integer> boundaryHops = new HashMap<IdVertex, Integer>();

      PlanarGraphs.boundaryHops(graph, boundaryHops);

      assertEquals(99, boundaryHops.size());
      assertEquals(5, (int) boundaryHops.get(vertexFactory.get(0)));
      assertEquals(0, (int) boundaryHops.get(vertexFactory.get(98)));
   }

   @Test
   public void testConnectedVertices() {
      System.out.println("connectedVertices");
      PlanarGraph<IdVertex, UndirectedIdEdge<IdVertex>> graph = new DoublyConnectedEdgeList<IdVertex, UndirectedIdEdge<IdVertex>, Object>(new UndirectedIdEdgeFactory<IdVertex>(), Object.class);

      IdVertexFactory vertexFactory = new IdVertexFactory();
      PlanarCircleGraphGenerator generator =
              new PlanarCircleGraphGenerator<IdVertex, UndirectedIdEdge<IdVertex>>(7);

      generator.generateGraph(graph, vertexFactory, null);

      assertEquals("[6, 1, 2, 3, 4, 5]", Arrays.toString(
              PlanarGraphs.getConnectedVertices(
              graph, vertexFactory.get(0)).toArray()));

      assertEquals("[1, 2, 3, 4, 5, 6]", Arrays.toString(
              PlanarGraphs.getConnectedVertices(
              graph, vertexFactory.get(0),
              vertexFactory.get(1)).toArray()));

      assertEquals("[4, 5, 6, 1, 2, 3]", Arrays.toString(
              PlanarGraphs.getConnectedVertices(
              graph, vertexFactory.get(0),
              vertexFactory.get(4)).toArray()));

      assertEquals("[4, 5, 6]", Arrays.toString(
              PlanarGraphs.getConnectedVertices(
              graph, vertexFactory.get(0),
              vertexFactory.get(4),
              vertexFactory.get(1)).toArray()));

      assertEquals("[1, 2, 3]", Arrays.toString(
              PlanarGraphs.getConnectedVertices(
              graph, vertexFactory.get(0),
              vertexFactory.get(1),
              vertexFactory.get(4)).toArray()));
   }

   @Test
   public void testVerticesOnFace() {
      System.out.println("verticesOnFace");
      PlanarGraph<IdVertex, UndirectedIdEdge<IdVertex>> graph = new DoublyConnectedEdgeList<IdVertex, UndirectedIdEdge<IdVertex>, Object>(new UndirectedIdEdgeFactory<IdVertex>(), Object.class);

      IdVertexFactory vertexFactory = new IdVertexFactory();
      PlanarCircleGraphGenerator generator =
              new PlanarCircleGraphGenerator<IdVertex, UndirectedIdEdge<IdVertex>>(7);

      generator.generateGraph(graph, vertexFactory, null);

      assertEquals("[1, 6, 5, 4, 3, 2]", Arrays.toString(
              PlanarGraphs.getVerticesOnFace(
              graph, vertexFactory.get(1),
              vertexFactory.get(6)).toArray()));

      assertEquals("[6, 1, 0]", Arrays.toString(
              PlanarGraphs.getVerticesOnFace(
              graph, vertexFactory.get(6),
              vertexFactory.get(1)).toArray()));

      assertEquals("[0, 4, 5]", Arrays.toString(
              PlanarGraphs.getVerticesOnFace(
              graph, vertexFactory.get(0),
              vertexFactory.get(4)).toArray()));

      assertEquals("[1, 6, 5]", Arrays.toString(
              PlanarGraphs.getVerticesOnFace(
              graph, vertexFactory.get(1),
              vertexFactory.get(6),
              vertexFactory.get(4)).toArray()));

      assertEquals("[1]", Arrays.toString(
              PlanarGraphs.getVerticesOnFace(
              graph, vertexFactory.get(1),
              vertexFactory.get(6),
              vertexFactory.get(6)).toArray()));
   }

   @Test
   public void testSubgraphDegreeTwo() {
      System.out.println("subgraph");
      PlanarGraph<IdVertex, UndirectedIdEdge<IdVertex>> graph = new DoublyConnectedEdgeList<IdVertex, UndirectedIdEdge<IdVertex>, Object>(new UndirectedIdEdgeFactory<IdVertex>(), Object.class);
      PlanarGraph<IdVertex, UndirectedIdEdge<IdVertex>> subgraph = new DoublyConnectedEdgeList<IdVertex, UndirectedIdEdge<IdVertex>, Object>(new UndirectedIdEdgeFactory<IdVertex>(), Object.class);

      IdVertexFactory vertexFactory = new IdVertexFactory();
      PlanarCircleGraphGenerator generator =
              new PlanarCircleGraphGenerator<IdVertex, UndirectedIdEdge<IdVertex>>(7);

      generator.generateGraph(graph, vertexFactory, null);

      Set<IdVertex> vertices = new HashSet<IdVertex>();
      for (int i = 0; i <= 4; ++i) {
         vertices.add(vertexFactory.get(i));
      }

      PlanarGraphs.subgraph(graph, subgraph, vertices, vertexFactory.get(2), vertexFactory.get(1));

      assertEquals(12, graph.edgeSet().size());
      assertEquals(7, subgraph.edgeSet().size());
      assertTrue(!subgraph.containsVertex(vertexFactory.get(5)));
      assertTrue(!subgraph.containsVertex(vertexFactory.get(6)));
      assertEmbeddingEquals(subgraph, vertexFactory.get(0), "4,3,2,1");
      assertEmbeddingEquals(subgraph, vertexFactory.get(1), "0,2");
      assertEmbeddingEquals(subgraph, vertexFactory.get(2), "0,3,1");
      assertEmbeddingEquals(subgraph, vertexFactory.get(3), "0,4,2");
      assertEmbeddingEquals(subgraph, vertexFactory.get(4), "3,0");
      assertEquals(5, PlanarGraphs.getBoundaryVertices(subgraph).size());
   }

   @Test
   public void testSubgraphDegreeHigherThanTwo() {
      System.out.println("subgraph");
      PlanarGraph<IdVertex, UndirectedIdEdge<IdVertex>> graph = new DoublyConnectedEdgeList<IdVertex, UndirectedIdEdge<IdVertex>, Object>(new UndirectedIdEdgeFactory<IdVertex>(), Object.class);
      PlanarGraph<IdVertex, UndirectedIdEdge<IdVertex>> subgraph = new DoublyConnectedEdgeList<IdVertex, UndirectedIdEdge<IdVertex>, Object>(new UndirectedIdEdgeFactory<IdVertex>(), Object.class);

      IdVertexFactory vertexFactory = new IdVertexFactory();
      PlanarCircleGraphGenerator generator =
              new PlanarCircleGraphGenerator<IdVertex, UndirectedIdEdge<IdVertex>>(7);

      generator.generateGraph(graph, vertexFactory, null);

      Set<IdVertex> vertices = new HashSet<IdVertex>();
      vertices.add(vertexFactory.get(1));
      vertices.add(vertexFactory.get(6));
      vertices.add(vertexFactory.get(5));
      vertices.add(vertexFactory.get(0));

      PlanarGraphs.subgraph(graph, subgraph, vertices, vertexFactory.get(1), vertexFactory.get(6));

      assertEquals(12, graph.edgeSet().size());
      assertEquals(5, subgraph.edgeSet().size());
      assertTrue(!subgraph.containsVertex(vertexFactory.get(2)));
      assertTrue(!subgraph.containsVertex(vertexFactory.get(3)));
      assertTrue(!subgraph.containsVertex(vertexFactory.get(4)));
      assertEmbeddingEquals(subgraph, vertexFactory.get(1), "6,0");
      assertEmbeddingEquals(subgraph, vertexFactory.get(6), "5,0,1");
      assertEmbeddingEquals(subgraph, vertexFactory.get(5), "0,6");
      assertEmbeddingEquals(subgraph, vertexFactory.get(0), "1,6,5");
      assertEquals(4, PlanarGraphs.getBoundaryVertices(subgraph).size());
   }

   @Test
   public void testDualGraph() {
      System.out.println("dualgraph");
      PlanarGraph<IdVertex, UndirectedIdEdge<IdVertex>> graph = new DoublyConnectedEdgeList<IdVertex, UndirectedIdEdge<IdVertex>, Object>(new UndirectedIdEdgeFactory<IdVertex>(), Object.class);
      PlanarGraph<IdVertex, UndirectedIdEdge<IdVertex>> dualGraph = new DoublyConnectedEdgeList<IdVertex, UndirectedIdEdge<IdVertex>, Object>(new UndirectedIdEdgeFactory<IdVertex>(), Object.class);

      IdVertexFactory vertexFactory = new IdVertexFactory();
      PlanarCircleGraphGenerator generator =
              new PlanarCircleGraphGenerator<IdVertex, UndirectedIdEdge<IdVertex>>(7);

      generator.generateGraph(graph, vertexFactory, null);

      IdVertexFactory dualVertexFactory = new IdVertexFactory();
      PlanarGraphs.dualGraph(graph, dualGraph, dualVertexFactory);

      assertEquals(12, dualGraph.edgeSet().size());
      assertEquals(3, PlanarGraphs.getBoundaryVertices(dualGraph).size());
      assertEmbeddingEquals(dualGraph, dualVertexFactory.get(0), "1,4,6");
      assertEmbeddingEquals(dualGraph, dualVertexFactory.get(1), "6,2,0");
      assertEmbeddingEquals(dualGraph, dualVertexFactory.get(2), "6,3,1");
      assertEmbeddingEquals(dualGraph, dualVertexFactory.get(3), "6,5,2");
      assertEmbeddingEquals(dualGraph, dualVertexFactory.get(4), "6,0,5");
      assertEmbeddingEquals(dualGraph, dualVertexFactory.get(5), "6,4,3");
      assertEmbeddingEquals(dualGraph, dualVertexFactory.get(6), "1,0,4,5,3,2");
   }

   @Test
   public void testDualGraphLarge() {
      System.out.println("dualgraphlarge");
      PlanarGraph<IdVertex, UndirectedIdEdge<IdVertex>> graph = new DoublyConnectedEdgeList<IdVertex, UndirectedIdEdge<IdVertex>, Object>(new UndirectedIdEdgeFactory<IdVertex>(), Object.class);
      PlanarGraph<IdVertex, UndirectedIdEdge<IdVertex>> dualGraph = new DoublyConnectedEdgeList<IdVertex, UndirectedIdEdge<IdVertex>, Object>(new UndirectedIdEdgeFactory<IdVertex>(), Object.class);

      IdVertexFactory vertexFactory = new IdVertexFactory();
      PlanarCircleGraphGenerator generator =
              new PlanarCircleGraphGenerator<IdVertex, UndirectedIdEdge<IdVertex>>(37);

      generator.generateGraph(graph, vertexFactory, null);

      IdVertexFactory dualVertexFactory = new IdVertexFactory();
      PlanarGraphs.dualGraph(graph, dualGraph, dualVertexFactory);
   }

   @Test
   public void testDelaunayVoronoiMedium() throws Exception {
      System.out.println("delaunayVoronoiMedium");
      PlanarGraph<IdVertex, UndirectedIdEdge<IdVertex>> delaunayGraph = new DoublyConnectedEdgeList<IdVertex, UndirectedIdEdge<IdVertex>, Object>(new UndirectedIdEdgeFactory<IdVertex>(), Object.class);

      IdVertexFactory vertexFactory = new IdVertexFactory();
      PlanarCircleGraphGenerator<IdVertex, UndirectedIdEdge<IdVertex>> generator =
              new PlanarCircleGraphGenerator<IdVertex, UndirectedIdEdge<IdVertex>>(37);

      generator.generateGraph(delaunayGraph, vertexFactory, null);

      CoordinateSequenceFactory coordinateSequenceFactory = CoordinateArraySequenceFactory.instance();
      LinearRing boundary = new LinearRing(coordinateSequenceFactory.create(new Coordinate[]{
                 new Coordinate(-1000, -1000),
                 new Coordinate(-1000, 1000),
                 new Coordinate(1000, 1000),
                 new Coordinate(1000, -1000),
                 new Coordinate(-1000, -1000)}),
              new GeometryFactory(coordinateSequenceFactory));

      runDelaunayVoronoi(delaunayGraph, generator, boundary);
   }

   @Test
   public void testDelaunayVoronoiTiny() throws Exception {
      System.out.println("delaunayVoronoiTiny");
      PlanarGraph<IdVertex, UndirectedIdEdge<IdVertex>> delaunayGraph = new DoublyConnectedEdgeList<IdVertex, UndirectedIdEdge<IdVertex>, Object>(new UndirectedIdEdgeFactory<IdVertex>(), Object.class);

      IdVertexFactory vertexFactory = new IdVertexFactory();
      for (int i = 0; i < 5; ++i) {
         vertexFactory.createVertex();
      }
      delaunayGraph.addEdge(vertexFactory.get(0), vertexFactory.get(1));
      delaunayGraph.addEdge(vertexFactory.get(1), vertexFactory.get(2));
      delaunayGraph.addEdge(vertexFactory.get(2), vertexFactory.get(0), vertexFactory.get(1), null);

      final Map<IdVertex, Coordinate> delaunayLocations = new HashMap<IdVertex, Coordinate>();
      delaunayLocations.put(vertexFactory.get(0), new Coordinate(-300, -200));
      delaunayLocations.put(vertexFactory.get(1), new Coordinate(300, -200));
      delaunayLocations.put(vertexFactory.get(2), new Coordinate(0, 200));

      PlanarLayout<IdVertex> delaunayLayout = new PlanarLayout<IdVertex>() {

         @Override
         public Coordinate getCoordinate(IdVertex vertex) {
            return delaunayLocations.get(vertex);
         }
      };
      
      CoordinateSequenceFactory coordinateSequenceFactory = CoordinateArraySequenceFactory.instance();
      LinearRing boundary = new LinearRing(coordinateSequenceFactory.create(new Coordinate[]{
                 new Coordinate(400, -300),
                 new Coordinate(-400, -300),
                 new Coordinate(0, 300),
                 new Coordinate(400, -300)}),
              new GeometryFactory(coordinateSequenceFactory));

      MaximalPlanar<IdVertex, UndirectedIdEdge<IdVertex>> maximalPlanar = new MaximalPlanar<IdVertex, UndirectedIdEdge<IdVertex>>();
      maximalPlanar.makeMaximalPlanar(delaunayGraph);

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
   private <V, E> void runDelaunayVoronoi(PlanarGraph<V, E> delaunayGraph, PlanarLayout<V> delaunayLayout, final LinearRing boundary) throws Exception {
      final DoublyConnectedEdgeList<IdCoordinateVertex, UndirectedIdEdge<IdCoordinateVertex>, Object> voronoiGraph = new DoublyConnectedEdgeList<IdCoordinateVertex, UndirectedIdEdge<IdCoordinateVertex>, Object>(new UndirectedIdEdgeFactory<IdCoordinateVertex>(), Object.class);

      int coordinatePrecision = 5;
      IdCoordinateVertexFactory voronoiVertexFactory = new IdCoordinateVertexFactory(new MathContext(coordinatePrecision));

      final PlanarLayout<IdCoordinateVertex> voronoiLayout =
              PlanarGraphs.delaunayToVoronoi(delaunayGraph, delaunayLayout, voronoiGraph, boundary, voronoiVertexFactory);
      assert (voronoiLayout.getCoordinate(voronoiVertexFactory.get(0)) != null);

      for (IdCoordinateVertex voronoiVertex : voronoiGraph.vertexSet()) {
         voronoiVertex.setCoordinate(voronoiLayout.getCoordinate(voronoiVertex));
      }

      // Output voronoi to file
      writeGraphToFile(voronoiGraph, "voronoi-graph-" + (voronoiGraph.faceSet().size() - 1) + ".xml");

      // Show layout using drawing pad and planar face traversal
      // TODO Move code into graph toolbox, create assertions for Delaunay tests
      /*
      DrawingPad pad = new DrawingPad(720, 600, 20, 20, "Unit test - Voronoi Large");
      pad.getViewport().setRect(new Rectangle.Double(-1200, -1200, 2400, 2400));
      Container graphComponents = new Container();
      graphComponents.add(new JPlanarGraphView<V, E>(delaunayGraph, delaunayLayout, Color.lightGray));
      graphComponents.add(new JPlanarGraphView<IdCoordinateVertex, UndirectedIdEdge<IdCoordinateVertex>>(voronoiGraph, voronoiLayout, Color.black));
      graphComponents.add(new JComponent() {

         {
            Envelope envelope = boundary.getEnvelopeInternal();
            setSize((int) envelope.getWidth(), (int) envelope.getHeight());
         }

         @Override
         public void paint(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setClip(null);
            for (int i = 0; i < boundary.getNumPoints() - 1; ++i) {
               g2d.setColor(Color.blue);
               Coordinate start = boundary.getCoordinateN(i);
               Coordinate end = boundary.getCoordinateN(i + 1);
               Shape line = new Line2D.Double(start.x, start.y, end.x, end.y);
               g2d.setStroke(new BasicStroke(3f));
               g2d.draw(line);
               double radius = 10.;
               Shape circle = new Ellipse2D.Double(start.x - radius, start.y - radius, radius * 2, radius * 2);
               g2d.fill(circle);
            }
         }
      });
      pad.getViewport().setView(graphComponents);
      PlanarGraphLabelProvider<V, E> delaunayLabels = new PlanarGraphLabelProvider<V, E>(delaunayGraph, delaunayLayout, 40, 40);
      for (JComponent component : delaunayLabels.getVertexLabels(pad.getViewport())) {
         pad.getLabelPane().add(component);
      }

      PlanarGraphLabelProvider<IdCoordinateVertex, UndirectedIdEdge<IdCoordinateVertex>> voronoiLabels = new PlanarGraphLabelProvider<IdCoordinateVertex, UndirectedIdEdge<IdCoordinateVertex>>(voronoiGraph, voronoiLayout, 40, 40);
      for (JComponent component : voronoiLabels.getVertexLabels(pad.getViewport())) {
         pad.getLabelPane().add(component);
      }

      pad.showAndWait();
      */
   }

   @Test
   public void testRemoveEdgesWithinBoundary() {
      System.out.println("removeEdgesWithinBoundary");
      PlanarGraph<IdVertex, UndirectedIdEdge<IdVertex>> graph = new DoublyConnectedEdgeList<IdVertex, UndirectedIdEdge<IdVertex>, Object>(new UndirectedIdEdgeFactory<IdVertex>(), Object.class);

      IdVertexFactory vertexFactory = new IdVertexFactory();
      for (int i = 0; i < 5; ++i) {
         graph.addVertex(vertexFactory.createVertex());
      }
      graph.addEdge(vertexFactory.get(0), vertexFactory.get(1));
      graph.addEdge(vertexFactory.get(1), vertexFactory.get(2), vertexFactory.get(0), null);
      graph.addEdge(vertexFactory.get(2), vertexFactory.get(0), vertexFactory.get(1), null);
      graph.addEdge(vertexFactory.get(1), vertexFactory.get(3), vertexFactory.get(0), null);
      graph.addEdge(vertexFactory.get(3), vertexFactory.get(2), vertexFactory.get(1), null);
      graph.addEdge(vertexFactory.get(3), vertexFactory.get(4), vertexFactory.get(1), null);
      graph.addEdge(vertexFactory.get(4), vertexFactory.get(2), vertexFactory.get(3), null);

      List<IdVertex> boundary = new LinkedList<IdVertex>();
      boundary.add(vertexFactory.get(0));
      boundary.add(vertexFactory.get(2));
      boundary.add(vertexFactory.get(4));
      boundary.add(vertexFactory.get(3));
      boundary.add(vertexFactory.get(1));
      EdgeVisitor<UndirectedIdEdge<IdVertex>> removeEdgeVisitor = null;
      PlanarGraphs.removeEdgesInsideBoundary(graph, boundary, removeEdgeVisitor);

      assertEmbeddingEquals(graph, vertexFactory.get(0), "1,2");
      assertEmbeddingEquals(graph, vertexFactory.get(1), "3,0");
      assertEmbeddingEquals(graph, vertexFactory.get(2), "0,4");
      assertEmbeddingEquals(graph, vertexFactory.get(3), "4,1");
      assertEmbeddingEquals(graph, vertexFactory.get(4), "2,3");
      assertEquals(5, graph.edgeSet().size());
   }

   @Test
   public void testRemoveEdgesWithinBoundaryCaseTwo() {
      System.out.println("removeEdgesWithinBoundaryCaseTwo");
      PlanarGraph<IdVertex, UndirectedIdEdge<IdVertex>> graph = new DoublyConnectedEdgeList<IdVertex, UndirectedIdEdge<IdVertex>, Object>(new UndirectedIdEdgeFactory<IdVertex>(), Object.class);

      IdVertexFactory vertexFactory = new IdVertexFactory();
      for (int i = 0; i < 8; ++i) {
         graph.addVertex(vertexFactory.createVertex());
      }
      addTriangularFace(graph, vertexFactory.get(0), vertexFactory.get(1), vertexFactory.get(7));
      addTriangularFace(graph, vertexFactory.get(7), vertexFactory.get(1), vertexFactory.get(6));
      addTriangularFace(graph, vertexFactory.get(6), vertexFactory.get(1), vertexFactory.get(5));
      addTriangularFace(graph, vertexFactory.get(5), vertexFactory.get(1), vertexFactory.get(2));
      addTriangularFace(graph, vertexFactory.get(5), vertexFactory.get(2), vertexFactory.get(4));
      addTriangularFace(graph, vertexFactory.get(4), vertexFactory.get(2), vertexFactory.get(3));

      List<IdVertex> boundary = new LinkedList<IdVertex>();
      for (int i = 0; i < 8; ++i) {
         boundary.add(vertexFactory.get(i));
      }

      EdgeVisitor<UndirectedIdEdge<IdVertex>> removeEdgeVisitor = null;
      PlanarGraphs.removeEdgesInsideBoundary(graph, boundary, removeEdgeVisitor);

      assertEmbeddingEquals(graph, vertexFactory.get(0), "1,7");
      assertEmbeddingEquals(graph, vertexFactory.get(1), "2,0");
      assertEmbeddingEquals(graph, vertexFactory.get(2), "3,1");
      assertEmbeddingEquals(graph, vertexFactory.get(3), "4,2");
      assertEmbeddingEquals(graph, vertexFactory.get(4), "5,3");
      assertEmbeddingEquals(graph, vertexFactory.get(5), "6,4");
      assertEmbeddingEquals(graph, vertexFactory.get(6), "7,5");
      assertEmbeddingEquals(graph, vertexFactory.get(7), "0,6");
      assertEquals(8, graph.edgeSet().size());
   }

   @Test
   public void testTriangulateFace() {
      System.out.println("triangulateFace");
      PlanarGraph<IdVertex, UndirectedIdEdge<IdVertex>> graph = new DoublyConnectedEdgeList<IdVertex, UndirectedIdEdge<IdVertex>, Object>(new UndirectedIdEdgeFactory<IdVertex>(), Object.class);

      IdVertexFactory vertexFactory = new IdVertexFactory();

      // Open pentagon
      for (int i = 0; i < 5; ++i) {
         graph.addVertex(vertexFactory.createVertex());
      }
      graph.addEdge(vertexFactory.get(0), vertexFactory.get(1));
      graph.addEdge(vertexFactory.get(1), vertexFactory.get(2), vertexFactory.get(0), null);
      graph.addEdge(vertexFactory.get(2), vertexFactory.get(3), vertexFactory.get(1), null);
      graph.addEdge(vertexFactory.get(3), vertexFactory.get(4), vertexFactory.get(2), null);
      graph.addEdge(vertexFactory.get(4), vertexFactory.get(0), vertexFactory.get(3), null);

      IdVertex source = vertexFactory.get(0);
      IdVertex target = vertexFactory.get(1);
      EdgeVisitor<UndirectedIdEdge<IdVertex>> addEdgeVisitor = null;
      PlanarGraphs.triangulateFace(graph, source, target, addEdgeVisitor);

      assertEquals(7, graph.edgeSet().size());
      assertEmbeddingEquals(graph, vertexFactory.get(0), "4,3,2,1");
      assertEmbeddingEquals(graph, vertexFactory.get(1), "2,0");
      assertEmbeddingEquals(graph, vertexFactory.get(2), "3,1,0");
      assertEmbeddingEquals(graph, vertexFactory.get(3), "4,2,0");
      assertEmbeddingEquals(graph, vertexFactory.get(4), "0,3");
   }

   @Test
   public void testTriangulateFaceCaseTwo() {
      System.out.println("triangulateFace");
      PlanarGraph<IdVertex, UndirectedIdEdge<IdVertex>> graph = new DoublyConnectedEdgeList<IdVertex, UndirectedIdEdge<IdVertex>, Object>(new UndirectedIdEdgeFactory<IdVertex>(), Object.class);

      IdVertexFactory vertexFactory = new IdVertexFactory();

      // Open pentagon
      for (int i = 0; i < 6; ++i) {
         graph.addVertex(vertexFactory.createVertex());
      }
      graph.addEdge(vertexFactory.get(0), vertexFactory.get(1));
      graph.addEdge(vertexFactory.get(1), vertexFactory.get(2), vertexFactory.get(0), null);
      graph.addEdge(vertexFactory.get(2), vertexFactory.get(3), vertexFactory.get(1), null);
      graph.addEdge(vertexFactory.get(3), vertexFactory.get(4), vertexFactory.get(2), null);
      graph.addEdge(vertexFactory.get(4), vertexFactory.get(5), vertexFactory.get(3), null);
      graph.addEdge(vertexFactory.get(5), vertexFactory.get(0), vertexFactory.get(4), null);
      graph.addEdge(vertexFactory.get(0), vertexFactory.get(1), vertexFactory.get(5), null);
      graph.addEdge(vertexFactory.get(0), vertexFactory.get(2), vertexFactory.get(1), null);

      IdVertex source = vertexFactory.get(0);
      IdVertex target = vertexFactory.get(1);
      EdgeVisitor<UndirectedIdEdge<IdVertex>> addEdgeVisitor = null;
      PlanarGraphs.triangulateFace(graph, source, target, addEdgeVisitor);

      assertEmbeddingEquals(graph, vertexFactory.get(0), "5,4,3,1,2");
      assertEmbeddingEquals(graph, vertexFactory.get(1), "0,3,2");
      assertEmbeddingEquals(graph, vertexFactory.get(2), "0,1,3");
      assertEmbeddingEquals(graph, vertexFactory.get(3), "2,1,0,4");
      assertEmbeddingEquals(graph, vertexFactory.get(4), "3,0,5");
      assertEmbeddingEquals(graph, vertexFactory.get(5), "4,0");
      assertEquals(10, graph.edgeSet().size());
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
              IdVertex.class,
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
