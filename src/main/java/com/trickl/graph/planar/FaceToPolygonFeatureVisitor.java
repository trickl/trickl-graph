package com.trickl.graph.planar;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequenceFactory;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import org.wololo.geojson.Feature;
import org.wololo.jts2geojson.GeoJSONWriter;

public class FaceToPolygonFeatureVisitor<V, E> implements PlanarFaceTraversalVisitor<V, E> {
   
   private final CoordinateList faceVertexCoordinates;
   private final PlanarGraph<V, E> graph;
   private final PlanarLayout<V> planarLayout;   
   private final CoordinateSequenceFactory coordinateSequenceFactory;
   private final GeometryFactory geometryFactory;
   private final List<Feature> features;
   private final BiFunction<V, V, HashMap> propertySelector;
   private final GeoJSONWriter geoJSONWriter;

   public FaceToPolygonFeatureVisitor(PlanarGraph<V, E> graph, PlanarLayout<V> planarLayout, BiFunction<V, V, HashMap> propertySelector)
   {
      this.graph = graph;
      this.planarLayout = planarLayout;
      this.propertySelector = propertySelector;
            
      geoJSONWriter = new GeoJSONWriter();
      faceVertexCoordinates = new CoordinateList();
      coordinateSequenceFactory = CoordinateArraySequenceFactory.instance();
      geometryFactory = new GeometryFactory(coordinateSequenceFactory);
      features = new LinkedList<>();      
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
         HashMap properties = propertySelector != null ? propertySelector.apply(source, target) : null;
         Feature feature = new Feature(geoJSONWriter.write(polygon), properties);         
         features.add(feature);
      }     
   }

   @Override
   public void endTraversal() {      
   }
   
   public List<Feature> getFeaturesList() {
       return features;
   }
}
