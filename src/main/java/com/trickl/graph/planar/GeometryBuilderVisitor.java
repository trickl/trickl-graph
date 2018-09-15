package com.trickl.graph.planar;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequenceFactory;
import java.util.LinkedList;
import java.util.List;

public class GeometryBuilderVisitor<V, E> implements PlanarFaceTraversalVisitor<V, E> {
   
   private final CoordinateList faceVertexCoordinates;
   private final PlanarGraph<V, E> graph;
   private final PlanarLayout<V> planarLayout;   
   private final CoordinateSequenceFactory coordinateSequenceFactory;
   private final GeometryFactory geometryFactory;
   private final List<Geometry> polygons;
   private Geometry geometry;

   public GeometryBuilderVisitor(PlanarGraph<V, E> graph, PlanarLayout<V> planarLayout)
   {
      this.graph = graph;
      this.planarLayout = planarLayout;
            
      faceVertexCoordinates = new CoordinateList();
      coordinateSequenceFactory = CoordinateArraySequenceFactory.instance();
      geometryFactory = new GeometryFactory(coordinateSequenceFactory);
      polygons = new LinkedList<>();
   }

   @Override
   public void beginTraversal() {    
   }

   @Override
   public void beginFace(V source, V target) {
      faceVertexCoordinates.clear();
   }

   @Override
   public void nextEdge(V source, V target) {      
   }

   @Override
   public void nextVertex(V vertex) {
      Coordinate location = planarLayout.getCoordinate(vertex);
      if (location != null && !Double.isNaN(location.x) && !Double.isNaN(location.y)) {
         faceVertexCoordinates.add(location);         
      }
   }

   @Override
   public void endFace(V source, V target) {
      boolean isBoundary = graph.isBoundary(source, target);
      if (faceVertexCoordinates.size() > 2 && !isBoundary)
      {                  
         // Create a linear ring for the boundary          
         faceVertexCoordinates.add(faceVertexCoordinates.get(0));
         CoordinateSequence coordinateSequence = coordinateSequenceFactory.create(faceVertexCoordinates.toCoordinateArray());
         LinearRing boundary = new LinearRing(coordinateSequence, geometryFactory);
         Polygon polygon = geometryFactory.createPolygon(boundary);
         polygons.add(polygon);                  
      }     
   }

   @Override
   public void endTraversal() {      
       geometry = geometryFactory.buildGeometry(polygons);
   }
   
   public Geometry getGeometry() {
       return geometry;
   }
}
