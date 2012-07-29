/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.trickl.graph.planar;

import com.jgraph.components.labels.MultiLineVertexView;
import com.trickl.graph.Labeller;
import com.trickl.graph.ext.FixedAttributeProvider;
import com.trickl.graph.ext.JGraphModelAdapterExt;
import com.trickl.graph.ext.VertexLabellerAttributeProvider;
import java.awt.geom.AffineTransform;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import org.jgraph.JGraph;
import org.jgraph.graph.DefaultCellViewFactory;
import org.jgraph.graph.GraphLayoutCache;
import org.jgraph.graph.VertexView;
import org.jgrapht.ext.ComponentAttributeProvider;
import org.jgrapht.ext.StringNameProvider;

/**
 *
 * @author tgee
 */
public final class JGraphAdaptor {
   private JGraphAdaptor() {};
   
   public static <V, E> JGraph getDisplayGraph(PlanarGraph<V, E> graph, PlanarLayout<V> layout)
           throws InterruptedException, InvocationTargetException {
      
      // Visual check     
      AffineTransform screenProjection = AffineTransform.getTranslateInstance(300, 300);
      screenProjection.scale(100, -100); // Flip y-axis            
      
      Map<String, String> vertexAttributes = new HashMap<String, String>();
      vertexAttributes.put("shape", "circle");
      vertexAttributes.put("size", "15,15");     
      vertexAttributes.put("color", "#000000");      
      
      JGraph jGraph = new JGraph(new JGraphModelAdapterExt(graph,
                         new StringNameProvider(),
                         null,
                         new PlanarLayoutPositionProvider(layout, screenProjection, 
                            new FixedAttributeProvider(vertexAttributes)),
                         null));
      jGraph.setEnabled(false);      
      jGraph.setMinimumSize(jGraph.getPreferredSize());            
      GraphLayoutCache layoutCache = jGraph.getGraphLayoutCache();
      layoutCache.setFactory(new DefaultCellViewFactory() {
         @Override
         protected VertexView createVertexView(Object v) {
            return new MultiLineVertexView(v);
         }   
      });
      layoutCache.reload();

      return jGraph;      
   }
        
   public static <V, E> JGraph getDisplayGraph(PlanarGraph<V, E> voronoiGraph, PlanarLayout<V> voronoiPlanarLayout,
                             PlanarGraph<V, E> delaunayGraph, PlanarLayout<V> delaunayPlanarLayout)
           throws InterruptedException, InvocationTargetException {
      
      // TODO: Remove code duplication with above function
      AffineTransform screenProjection = AffineTransform.getTranslateInstance(300, 300);
      screenProjection.scale(100, -100); // Flip y-axis      
      
      Map<String, String> voronoiFixedAttributes = new HashMap<String, String>();
      voronoiFixedAttributes.put("shape", "circle");
      voronoiFixedAttributes.put("size", "5,5");     
      voronoiFixedAttributes.put("color", "#000000");
      voronoiFixedAttributes.put("fillcolor", "#AA0000");
      
      JGraph jVoronoiGraph = new JGraph(new JGraphModelAdapterExt(voronoiGraph,
                         new StringNameProvider(),
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
                         null, 
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
   
   public static <V, E> JGraph getDisplayGraph(PlanarGraph<V, E> graph, PlanarLayout<V> layout, Labeller<V> labeller)
           throws InterruptedException, InvocationTargetException {
      
      // TODO: Factorise out code duplication in above methods  
      AffineTransform screenProjection = AffineTransform.getTranslateInstance(300, 300);
      screenProjection.scale(100, -100); // Flip y-axis            
      
      Map<String, String> vertexAttributes = new HashMap<String, String>();
      vertexAttributes.put("shape", "circle");
      vertexAttributes.put("size", "15,15");     
      vertexAttributes.put("color", "#000000");
      
      Map<String, String>[] labelAttributes = new Map[6];
      for (int i = 0; i < 6; ++i) {
         labelAttributes[i] = new HashMap<String, String>();
      }
      
      // Color vertices according to the label
      labelAttributes[0].put("fillcolor", "#CC0000");
      labelAttributes[1].put("fillcolor", "#00CC00");
      labelAttributes[2].put("fillcolor", "#0000CC");
      labelAttributes[3].put("fillcolor", "#AAAA00");
      labelAttributes[4].put("fillcolor", "#00AAAA");
      labelAttributes[5].put("fillcolor", "#AA00AA");
      
      Map<Integer, ComponentAttributeProvider<V>> labelAttributeProviders = new HashMap<Integer, ComponentAttributeProvider<V>>();
      for (int i = 0; i < 6; ++i) {
         labelAttributeProviders.put(i, new FixedAttributeProvider(labelAttributes[i]));
      }
      
      JGraph jGraph = new JGraph(new JGraphModelAdapterExt(graph,
                         null, //new StringNameProvider(),
                         null,
                         new PlanarLayoutPositionProvider(layout, screenProjection, 
                            new FixedAttributeProvider(vertexAttributes,
                              new VertexLabellerAttributeProvider<V>(labeller, labelAttributeProviders))),
                         null));
      jGraph.setEnabled(false);      
      jGraph.setMinimumSize(jGraph.getPreferredSize());            
      GraphLayoutCache layoutCache = jGraph.getGraphLayoutCache();
      layoutCache.setFactory(new DefaultCellViewFactory() {
         @Override
         protected VertexView createVertexView(Object v) {
            return new MultiLineVertexView(v);
         }   
      });
      layoutCache.reload();

      return jGraph;      
   }
}
