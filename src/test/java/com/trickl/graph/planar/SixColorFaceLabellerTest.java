/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.trickl.graph.planar;

import com.jgraph.components.labels.MultiLineVertexView;
import com.trickl.graph.Labeller;
import com.trickl.graph.edges.DirectedEdge;
import com.trickl.graph.edges.IntegerEdgeFactory;
import com.trickl.graph.ext.*;
import com.trickl.graph.planar.faces.IdFace;
import com.trickl.graph.planar.faces.IdFaceFactory;
import com.trickl.graph.planar.generate.PlanarCircleGraphGenerator;
import com.trickl.graph.vertices.IntegerVertexFactory;
import java.awt.geom.AffineTransform;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JScrollPane;
import junit.framework.Assert;
import org.jgraph.JGraph;
import org.jgraph.graph.DefaultCellViewFactory;
import org.jgraph.graph.GraphLayoutCache;
import org.jgraph.graph.PortView;
import org.jgraph.graph.VertexView;
import org.jgrapht.ext.ComponentAttributeProvider;
import org.jgrapht.ext.StringNameProvider;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author tgee
 */
public class SixColorFaceLabellerTest {

   public SixColorFaceLabellerTest() {
   }

   @Test
   @Ignore
   public void testEmptyGraphLabelling() {
      PlanarFaceGraph<Integer, Integer, IdFace> graph = new DoublyConnectedEdgeList<>(new IntegerEdgeFactory<Integer>(), new IdFaceFactory());
      SixColorFaceLabeller<Integer, Integer, IdFace> labeller = new SixColorFaceLabeller<>(graph);
      int labelCount = labeller.getLabelCount();

      // Ensure there is just one label, for the boundary face
      Assert.assertEquals(1, labelCount);
   }

   @Test
   @Ignore
   public void testTrivialGraphLabelling() throws InterruptedException, InvocationTargetException {
      PlanarFaceGraph<Integer, Integer, IdFace> graph = new DoublyConnectedEdgeList<>(new IntegerEdgeFactory<Integer>(), new IdFaceFactory());
      IntegerVertexFactory vertexFactory = new IntegerVertexFactory();
      ArrayList<Integer> vertices = new ArrayList<Integer>(3);
      for (int i = 0; i < 3; ++i) {
         Integer vertex = vertexFactory.createVertex();
         graph.addVertex(vertex);
         vertices.add(vertex);
      }

      // A triangle
      graph.addEdge(vertices.get(0), vertices.get(1));
      graph.addEdge(vertices.get(0), vertices.get(2));
      graph.addEdge(vertices.get(1), vertices.get(2));

      SixColorFaceLabeller<Integer, Integer, IdFace> labeller = new SixColorFaceLabeller<>(graph);
      int labelCount = labeller.getLabelCount();

      // Visual Check
      if (Boolean.parseBoolean(System.getProperty("visualTests"))) {
        JGraph jGraph = getDisplayGraph(graph, new ChrobakPayneLayout<Integer, Integer>(graph), labeller);
        JComponentWindow window = new JComponentWindow(new JScrollPane(jGraph));              
        window.showAndWait();
      }
   }

   @Test
   public void testSmallMaximalGraphLabelling() throws InterruptedException, InvocationTargetException {
      PlanarFaceGraph<Integer, Integer, IdFace> graph = new DoublyConnectedEdgeList<>(new IntegerEdgeFactory<Integer>(), new IdFaceFactory());
      IntegerVertexFactory vertexFactory = new IntegerVertexFactory();
      ArrayList<Integer> vertices = new ArrayList<Integer>(6);
      for (int i = 0; i < 6; ++i) {
         Integer vertex = vertexFactory.createVertex();
         graph.addVertex(vertex);
         vertices.add(vertex);
      }

      // A maximally connected group of five vertices
      graph.addEdge(vertices.get(0), vertices.get(1));
      graph.addEdge(vertices.get(0), vertices.get(2));
      graph.addEdge(vertices.get(1), vertices.get(2), vertices.get(0), vertices.get(0));
      graph.addEdge(vertices.get(1), vertices.get(3), vertices.get(0), null);
      graph.addEdge(vertices.get(3), vertices.get(2), vertices.get(1), vertices.get(0));
      graph.addEdge(vertices.get(3), vertices.get(0), vertices.get(1), vertices.get(1));
      graph.addEdge(vertices.get(1), vertices.get(4), vertices.get(0), null);
      graph.addEdge(vertices.get(4), vertices.get(3), vertices.get(1), vertices.get(0));
      graph.addEdge(vertices.get(0), vertices.get(4), vertices.get(3), vertices.get(3));
      graph.addEdge(vertices.get(4), vertices.get(5), vertices.get(0), null);
      graph.addEdge(vertices.get(5), vertices.get(3), vertices.get(4), vertices.get(0));
      graph.addEdge(vertices.get(0), vertices.get(5), vertices.get(3), vertices.get(3));

      SixColorFaceLabeller<Integer, Integer, IdFace> labeller = new SixColorFaceLabeller<>(graph);
      int labelCount = labeller.getLabelCount();

      if (Boolean.parseBoolean(System.getProperty("visualTests"))) {
        JGraph jGraph = getDisplayGraph(graph, new ChrobakPayneLayout<Integer, Integer>(graph), labeller);
        JComponentWindow window = new JComponentWindow(new JScrollPane(jGraph));              
        window.showAndWait();
      }
   }

   @Test
   public void testMediumRegularGraphLabelling() throws InterruptedException, InvocationTargetException {
      PlanarFaceGraph<Integer, Integer, IdFace> graph = new DoublyConnectedEdgeList<>(new IntegerEdgeFactory<Integer>(), new IdFaceFactory());
      PlanarCircleGraphGenerator<Integer, Integer> generator = new PlanarCircleGraphGenerator<Integer, Integer>(37, 0.2);

      generator.generateGraph(graph, new IntegerVertexFactory(), null);
      SixColorFaceLabeller<Integer, Integer, IdFace> labeller = new SixColorFaceLabeller<>(graph);
      int labelCount = labeller.getLabelCount();

      // Visual Check
      if (Boolean.parseBoolean(System.getProperty("visualTests"))) {
        JGraph jGraph = getDisplayGraph(graph, new ChrobakPayneLayout<Integer, Integer>(graph), labeller);
        JComponentWindow window = new JComponentWindow(new JScrollPane(jGraph));              
        window.showAndWait();
      }
   }

   private JGraph getDisplayGraph(PlanarGraph<Integer, Integer> graph, PlanarLayout<Integer> layout, Labeller<DirectedEdge<Integer>> labeller)
           throws InterruptedException, InvocationTargetException {

      // Visual check     
      AffineTransform screenProjection = AffineTransform.getTranslateInstance(300, 300);
      screenProjection.scale(100, -100); // Flip y-axis            

      Map<String, String> vertexAttributes = new HashMap<String, String>();
      vertexAttributes.put("shape", "circle");
      vertexAttributes.put("size", "5,5");
      vertexAttributes.put("color", "#000000");
      vertexAttributes.put("fillcolor", "#777777");

      Map<String, String>[] labelAttributes = new Map[6];
      for (int i = 0; i < 6; ++i) {
         labelAttributes[i] = new HashMap<String, String>();
      }

      // Color vertices according to the label
      labelAttributes[0].put("fillcolor", "#CC4444");
      labelAttributes[1].put("fillcolor", "#44CC44");
      labelAttributes[2].put("fillcolor", "#4444CC");
      labelAttributes[3].put("fillcolor", "#AAAA44");
      labelAttributes[4].put("fillcolor", "#44AAAA");
      labelAttributes[5].put("fillcolor", "#AA44AA");

      Map<Integer, ComponentAttributeProvider<DirectedEdge<Integer>>> labelAttributeProviders = new HashMap<Integer, ComponentAttributeProvider<DirectedEdge<Integer>>>();
      for (int i = 0; i < 6; ++i) {
         labelAttributeProviders.put(i, new FixedAttributeProvider(labelAttributes[i]));
      }

      JGraph jGraph = new JGraph(new JGraphModelAdapterExt(graph,
              null, //new StringNameProvider(),
              null,
              null,
              new PlanarLayoutPositionProvider(layout, screenProjection,
              new FixedAttributeProvider(vertexAttributes)),
              null,
              new FaceLabellerAttributeProvider<Integer>(labeller, labelAttributeProviders)));
      jGraph.setEnabled(false);
      jGraph.setMinimumSize(jGraph.getPreferredSize());
      GraphLayoutCache layoutCache = jGraph.getGraphLayoutCache();
      layoutCache.setFactory(new DefaultCellViewFactory() {

         @Override
         protected VertexView createVertexView(Object v) {
            if (v instanceof FaceCell) {
               return new FaceView((FaceCell) v);
            } else {
               return new MultiLineVertexView(v);
            }
         }
      });
      layoutCache.reload();

      return jGraph;
   }
}
