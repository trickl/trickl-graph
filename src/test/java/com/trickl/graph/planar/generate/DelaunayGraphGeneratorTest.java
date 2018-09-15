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
package com.trickl.graph.planar.generate;

import cern.jet.random.engine.MersenneTwister;
import com.trickl.graph.edges.UndirectedIdEdge;
import com.trickl.graph.edges.UndirectedIdEdgeFactory;
import com.trickl.graph.ext.JComponentWindow;
import com.trickl.graph.planar.DoublyConnectedEdgeList;
import com.trickl.graph.planar.JGraphAdaptor;
import com.trickl.graph.planar.PlanarGraph;
import com.trickl.graph.planar.PlanarLayout;
import com.trickl.graph.vertices.IdCoordinateVertex;
import com.trickl.graph.vertices.IdCoordinateVertexFactory;
import com.trickl.graph.vertices.IdVertex;
import com.trickl.graph.vertices.IdVertexFactory;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateList;
import javax.swing.JScrollPane;
import org.jgraph.JGraph;
import org.jgrapht.VertexFactory;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class DelaunayGraphGeneratorTest {

   @Test    
   public void producesCorrectLayoutForFourPoints() throws Exception {

      PlanarGraph<IdVertex, UndirectedIdEdge<IdVertex>> graph = new DoublyConnectedEdgeList<IdVertex, UndirectedIdEdge<IdVertex>, Object>(new UndirectedIdEdgeFactory<IdVertex>(), Object.class);

      CoordinateList sites = new CoordinateList();
      Coordinate A = new Coordinate(0, 1);
      Coordinate B = new Coordinate(0, 0);
      Coordinate C = new Coordinate(1, 0);
      Coordinate D = new Coordinate(0.6, 0.6);
      sites.add(A);
      sites.add(B);
      sites.add(C);
      sites.add(D);
      
      VertexFactory<IdVertex> vertexFactory = new IdVertexFactory();
      DelaunayGraphGenerator<IdVertex, UndirectedIdEdge<IdVertex>> generator 
              = new DelaunayGraphGenerator<IdVertex, UndirectedIdEdge<IdVertex>>(sites, vertexFactory);
      generator.setRandomEngine(new MersenneTwister(12345));
      generator.generateGraph(graph, null, null);
      
      assertEquals(5, graph.edgeSet().size());
      assertTrue(graph.containsEdge(generator.getVertex(B), generator.getVertex(D)));
      assertTrue(graph.containsEdge(generator.getVertex(A), generator.getVertex(B)));
      assertTrue(graph.containsEdge(generator.getVertex(B), generator.getVertex(C)));
      assertTrue(graph.containsEdge(generator.getVertex(C), generator.getVertex(D)));
      assertTrue(graph.containsEdge(generator.getVertex(D), generator.getVertex(A)));
      
      if (Boolean.parseBoolean(System.getProperty("visualTests"))) {
        JGraph jGraph = JGraphAdaptor.getDisplayGraph(graph, generator);
        JComponentWindow window = new JComponentWindow(new JScrollPane(jGraph));              
        window.showAndWait();
      }
   }

   @Test
   public void producesCorrectLayoutForSixPoints() throws Exception {

      PlanarGraph<IdVertex, UndirectedIdEdge<IdVertex>> graph = new DoublyConnectedEdgeList<IdVertex, UndirectedIdEdge<IdVertex>, Object>(new UndirectedIdEdgeFactory<IdVertex>(), Object.class);

      CoordinateList sites = new CoordinateList();
      Coordinate A = new Coordinate(0, 1);
      Coordinate B = new Coordinate(0, 0);
      Coordinate C = new Coordinate(1, 0);
      Coordinate D = new Coordinate(0.6, 0.6);
      Coordinate E = new Coordinate(0.1, 0.5);
      Coordinate F = new Coordinate(0.5, 0.1);

      sites.add(A);
      sites.add(B);
      sites.add(C);
      sites.add(D);
      sites.add(E);
      sites.add(F);

      VertexFactory<IdVertex> vertexFactory = new IdVertexFactory();
      DelaunayGraphGenerator<IdVertex, UndirectedIdEdge<IdVertex>> generator
              = new DelaunayGraphGenerator<IdVertex, UndirectedIdEdge<IdVertex>>(sites, vertexFactory);
      generator.setRandomEngine(new MersenneTwister(12345));
      generator.generateGraph(graph, null, null);

      assertEquals(11, graph.edgeSet().size());
      assertTrue(graph.containsEdge(generator.getVertex(A), generator.getVertex(B)));
      assertTrue(graph.containsEdge(generator.getVertex(B), generator.getVertex(E)));
      assertTrue(graph.containsEdge(generator.getVertex(E), generator.getVertex(A)));
      assertTrue(graph.containsEdge(generator.getVertex(B), generator.getVertex(C)));
      assertTrue(graph.containsEdge(generator.getVertex(C), generator.getVertex(F)));
      assertTrue(graph.containsEdge(generator.getVertex(F), generator.getVertex(B)));
      assertTrue(graph.containsEdge(generator.getVertex(E), generator.getVertex(F)));
      assertTrue(graph.containsEdge(generator.getVertex(F), generator.getVertex(D)));
      assertTrue(graph.containsEdge(generator.getVertex(D), generator.getVertex(E)));
      assertTrue(graph.containsEdge(generator.getVertex(A), generator.getVertex(D)));
      assertTrue(graph.containsEdge(generator.getVertex(D), generator.getVertex(C)));

      if (Boolean.parseBoolean(System.getProperty("visualTests"))) {
        JGraph jGraph = JGraphAdaptor.getDisplayGraph(graph, generator);
        JComponentWindow window = new JComponentWindow(new JScrollPane(jGraph));              
        window.showAndWait();
      }
   }

   @Test
   public void worksForExistingGraphWithPlanarLayout() throws Exception {

      PlanarGraph<IdCoordinateVertex, UndirectedIdEdge<IdCoordinateVertex>> baseGraph = new DoublyConnectedEdgeList<>(new UndirectedIdEdgeFactory<IdCoordinateVertex>(), Object.class);

      IdCoordinateVertexFactory vertexFactory = new IdCoordinateVertexFactory();
      IdCoordinateVertex A = vertexFactory.createVertex();
      IdCoordinateVertex B = vertexFactory.createVertex();
      IdCoordinateVertex C = vertexFactory.createVertex();
      IdCoordinateVertex D = vertexFactory.createVertex();
      A.setCoordinate(new Coordinate(0, 1));
      B.setCoordinate(new Coordinate(0, 0));
      C.setCoordinate(new Coordinate(1, 0));
      D.setCoordinate(new Coordinate(0.6, 0.6));
      baseGraph.addVertex(A);
      baseGraph.addVertex(B);
      baseGraph.addVertex(C);
      baseGraph.addVertex(D);

      PlanarLayout<IdCoordinateVertex> layout = (IdCoordinateVertex vertex) -> vertex.getCoordinate();
      
      DelaunayGraphGenerator<IdCoordinateVertex, UndirectedIdEdge<IdCoordinateVertex>> generator
              = new DelaunayGraphGenerator<>(baseGraph.vertexSet(), layout);
      
      
      PlanarGraph<IdCoordinateVertex, UndirectedIdEdge<IdCoordinateVertex>> graph = new DoublyConnectedEdgeList<>(new UndirectedIdEdgeFactory<>(), Object.class);
      generator.setRandomEngine(new MersenneTwister(12345));
      generator.generateGraph(graph, null, null);

      assertEquals(5, graph.edgeSet().size());
      assertTrue(graph.containsEdge(B, D));
      assertTrue(graph.containsEdge(A, B));
      assertTrue(graph.containsEdge(B, C));
      assertTrue(graph.containsEdge(C, D));
      assertTrue(graph.containsEdge(D, A));
      
      if (Boolean.parseBoolean(System.getProperty("visualTests"))) {
        JGraph jGraph = JGraphAdaptor.getDisplayGraph(graph, generator);
        JComponentWindow window = new JComponentWindow(new JScrollPane(jGraph));              
        window.showAndWait();
      }
   }      
}
