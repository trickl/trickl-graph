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

import com.jgraph.components.labels.MultiLineVertexView;
import com.trickl.graph.edges.UndirectedIdEdge;
import com.trickl.graph.edges.UndirectedIdEdgeFactory;
import com.trickl.graph.ext.FixedAttributeProvider;
import com.trickl.graph.ext.JComponentWindow;
import com.trickl.graph.ext.JGraphModelAdapterExt;
import com.trickl.graph.planar.*;
import com.trickl.graph.planar.faces.IdFace;
import com.trickl.graph.planar.faces.IdFaceFactory;
import com.trickl.graph.planar.xml.XmlDcelDocument;
import com.trickl.graph.vertices.IdVertex;
import com.trickl.graph.vertices.IdVertexFactory;
import com.trickl.graph.vertices.IdVertexNameProvider;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import java.awt.geom.AffineTransform;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JScrollPane;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.jgraph.JGraph;
import org.jgraph.graph.DefaultCellViewFactory;
import org.jgraph.graph.GraphLayoutCache;
import org.jgraph.graph.VertexView;
import org.jgrapht.VertexFactory;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Ignore;
import org.junit.Test;

public class FortuneVoronoiGraphGeneratorTest {
   
   @Test
   public void producesCorrectLayoutForZeroPointsWithBoundary() throws Exception {

      PlanarGraph<IdVertex, UndirectedIdEdge<IdVertex>> voronoiGraph = new DoublyConnectedEdgeList<IdVertex, UndirectedIdEdge<IdVertex>, IdFace>(new UndirectedIdEdgeFactory<IdVertex>(), new IdFaceFactory());
      PlanarGraph<IdVertex, UndirectedIdEdge<IdVertex>> siteGraph = new DoublyConnectedEdgeList<IdVertex, UndirectedIdEdge<IdVertex>, IdFace>(new UndirectedIdEdgeFactory<IdVertex>(), new IdFaceFactory());
      
      Map<IdVertex, Coordinate> siteCoordinates = new HashMap<IdVertex, Coordinate>();
      IdVertexFactory siteVertexFactory = new IdVertexFactory();      
      CoordinateList coordList = new CoordinateList();
      
      for (Coordinate coord : coordList.toCoordinateArray()) {
         IdVertex siteVertex = siteVertexFactory.createVertex();
         siteGraph.addVertex(siteVertex);
         siteCoordinates.put(siteVertex, coord);            
      }

      // Note the points on the boundary must be closed (end with the first point)
      CoordinateList boundaryCoords = new CoordinateList();
      boundaryCoords.add(new Coordinate(0.5, 1.0));
      boundaryCoords.add(new Coordinate(1.5, -0.5));
      boundaryCoords.add(new Coordinate(-0.5, -0.5));
      boundaryCoords.add(new Coordinate(0.5, 1.0));
      LinearRing boundary = new LinearRing(new CoordinateArraySequence(boundaryCoords.toCoordinateArray()),
              new GeometryFactory());

      VertexFactory<IdVertex> voronoiVertexFactory = new IdVertexFactory();
      FortuneVoronoiGraphGenerator<IdVertex, UndirectedIdEdge<IdVertex>> voronoiGraphGenerator =
              new FortuneVoronoiGraphGenerator<IdVertex, UndirectedIdEdge<IdVertex>>(siteCoordinates.values(), boundary);
      voronoiGraphGenerator.generateGraph(voronoiGraph, voronoiVertexFactory, null);

      // Compare to a model graph
      writeGraph(voronoiGraph, "voronoi-zeropointswithboundary.xml");
      PlanarGraph<IdVertex, UndirectedIdEdge<IdVertex>> modelGraph = readGraph("voronoi-zeropointswithboundary.xml");
      assertTrue(PlanarGraphs.isIsomorphic(voronoiGraph, modelGraph));
      
      // Visual check     
      //JGraph jGraph = getDisplayGraph(voronoiGraph, voronoiGraphGenerator, siteGraph, new MapPlanarLayout<IdVertex>(siteCoordinates));
      //JComponentWindow window = new JComponentWindow(new JScrollPane(jGraph));              
      //window.showAndWait();
   }
   
   @Test
   public void producesCorrectLayoutForOnePointWithBoundary() throws Exception {

      PlanarGraph<IdVertex, UndirectedIdEdge<IdVertex>> voronoiGraph = new DoublyConnectedEdgeList<IdVertex, UndirectedIdEdge<IdVertex>, IdFace>(new UndirectedIdEdgeFactory<IdVertex>(), new IdFaceFactory());
      PlanarGraph<IdVertex, UndirectedIdEdge<IdVertex>> siteGraph = new DoublyConnectedEdgeList<IdVertex, UndirectedIdEdge<IdVertex>, IdFace>(new UndirectedIdEdgeFactory<IdVertex>(), new IdFaceFactory());
      
      Map<IdVertex, Coordinate> siteCoordinates = new HashMap<IdVertex, Coordinate>();
      IdVertexFactory siteVertexFactory = new IdVertexFactory();      
      CoordinateList coordList = new CoordinateList();
      coordList.add(new Coordinate(0, 0));
    
      for (Coordinate coord : coordList.toCoordinateArray()) {
         IdVertex siteVertex = siteVertexFactory.createVertex();
         siteGraph.addVertex(siteVertex);
         siteCoordinates.put(siteVertex, coord);            
      }

      // Note the points on the boundary must be closed (end with the first point)
      CoordinateList boundaryCoords = new CoordinateList();
      boundaryCoords.add(new Coordinate(0.5, 1.0));
      boundaryCoords.add(new Coordinate(1.5, -0.5));
      boundaryCoords.add(new Coordinate(-0.5, -0.5));
      boundaryCoords.add(new Coordinate(0.5, 1.0));
      LinearRing boundary = new LinearRing(new CoordinateArraySequence(boundaryCoords.toCoordinateArray()),
              new GeometryFactory());

      VertexFactory<IdVertex> voronoiVertexFactory = new IdVertexFactory();
      FortuneVoronoiGraphGenerator<IdVertex, UndirectedIdEdge<IdVertex>> voronoiGraphGenerator =
              new FortuneVoronoiGraphGenerator<IdVertex, UndirectedIdEdge<IdVertex>>(siteCoordinates.values(), boundary);
      voronoiGraphGenerator.generateGraph(voronoiGraph, voronoiVertexFactory, null);

      // Compare to a model graph
      //writeGraph(voronoiGraph, "voronoi-onepointwithboundary.xml");
      PlanarGraph<IdVertex, UndirectedIdEdge<IdVertex>> modelGraph = readGraph("voronoi-onepointwithboundary.xml");
      assertTrue(PlanarGraphs.isIsomorphic(voronoiGraph, modelGraph));
      
      // Visual check     
      //JGraph jGraph = getDisplayGraph(voronoiGraph, voronoiGraphGenerator, siteGraph, new MapPlanarLayout<IdVertex>(siteCoordinates));
      //JComponentWindow window = new JComponentWindow(new JScrollPane(jGraph));              
      //window.showAndWait();
   }
   
   @Test
   public void producesCorrectLayoutForTwoPointsWithBoundary() throws Exception {

      PlanarGraph<IdVertex, UndirectedIdEdge<IdVertex>> voronoiGraph = new DoublyConnectedEdgeList<IdVertex, UndirectedIdEdge<IdVertex>, IdFace>(new UndirectedIdEdgeFactory<IdVertex>(), new IdFaceFactory());
      PlanarGraph<IdVertex, UndirectedIdEdge<IdVertex>> siteGraph = new DoublyConnectedEdgeList<IdVertex, UndirectedIdEdge<IdVertex>, IdFace>(new UndirectedIdEdgeFactory<IdVertex>(), new IdFaceFactory());
      
      Map<IdVertex, Coordinate> siteCoordinates = new HashMap<IdVertex, Coordinate>();
      IdVertexFactory siteVertexFactory = new IdVertexFactory();      
      CoordinateList coordList = new CoordinateList();
      coordList.add(new Coordinate(0, 0));
      coordList.add(new Coordinate(1, 0));

      for (Coordinate coord : coordList.toCoordinateArray()) {
         IdVertex siteVertex = siteVertexFactory.createVertex();
         siteGraph.addVertex(siteVertex);
         siteCoordinates.put(siteVertex, coord);            
      }

      // Note the points on the boundary must be closed (end with the first point)
      CoordinateList boundaryCoords = new CoordinateList();
      boundaryCoords.add(new Coordinate(0.5, 1.0));
      boundaryCoords.add(new Coordinate(1.5, -0.5));
      boundaryCoords.add(new Coordinate(-0.5, -0.5));
      boundaryCoords.add(new Coordinate(0.5, 1.0));
      LinearRing boundary = new LinearRing(new CoordinateArraySequence(boundaryCoords.toCoordinateArray()),
              new GeometryFactory());

      VertexFactory<IdVertex> voronoiVertexFactory = new IdVertexFactory();
      FortuneVoronoiGraphGenerator<IdVertex, UndirectedIdEdge<IdVertex>> voronoiGraphGenerator =
              new FortuneVoronoiGraphGenerator<IdVertex, UndirectedIdEdge<IdVertex>>(siteCoordinates.values(), boundary);
      voronoiGraphGenerator.generateGraph(voronoiGraph, voronoiVertexFactory, null);

      // Compare to a model graph
      //writeGraph(voronoiGraph, "voronoi-twopointswithboundary.xml");
      PlanarGraph<IdVertex, UndirectedIdEdge<IdVertex>> modelGraph = readGraph("voronoi-twopointswithboundary.xml");
      assertTrue(PlanarGraphs.isIsomorphic(voronoiGraph, modelGraph));
      
      // Visual check     
      //JGraph jGraph = getDisplayGraph(voronoiGraph, voronoiGraphGenerator, siteGraph, new MapPlanarLayout<IdVertex>(siteCoordinates));
      //JComponentWindow window = new JComponentWindow(new JScrollPane(jGraph));              
      //window.showAndWait();
   }

   @Test
   public void producesCorrectLayoutForThreePointsNoBoundary() throws Exception {

      PlanarGraph<IdVertex, UndirectedIdEdge<IdVertex>> voronoiGraph = new DoublyConnectedEdgeList<IdVertex, UndirectedIdEdge<IdVertex>, IdFace>(new UndirectedIdEdgeFactory<IdVertex>(), new IdFaceFactory());
      PlanarGraph<IdVertex, UndirectedIdEdge<IdVertex>> siteGraph = new DoublyConnectedEdgeList<IdVertex, UndirectedIdEdge<IdVertex>, IdFace>(new UndirectedIdEdgeFactory<IdVertex>(), new IdFaceFactory());

      Map<IdVertex, Coordinate> siteCoordinates = new HashMap<IdVertex, Coordinate>();
      IdVertexFactory siteVertexFactory = new IdVertexFactory();      
      CoordinateList coordList = new CoordinateList();
      coordList.add(new Coordinate(0, 0));
      coordList.add(new Coordinate(0.5, 0.5));
      coordList.add(new Coordinate(1, 0));

      for (Coordinate coord : coordList.toCoordinateArray()) {
         IdVertex siteVertex = siteVertexFactory.createVertex();
         siteGraph.addVertex(siteVertex);
         siteCoordinates.put(siteVertex, coord);            
      }

      VertexFactory<IdVertex> voronoiVertexFactory = new IdVertexFactory();
      FortuneVoronoiGraphGenerator<IdVertex, UndirectedIdEdge<IdVertex>> voronoiGraphGenerator =
              new FortuneVoronoiGraphGenerator<IdVertex, UndirectedIdEdge<IdVertex>>(siteCoordinates.values());
      voronoiGraphGenerator.generateGraph(voronoiGraph, voronoiVertexFactory, null);

      // Compare to a model graph
      //writeGraph(voronoiGraph, "voronoi-threepointsnoboundary.xml");
      PlanarGraph<IdVertex, UndirectedIdEdge<IdVertex>> modelGraph = readGraph("voronoi-threepointsnoboundary.xml");
      assertTrue(PlanarGraphs.isIsomorphic(voronoiGraph, modelGraph));
      
      // Visual check     
      //JGraph jGraph = getDisplayGraph(voronoiGraph, voronoiGraphGenerator, siteGraph, new MapPlanarLayout<IdVertex>(siteCoordinates));
      //JComponentWindow window = new JComponentWindow(new JScrollPane(jGraph));              
      //window.showAndWait();
   }

   @Test
   public void producesCorrectLayoutForThreePointsWithBoundary() throws Exception {

      PlanarGraph<IdVertex, UndirectedIdEdge<IdVertex>> voronoiGraph = new DoublyConnectedEdgeList<IdVertex, UndirectedIdEdge<IdVertex>, IdFace>(new UndirectedIdEdgeFactory<IdVertex>(), new IdFaceFactory());
      PlanarGraph<IdVertex, UndirectedIdEdge<IdVertex>> siteGraph = new DoublyConnectedEdgeList<IdVertex, UndirectedIdEdge<IdVertex>, IdFace>(new UndirectedIdEdgeFactory<IdVertex>(), new IdFaceFactory());
      
      Map<IdVertex, Coordinate> siteCoordinates = new HashMap<IdVertex, Coordinate>();
      IdVertexFactory siteVertexFactory = new IdVertexFactory();      
      CoordinateList coordList = new CoordinateList();
      coordList.add(new Coordinate(0, 0));
      coordList.add(new Coordinate(0.5, 0.5));
      coordList.add(new Coordinate(1, 0));

      for (Coordinate coord : coordList.toCoordinateArray()) {
         IdVertex siteVertex = siteVertexFactory.createVertex();
         siteGraph.addVertex(siteVertex);
         siteCoordinates.put(siteVertex, coord);            
      }

      // Note the points on the boundary must be closed (end with the first point)
      CoordinateList boundaryCoords = new CoordinateList();
      boundaryCoords.add(new Coordinate(0.5, 1.0));
      boundaryCoords.add(new Coordinate(1.5, -0.5));
      boundaryCoords.add(new Coordinate(-0.5, -0.5));
      boundaryCoords.add(new Coordinate(0.5, 1.0));
      LinearRing boundary = new LinearRing(new CoordinateArraySequence(boundaryCoords.toCoordinateArray()),
              new GeometryFactory());

      VertexFactory<IdVertex> voronoiVertexFactory = new IdVertexFactory();
      FortuneVoronoiGraphGenerator<IdVertex, UndirectedIdEdge<IdVertex>> voronoiGraphGenerator =
              new FortuneVoronoiGraphGenerator<IdVertex, UndirectedIdEdge<IdVertex>>(siteCoordinates.values(), boundary);
      voronoiGraphGenerator.generateGraph(voronoiGraph, voronoiVertexFactory, null);

      // Compare to a model graph
      //writeGraph(voronoiGraph, "voronoi-threepointswithboundary.xml");
      PlanarGraph<IdVertex, UndirectedIdEdge<IdVertex>> modelGraph = readGraph("voronoi-threepointswithboundary.xml");
      assertTrue(PlanarGraphs.isIsomorphic(voronoiGraph, modelGraph));
      
      // Visual check     
      //JGraph jGraph = getDisplayGraph(voronoiGraph, voronoiGraphGenerator, siteGraph, new MapPlanarLayout<IdVertex>(siteCoordinates));
      //JComponentWindow window = new JComponentWindow(new JScrollPane(jGraph));              
      //window.showAndWait();
   }

   @Test
   public void producesCorrectLayoutForFourPointsNoBoundary() throws Exception {

      PlanarGraph<IdVertex, UndirectedIdEdge<IdVertex>> voronoiGraph = new DoublyConnectedEdgeList<IdVertex, UndirectedIdEdge<IdVertex>, IdFace>(new UndirectedIdEdgeFactory<IdVertex>(), new IdFaceFactory());
      PlanarGraph<IdVertex, UndirectedIdEdge<IdVertex>> siteGraph = new DoublyConnectedEdgeList<IdVertex, UndirectedIdEdge<IdVertex>, IdFace>(new UndirectedIdEdgeFactory<IdVertex>(), new IdFaceFactory());
      
      Map<IdVertex, Coordinate> siteCoordinates = new HashMap<IdVertex, Coordinate>();
      IdVertexFactory siteVertexFactory = new IdVertexFactory();      
      CoordinateList coordList = new CoordinateList();
      coordList.add(new Coordinate(0, 0));
      coordList.add(new Coordinate(0.5, 0.5));
      coordList.add(new Coordinate(1, 0));
      coordList.add(new Coordinate(1.5, 0.5));

      for (Coordinate coord : coordList.toCoordinateArray()) {
         IdVertex siteVertex = siteVertexFactory.createVertex();
         siteGraph.addVertex(siteVertex);
         siteCoordinates.put(siteVertex, coord);            
      }

      VertexFactory<IdVertex> voronoiVertexFactory = new IdVertexFactory();
      FortuneVoronoiGraphGenerator<IdVertex, UndirectedIdEdge<IdVertex>> voronoiGraphGenerator =
              new FortuneVoronoiGraphGenerator<IdVertex, UndirectedIdEdge<IdVertex>>(siteCoordinates.values());
      voronoiGraphGenerator.generateGraph(voronoiGraph, voronoiVertexFactory, null);

      // Compare to a model graph
      //writeGraph(voronoiGraph, "voronoi-fourpointsnoboundary.xml");
      PlanarGraph<IdVertex, UndirectedIdEdge<IdVertex>> modelGraph = readGraph("voronoi-fourpointsnoboundary.xml");
      assertTrue(PlanarGraphs.isIsomorphic(voronoiGraph, modelGraph));
      
      // Visual check     
      //JGraph jGraph = getDisplayGraph(voronoiGraph, voronoiGraphGenerator, siteGraph, new MapPlanarLayout<IdVertex>(siteCoordinates));
      //JComponentWindow window = new JComponentWindow(new JScrollPane(jGraph));              
      //window.showAndWait();
   }   
   
   @Test
   public void producesCorrectLayoutForFourPointsWithBoundary() throws Exception {

      PlanarGraph<IdVertex, UndirectedIdEdge<IdVertex>> voronoiGraph = new DoublyConnectedEdgeList<IdVertex, UndirectedIdEdge<IdVertex>, IdFace>(new UndirectedIdEdgeFactory<IdVertex>(), new IdFaceFactory());
      PlanarGraph<IdVertex, UndirectedIdEdge<IdVertex>> siteGraph = new DoublyConnectedEdgeList<IdVertex, UndirectedIdEdge<IdVertex>, IdFace>(new UndirectedIdEdgeFactory<IdVertex>(), new IdFaceFactory());
      
      Map<IdVertex, Coordinate> siteCoordinates = new HashMap<IdVertex, Coordinate>();      
      IdVertexFactory siteVertexFactory = new IdVertexFactory();      
      CoordinateList coordList = new CoordinateList();
      coordList.add(new Coordinate(0, 0));
      coordList.add(new Coordinate(0.5, 0.5));
      coordList.add(new Coordinate(1, 0));
      coordList.add(new Coordinate(1.5, 0.5));

      for (Coordinate coord : coordList.toCoordinateArray()) {
         IdVertex siteVertex = siteVertexFactory.createVertex();
         siteGraph.addVertex(siteVertex);
         siteCoordinates.put(siteVertex, coord);            
      }
      
      // Note the points on the boundary must be closed (end with the first point)
      CoordinateList boundaryCoords = new CoordinateList();
      boundaryCoords.add(new Coordinate(0, 1.0));
      boundaryCoords.add(new Coordinate(1.5, 1.0));
      boundaryCoords.add(new Coordinate(1.5, -0.5));
      boundaryCoords.add(new Coordinate(0, -0.5));
      boundaryCoords.add(new Coordinate(0, 1.0));
      LinearRing boundary = new LinearRing(new CoordinateArraySequence(boundaryCoords.toCoordinateArray()),
              new GeometryFactory());

      VertexFactory<IdVertex> voronoiVertexFactory = new IdVertexFactory();
      FortuneVoronoiGraphGenerator<IdVertex, UndirectedIdEdge<IdVertex>> voronoiGraphGenerator = 
              new FortuneVoronoiGraphGenerator<IdVertex, UndirectedIdEdge<IdVertex>>(siteCoordinates.values(), boundary);
      voronoiGraphGenerator.generateGraph(voronoiGraph, voronoiVertexFactory, null);
      
      // Compare to a model graph
      //writeGraph(voronoiGraph, "voronoi-fourpointswithboundary.xml");
      PlanarGraph<IdVertex, UndirectedIdEdge<IdVertex>> modelGraph = readGraph("voronoi-fourpointswithboundary.xml");
      assertTrue(PlanarGraphs.isIsomorphic(voronoiGraph, modelGraph));
      
      // Visual check     
      //JGraph jGraph = getDisplayGraph(voronoiGraph, voronoiGraphGenerator, siteGraph, new MapPlanarLayout<IdVertex>(siteCoordinates));
      //JComponentWindow window = new JComponentWindow(new JScrollPane(jGraph));              
      //window.showAndWait();
   }
   
   @Test
   public void producesCorrectLayoutForManyPointsWithBoundary() throws Exception {

      PlanarGraph<IdVertex, UndirectedIdEdge<IdVertex>> voronoiGraph = new DoublyConnectedEdgeList<IdVertex, UndirectedIdEdge<IdVertex>, IdFace>(new UndirectedIdEdgeFactory<IdVertex>(), new IdFaceFactory());
      PlanarGraph<IdVertex, UndirectedIdEdge<IdVertex>> siteGraph = new DoublyConnectedEdgeList<IdVertex, UndirectedIdEdge<IdVertex>, IdFace>(new UndirectedIdEdgeFactory<IdVertex>(), new IdFaceFactory());
      
      // Read the point list from a file
      URL pointFile = this.getClass().getResource("voronoi-points.dat");
      InputStreamReader pointReader = new InputStreamReader(pointFile.openStream());
      final Pattern pointPattern = Pattern.compile("([-+]?[0-9]*\\.?[0-9]+)\\s*,\\s*([-+]?[0-9]*\\.?[0-9]+)[!]?");      
      BufferedReader bufferedReader = new BufferedReader(pointReader);
      Map<IdVertex, Coordinate> siteCoordinates = new HashMap<IdVertex, Coordinate>();
      IdVertexFactory siteVertexFactory = new IdVertexFactory();      
      String line;
      while ((line = bufferedReader.readLine()) != null) {
         Matcher matcher = pointPattern.matcher(line);
         if (matcher.matches()) {            
            IdVertex siteVertex = siteVertexFactory.createVertex();
            siteGraph.addVertex(siteVertex);
            siteCoordinates.put(siteVertex, new Coordinate(Double.parseDouble(matcher.group(1)), Double.parseDouble(matcher.group(2))));            
         } else {
            throw new ParseException("Could not parse point '" + line + "'", 0);
         }
      }
      
      // Note the points on the boundary must be closed (end with the first point)
      CoordinateList boundaryCoords = new CoordinateList();
      boundaryCoords.add(new Coordinate(-2, -2));
      boundaryCoords.add(new Coordinate(-2, 2));
      boundaryCoords.add(new Coordinate(2, 2));
      boundaryCoords.add(new Coordinate(2, -2));
      boundaryCoords.add(new Coordinate(-2, -2));
      LinearRing boundary = new LinearRing(new CoordinateArraySequence(boundaryCoords.toCoordinateArray()),
              new GeometryFactory());
      
      VertexFactory<IdVertex> voronoiVertexFactory = new IdVertexFactory();
      FortuneVoronoiGraphGenerator<IdVertex, UndirectedIdEdge<IdVertex>> voronoiGraphGenerator =
              new FortuneVoronoiGraphGenerator<IdVertex, UndirectedIdEdge<IdVertex>>(siteCoordinates.values(), boundary);
      voronoiGraphGenerator.generateGraph(voronoiGraph, voronoiVertexFactory, null);
      
      // Compare to a model graph
      //writeGraph(voronoiGraph, "voronoi-manypointswithboundary.xml");
      PlanarGraph<IdVertex, UndirectedIdEdge<IdVertex>> modelGraph = readGraph("voronoi-manypointswithboundary.xml");
      assertTrue(PlanarGraphs.isIsomorphic(voronoiGraph, modelGraph));
      
      // Visual check     
      //JGraph jGraph = getDisplayGraph(voronoiGraph, voronoiGraphGenerator, siteGraph, new MapPlanarLayout<IdVertex>(siteCoordinates));
      //JComponentWindow window = new JComponentWindow(new JScrollPane(jGraph));              
      //window.showAndWait();
   }
   
   private JGraph getDisplayGraph(PlanarGraph<IdVertex, UndirectedIdEdge<IdVertex>> voronoiGraph, PlanarLayout<IdVertex> voronoiPlanarLayout,
                             PlanarGraph<IdVertex, UndirectedIdEdge<IdVertex>> delaunayGraph, PlanarLayout<IdVertex> delaunayPlanarLayout)
           throws InterruptedException, InvocationTargetException {
      
      // Visual check     
      AffineTransform screenProjection = AffineTransform.getTranslateInstance(300, 300);
      screenProjection.scale(100, -100); // Flip y-axis      
      
      Map<String, String> voronoiFixedAttributes = new HashMap<String, String>();
      voronoiFixedAttributes.put("shape", "circle");
      voronoiFixedAttributes.put("size", "5,5");     
      voronoiFixedAttributes.put("color", "#000000");
      voronoiFixedAttributes.put("fillcolor", "#AA0000");
      
      JGraph jVoronoiGraph = new JGraph(new JGraphModelAdapterExt(voronoiGraph,
                         new IdVertexNameProvider(),
                         null,
                         new PlanarLayoutPositionProvider(voronoiPlanarLayout, screenProjection, 
                            new FixedAttributeProvider(voronoiFixedAttributes)),
                         null));
      jVoronoiGraph.setEnabled(false);      
      jVoronoiGraph.setMinimumSize(jVoronoiGraph.getPreferredSize());      
      GraphLayoutCache voronoiCache = jVoronoiGraph.getGraphLayoutCache();
      voronoiCache.setFactory(new DefaultCellViewFactory() {
         @Override
         protected VertexView createVertexView(Object v) {
            return new MultiLineVertexView(v);
         }   
      });
      voronoiCache.reload();
      
      Map<String, String> delaunayFixedAttributes = new HashMap<String, String>();
      delaunayFixedAttributes.put("shape", "circle");
      delaunayFixedAttributes.put("size", "7,7");     
      delaunayFixedAttributes.put("color", "#000000");
      delaunayFixedAttributes.put("fillcolor", "#00FF00");
      
      JGraph jDelaunayGraph = new JGraph(new JGraphModelAdapterExt(delaunayGraph,
                         null, //new IdVertexNameProvider(),
                         null,
                         new PlanarLayoutPositionProvider(delaunayPlanarLayout, screenProjection, 
                            new FixedAttributeProvider(delaunayFixedAttributes)),
                         null));
      jDelaunayGraph.setEnabled(false);      
      jDelaunayGraph.setMinimumSize(jDelaunayGraph.getPreferredSize());            
      GraphLayoutCache delaunayCache = jDelaunayGraph.getGraphLayoutCache();
      delaunayCache.setFactory(new DefaultCellViewFactory() {
         @Override
         protected VertexView createVertexView(Object v) {
            return new MultiLineVertexView(v);
         }   
      });
      delaunayCache.reload();

      jDelaunayGraph.setBackgroundComponent(jVoronoiGraph);
      return jDelaunayGraph;      
   }
   
   private PlanarGraph<IdVertex, UndirectedIdEdge<IdVertex>> readGraph(String fileName)
           throws IOException, JAXBException {
            
      URL controlFile = this.getClass().getResource(fileName);
      InputStreamReader controlReader = new InputStreamReader(controlFile.openStream());
      JAXBContext context = JAXBContext.newInstance(XmlDcelDocument.class,
                 IdVertex.class,
                 UndirectedIdEdge.class,
                 UndirectedIdEdgeFactory.class,
                 IdFace.class,
                 IdFaceFactory.class);
      XmlDcelDocument document = new XmlDcelDocument();
      document.read(controlReader, context);
      
      
      return document.getDoublyConnectedEdgeList();      
   } 
   
   private void writeGraph(PlanarGraph<IdVertex, UndirectedIdEdge<IdVertex>> graph, String fileName)
           throws JAXBException, IOException {
      JAXBContext context = JAXBContext.newInstance(XmlDcelDocument.class,
                 IdVertex.class,
                 UndirectedIdEdge.class,
                 UndirectedIdEdgeFactory.class,
                 IdFace.class,
                 IdFaceFactory.class);
      XmlDcelDocument document = new XmlDcelDocument();
      document.setDoublyConnectedEdgeList((DoublyConnectedEdgeList<IdVertex, UndirectedIdEdge<IdVertex>, IdFace>) graph);
      String packagePath = this.getClass().getPackage().getName().replaceAll("\\.", "/");
      File outputFile = new File("src/test/resources/"
              + packagePath
              + "/" + fileName);
      FileWriter writer = new FileWriter(outputFile);
      document.write(writer, context);               
   }
}
