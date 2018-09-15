/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.trickl.graph.planar;

import com.trickl.graph.edges.IntegerEdgeFactory;
import com.trickl.graph.ext.JComponentWindow;
import com.trickl.graph.planar.faces.IdFace;
import com.trickl.graph.planar.faces.IdFaceFactory;
import com.trickl.graph.planar.generate.PlanarCircleGraphGenerator;
import com.trickl.graph.vertices.IntegerVertexFactory;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import javax.swing.JScrollPane;
import junit.framework.Assert;
import org.jgraph.JGraph;
import org.junit.Ignore;
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
      if (Boolean.parseBoolean(System.getProperty("visualTests"))) {
        JGraph jGraph = JGraphAdaptor.getDisplayGraph(graph, new ChrobakPayneLayout<Integer, Integer>(graph), labeller);
        JComponentWindow window = new JComponentWindow(new JScrollPane(jGraph));              
        window.showAndWait();
      }
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
      if (Boolean.parseBoolean(System.getProperty("visualTests"))) {
        JGraph jGraph = JGraphAdaptor.getDisplayGraph(graph, new ChrobakPayneLayout<Integer, Integer>(graph), labeller);
        JComponentWindow window = new JComponentWindow(new JScrollPane(jGraph));              
        window.showAndWait();
      }
   }
   
   @Test
   public void testMediumRegularGraphLabelling() throws InterruptedException, InvocationTargetException {
      DoublyConnectedEdgeList<Integer, Integer, Object> graph = new DoublyConnectedEdgeList<Integer, Integer, Object>(new IntegerEdgeFactory(), Object.class);
      PlanarCircleGraphGenerator<Integer, Integer> generator = new PlanarCircleGraphGenerator<Integer, Integer>(37, 0.2);
      
      generator.generateGraph(graph, new IntegerVertexFactory(), null);            
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
      if (Boolean.parseBoolean(System.getProperty("visualTests"))) {
        JGraph jGraph = JGraphAdaptor.getDisplayGraph(graph, new ChrobakPayneLayout<Integer, Integer>(graph), labeller);
        JComponentWindow window = new JComponentWindow(new JScrollPane(jGraph));              
        window.showAndWait();
      }
   }      
}
