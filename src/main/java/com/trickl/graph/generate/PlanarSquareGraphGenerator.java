package com.trickl.graph.generate;

import com.trickl.graph.planar.PlanarGraph;
import com.trickl.graph.planar.PlanarLayout;
import com.vividsolutions.jts.geom.Coordinate;
import java.util.Map;
import java.util.Hashtable;
import java.util.ArrayList;

import org.jgrapht.VertexFactory;

public class PlanarSquareGraphGenerator<V, E> 
        implements PlanarGraphGenerator<V, E, V>, PlanarLayout<V> {

   private Map<V, Coordinate> positions = new Hashtable<V, Coordinate>();

   private ArrayList<V> vertices = new ArrayList<V>();      
   
   private final int size;
   private final double scale;

   public PlanarSquareGraphGenerator(int size)
   {
      this(size, 100);
   }

   public PlanarSquareGraphGenerator(int size, double scale)
   {
      this.size = size;
      this.scale = scale;
   }

   @Override
   public void generateGraph(PlanarGraph<V,E> graph, VertexFactory<V> vertexFactory,
           java.util.Map<java.lang.String,V> resultMap)
   {      
      int width = ((int) Math.sqrt(size - 1)) + 1;

      for (int k = 0; k < size; ++k)
      {
         V vertex = vertexFactory.createVertex();
         vertices.add(vertex);
         graph.addVertex(vertex);
      }

      for (int k = 0; k < size; ++k)
      {         
         V vertex = vertices.get(k);
         
         int i = k % width;
         int j = k / width;

         // Build upwards (positive j = top)
         int k_left = (i - 1) + j * width;
         int k_right = (i + 1) + j * width;
         int k_top = i + (j + 1) * width;
         int k_bottom = i + (j - 1) * width;

         V bottom = null;
         V left = null;
         V top = null;
         V right = null;

         // Add edges cyclically to ensure planarity
         if (j != 0 && k_bottom < size) {
            bottom = vertices.get(k_bottom);
         }
         if (i != 0 && k_left < size) {
            left = vertices.get(k_left);
         }
         if (j != width - 1 && k_top < size) {
            top = vertices.get(k_top);
         }
         if (i != width - 1 && k_right < size) {
            right = vertices.get(k_right);
         }
                  
         if (right != null) graph.addEdge(vertex, right, bottom, null);
         if (top != null) graph.addEdge(vertex, top, right == null ? bottom : right, null);
         if (left != null) graph.addEdge(vertex, left, top, null);
         if (bottom != null) graph.addEdge(vertex, bottom, left, null);
                
         positions.put(vertex, new Coordinate(i * scale, j * scale));
      }
   }

   @Override
   public Coordinate getCoordinate(V vertex) {
      return positions.get(vertex);
   }
}
