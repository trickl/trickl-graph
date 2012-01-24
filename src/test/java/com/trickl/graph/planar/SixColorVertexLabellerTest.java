/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.trickl.graph.planar;

import com.jgraph.components.labels.MultiLineVertexView;
import com.trickl.graph.Labeller;
import com.trickl.graph.edges.IntegerEdgeFactory;
import com.trickl.graph.edges.UndirectedEdge;
import com.trickl.graph.edges.UndirectedIdEdge;
import com.trickl.graph.edges.UndirectedIdEdgeFactory;
import com.trickl.graph.ext.FixedAttributeProvider;
import com.trickl.graph.ext.JComponentWindow;
import com.trickl.graph.ext.JGraphModelAdapterExt;
import com.trickl.graph.ext.VertexLabellerAttributeProvider;
import com.trickl.graph.planar.faces.IdFace;
import com.trickl.graph.planar.faces.IdFaceFactory;
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
import org.jgraph.graph.VertexView;
import org.jgrapht.ext.ComponentAttributeProvider;
import org.junit.Test;

/**
 *
 * @author tgee
 */
public class SixColorVertexLabellerTest {
   
   public SixColorVertexLabellerTest() {
   }

   @Test
   public void testEmptyGraphLabelling() {
      PlanarGraph<Integer, Integer> graph = new DoublyConnectedEdgeList<Integer, Integer, IdFace>(new IntegerEdgeFactory<Integer>(), new IdFaceFactory());
      SixColorVertexLabeller<Integer, Integer> labeller = new SixColorVertexLabeller<Integer, Integer>(graph);
      int labelCount = labeller.getLabelCount();
      Assert.assertEquals(0, labelCount);
   }
   
   @Test
   public void testTrivialGraphLabelling() throws InterruptedException, InvocationTargetException {
      DoublyConnectedEdgeList<Integer, Integer, Object> graph = new DoublyConnectedEdgeList<Integer, Integer, Object>(new IntegerEdgeFactory<Integer>(), Object.class);
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
      
      SixColorVertexLabeller labeller = new SixColorVertexLabeller(graph);
      int labelCount = labeller.getLabelCount();
      
      // Ensure three distinct labels are created
      Assert.assertEquals(3, labelCount);
      Assert.assertNotNull(labeller.getLabel(vertices.get(0)));
      Assert.assertNotNull(labeller.getLabel(vertices.get(1)));
      Assert.assertNotNull(labeller.getLabel(vertices.get(2)));
      Assert.assertTrue(labeller.getLabel(vertices.get(0)) != 
                        labeller.getLabel(vertices.get(1)));
      Assert.assertTrue(labeller.getLabel(vertices.get(0)) != 
                        labeller.getLabel(vertices.get(2)));
      Assert.assertTrue(labeller.getLabel(vertices.get(1)) != 
                        labeller.getLabel(vertices.get(2)));
      Assert.assertEquals(1,  labeller.getMembers(0).size());
      Assert.assertEquals(1,  labeller.getMembers(1).size());
      Assert.assertEquals(1,  labeller.getMembers(2).size());
    
      // Visual Check
      //JGraph jGraph = getDisplayGraph(graph, new ChrobakPayneLayout<Integer, Integer>(graph), labeller);
      //JComponentWindow window = new JComponentWindow(new JScrollPane(jGraph));              
      //window.showAndWait();
   }
   
   @Test
   public void testSmallMaximalGraphLabelling() throws InterruptedException, InvocationTargetException {
      DoublyConnectedEdgeList<Integer, Integer, Object> graph = new DoublyConnectedEdgeList<Integer, Integer, Object>(new IntegerEdgeFactory(), Object.class);
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
      
      SixColorVertexLabeller labeller = new SixColorVertexLabeller(graph);
      int labelCount = labeller.getLabelCount();
      
      // Check that no adjacent vertices have the same color
      Assert.assertTrue(labelCount < 6);
      for (Integer edge : graph.edgeSet()) {
         int sourceLabel = labeller.getLabel(graph.getEdgeSource(edge));
         int targetLabel = labeller.getLabel(graph.getEdgeTarget(edge));
         Assert.assertNotSame(sourceLabel, targetLabel);
      }
      
      // Visual Check
      //JGraph jGraph = getDisplayGraph(graph, new ChrobakPayneLayout<Integer, Integer>(graph, 0.5), labeller);
      //JComponentWindow window = new JComponentWindow(new JScrollPane(jGraph));              
      //window.showAndWait();
   }
   
   private JGraph getDisplayGraph(PlanarGraph<Integer, Integer> graph, PlanarLayout<Integer> layout, Labeller<Integer> labeller)
           throws InterruptedException, InvocationTargetException {
      
      // Visual check     
      AffineTransform screenProjection = AffineTransform.getTranslateInstance(300, 300);
      screenProjection.scale(100, -100); // Flip y-axis            
      
      Map<String, String> delaunayFixedAttributes = new HashMap<String, String>();
      delaunayFixedAttributes.put("shape", "circle");
      delaunayFixedAttributes.put("size", "15,15");     
      delaunayFixedAttributes.put("color", "#000000");
      
      Map<String, String>[] labelAttributes = new Map[6];
      for (int i = 0; i < 6; ++i) {
         labelAttributes[i] = new HashMap<String, String>();
      }
      
      // Color vertices according to the label
      labelAttributes[0].put("fillcolor", "#FF0000");
      labelAttributes[1].put("fillcolor", "#00FF00");
      labelAttributes[2].put("fillcolor", "#0000FF");
      labelAttributes[3].put("fillcolor", "#FFFF00");
      labelAttributes[4].put("fillcolor", "#00FFFF");
      labelAttributes[5].put("fillcolor", "#FF00FF");
      
      Map<Integer, ComponentAttributeProvider<Integer>> labelAttributeProviders = new HashMap<Integer, ComponentAttributeProvider<Integer>>();
      for (int i = 0; i < 6; ++i) {
         labelAttributeProviders.put(i, new FixedAttributeProvider(labelAttributes[i]));
      }
      
      JGraph jGraph = new JGraph(new JGraphModelAdapterExt(graph,
                         null, //new IdVertexNameProvider(),
                         null,
                         new PlanarLayoutPositionProvider(layout, screenProjection, 
                            new FixedAttributeProvider(delaunayFixedAttributes,
                              new VertexLabellerAttributeProvider<Integer>(labeller, labelAttributeProviders))),
                         null));
      jGraph.setEnabled(false);      
      jGraph.setMinimumSize(jGraph.getPreferredSize());            
      GraphLayoutCache delaunayCache = jGraph.getGraphLayoutCache();
      delaunayCache.setFactory(new DefaultCellViewFactory() {
         @Override
         protected VertexView createVertexView(Object v) {
            return new MultiLineVertexView(v);
         }   
      });
      delaunayCache.reload();

      return jGraph;      
   }
}