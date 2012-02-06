/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.trickl.graph.ext;

import com.jgraph.components.labels.CellConstants;
import com.jgraph.components.labels.MultiLineVertexRenderer;
import com.trickl.graph.edges.DirectedEdge;
import com.trickl.graph.planar.AbstractPlanarFaceTraversalVisitor;
import com.trickl.graph.planar.CanonicalPlanarFaceTraversal;
import com.trickl.graph.planar.PlanarFaceTraversal;
import com.trickl.graph.planar.PlanarGraph;
import com.trickl.graph.planar.PlanarGraphs;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.ParseException;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import org.jgraph.event.GraphModelEvent;
import org.jgraph.event.GraphModelEvent.GraphModelChange;
import org.jgraph.event.GraphModelListener;
import org.jgraph.graph.*;
import org.jgrapht.Graph;
import org.jgrapht.ext.ComponentAttributeProvider;
import org.jgrapht.ext.EdgeNameProvider;
import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.ext.VertexNameProvider;

/**
 *
 * @author tgee
 */
public class JGraphModelAdapterExt<V, E> extends JGraphModelAdapter {

   private PlanarGraph<V, E> graph;
   private VertexNameProvider<V> vertexLabelProvider;
   private EdgeNameProvider<E> edgeLabelProvider;
   private FaceNameProvider<V> faceLabelProvider;
   private ComponentAttributeProvider<V> vertexAttributeProvider;
   private ComponentAttributeProvider<E> edgeAttributeProvider;
   private ComponentAttributeProvider<DirectedEdge<V>> faceAttributeProvider;
   private AttributeMap defaultFaceAttributes;
   private final Map<DirectedEdge<V>, GraphCell> faceToCell = new HashMap<DirectedEdge<V>, GraphCell>();
   private final Map<GraphCell, DirectedEdge<V>> cellToFace = new HashMap<GraphCell, DirectedEdge<V>>();
   final private Pattern pointPattern = Pattern.compile("([-+]?[0-9]*\\.?[0-9]+)\\s*,\\s*([-+]?[0-9]*\\.?[0-9]+)[!]?");
   final private Pattern dimensionPattern = Pattern.compile("([0-9]+)\\s*,\\s*([0-9]+)");
   final private Pattern colorPattern = Pattern.compile("#([0-9a-fA-F][0-9a-fA-F])([0-9a-fA-F][0-9a-fA-F])([0-9a-fA-F][0-9a-fA-F])([0-9a-fA-F][0-9a-fA-F])?");

   public JGraphModelAdapterExt(PlanarGraph<V, E> graph,
           VertexNameProvider<V> vertexLabelProvider,
           EdgeNameProvider<E> edgeLabelProvider,
           ComponentAttributeProvider<V> vertexAttributeProvider,
           ComponentAttributeProvider<E> edgeAttributeProvider) {
      this(graph, vertexLabelProvider, edgeLabelProvider, null, vertexAttributeProvider, edgeAttributeProvider, null);
   }

   public JGraphModelAdapterExt(PlanarGraph<V, E> graph,
           VertexNameProvider<V> vertexLabelProvider,
           EdgeNameProvider<E> edgeLabelProvider,
           FaceNameProvider<V> faceLabelProvider,
           ComponentAttributeProvider<V> vertexAttributeProvider,
           ComponentAttributeProvider<E> edgeAttributeProvider,
           ComponentAttributeProvider<DirectedEdge<V>> faceAttributeProvider) {
      super(graph);
      this.graph = graph;
      this.vertexLabelProvider = vertexLabelProvider;
      this.edgeLabelProvider = edgeLabelProvider;
      this.faceLabelProvider = faceLabelProvider;
      this.vertexAttributeProvider = vertexAttributeProvider;
      this.edgeAttributeProvider = edgeAttributeProvider;
      this.faceAttributeProvider = faceAttributeProvider;
      
      defaultFaceAttributes = createDefaultFaceAttributes();
            
      // A nasty hack to use a custom listener so we can filter the face vertices
      // If only "internalAddVertex" was protected not private... 
      for (GraphModelListener listener : getGraphModelListeners()) {
         this.removeGraphModelListener(listener);
      }

      // We're unable to set these on vertex creation correctly, due to visibility
      // issues. So we'll update them now. Note, this workaround means that the correct
      // dynamic labels and attributes are not set correctly for modified graphs.
      updateVertexLabels();
      updateEdgeLabels();
      updateVertexAttributes();
      updateEdgeAttributes();      
            
      // Traverse all faces and add face cells
      PlanarFaceTraversal<V, E> planarFaceTraversal = new CanonicalPlanarFaceTraversal<V, E>(graph);
      planarFaceTraversal.traverse(new AbstractPlanarFaceTraversalVisitor<V, E>() {

         @Override
         public void beginFace(V source, V target) {
            handleJGraphTAddedFace(new DirectedEdge<V>(source, target));
         }
      });
   }

   private void updateEdgeLabels() {
      for (E edge : graph.edgeSet()) {
         DefaultEdge cell = getEdgeCell(edge);
         String label = edgeLabelProvider == null ? "" : edgeLabelProvider.getEdgeName(edge);
         cell.setUserObject(label == null ? "" : label);
      }
   }

   private void updateVertexLabels() {
      for (V vertex : graph.vertexSet()) {
         DefaultGraphCell cell = getVertexCell(vertex);
         String label = vertexLabelProvider == null ? "" : vertexLabelProvider.getVertexName(vertex);
         cell.setUserObject(label == null ? "" : label);
      }
   }

   private void updateVertexAttributes() {
      for (V vertex : graph.vertexSet()) {
         DefaultGraphCell cell = getVertexCell(vertex);
         AttributeMap cellAttributes = cell.getAttributes();
         cellAttributes.cloneEntries(getVertexAttributeMap(vertex, cell));
      }
   }

   private void updateEdgeAttributes() {
      for (E edge : graph.edgeSet()) {
         DefaultEdge cell = getEdgeCell(edge);
         AttributeMap cellAttributes = cell.getAttributes();
         cellAttributes.cloneEntries(getEdgeAttributeMap(edge, cell));
      }
   }

   private Point2D.Double parsePoint(String pointString) throws ParseException {
      Matcher matcher = pointPattern.matcher(pointString);
      if (matcher.matches()) {
         return new Point2D.Double(Double.parseDouble(matcher.group(1)), Double.parseDouble(matcher.group(2)));
      } else {
         throw new ParseException("Could not parse point '" + pointString + "'", 0);
      }
   }

   private Dimension parseDimension(String dimensionString) throws ParseException {
      Matcher matcher = dimensionPattern.matcher(dimensionString);
      if (matcher.matches()) {
         return new Dimension(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)));
      } else {
         throw new ParseException("Could not parse dimension '" + dimensionString + "'", 0);
      }
   }

   private int parseMultiLineVertexRendererShape(String shapeString) throws ParseException {
      shapeString = shapeString.toLowerCase();
      if ("circle".equals(shapeString)) {
         return MultiLineVertexRenderer.SHAPE_CIRCLE;
      } else if ("cylinder".equals(shapeString)) {
         return MultiLineVertexRenderer.SHAPE_CYLINDER;
      } else if ("diamond".equals(shapeString)) {
         return MultiLineVertexRenderer.SHAPE_DIAMOND;
      } else if ("box".equals(shapeString)) {
         return MultiLineVertexRenderer.SHAPE_RECTANGLE;
      } else if ("rounded".equals(shapeString)) {
         return MultiLineVertexRenderer.SHAPE_ROUNDED;
      } else if ("triangle".equals(shapeString)) {
         return MultiLineVertexRenderer.SHAPE_TRIANGLE;
      } else {
         throw new ParseException("Could not parse shape '" + shapeString + "'", 0);
      }
   }

   /**
    * Creates and returns a map of attributes to be used as defaults for face
    *
    * @return a map of attributes to be used as defaults for vertex attributes.
    */
   public static AttributeMap createDefaultFaceAttributes() {
      AttributeMap map = new AttributeMap();
      Color c = Color.decode("#DDDDDD");
      GraphConstants.setBackground(map, c);
      GraphConstants.setForeground(map, Color.white);
      GraphConstants.setFont(
              map,
              GraphConstants.DEFAULTFONT.deriveFont(Font.BOLD, 12));
      GraphConstants.setOpaque(map, true);

      return map;
   }
   
   public AttributeMap getDefaultFaceAttributes() {
      return defaultFaceAttributes;
   }

   protected AttributeMap getVertexAttributeMap(V vertex, DefaultGraphCell cell) {
      AttributeMap attrs = cell.getAttributes();      
      attrs.putAll(attrs.cloneEntries(getDefaultVertexAttributes()));      
      if (vertexAttributeProvider != null) {
         Map<String, String> vertexAttributes = vertexAttributeProvider.getComponentAttributes(vertex);

         for (String attributeKey : vertexAttributes.keySet()) {
            Object attributeValue = vertexAttributes.get(attributeKey);
            try {
               if ("size".equals(attributeKey)) {
                  Dimension dimension = parseDimension(attributeValue.toString());
                  Rectangle2D bounds = GraphConstants.getBounds(attrs);
                  GraphConstants.setBounds(attrs,
                          new Rectangle((int) bounds.getX(), (int) bounds.getY(), dimension.width, dimension.height));
               } else if ("width".equals(attributeKey)) {
                  int vertexWidth = Integer.parseInt(attributeValue.toString());
                  Rectangle2D bounds = GraphConstants.getBounds(attrs);
                  GraphConstants.setBounds(attrs,
                          new Rectangle((int) bounds.getX(), (int) bounds.getY(), vertexWidth, (int) bounds.getHeight()));
               } else if ("height".equals(attributeKey)) {
                  int vertexHeight = Integer.parseInt(attributeValue.toString());
                  Rectangle2D bounds = GraphConstants.getBounds(attrs);
                  GraphConstants.setBounds(attrs,
                          new Rectangle((int) bounds.getX(), (int) bounds.getY(), (int) bounds.getWidth(), vertexHeight));
               } else if ("pos".equals(attributeKey)) {
                  Point2D.Double point = parsePoint(attributeValue.toString());
                  Rectangle2D bounds = GraphConstants.getBounds(attrs);
                  GraphConstants.setBounds(attrs,
                          new Rectangle((int) point.x, (int) point.y, (int) bounds.getWidth(), (int) bounds.getHeight()));
               } else if ("color".equals(attributeKey)) {
                  attrs.applyValue(GraphConstants.BORDERCOLOR, Color.decode(attributeValue.toString()));
               } else if ("fillcolor".equals(attributeKey)) {
                  attrs.applyValue(GraphConstants.BACKGROUND, Color.decode(attributeValue.toString()));
               } else if ("shape".equals(attributeKey)) {
                  attrs.applyValue(CellConstants.VERTEXSHAPE, parseMultiLineVertexRendererShape(attributeValue.toString()));
               }
            } catch (ParseException ex) {
               // Silently ignore?
            }
         }
      }
      return attrs;
   }

   protected AttributeMap getEdgeAttributeMap(E edge, DefaultGraphCell cell) {
      AttributeMap attrs = cell.getAttributes();
      attrs.putAll(attrs.cloneEntries(getDefaultEdgeAttributes()));
      if (edgeAttributeProvider != null) {
         Map<String, String> edgeAttributes = edgeAttributeProvider.getComponentAttributes(edge);
         for (String attributeKey : edgeAttributes.keySet()) {
            Object attributeValue = edgeAttributes.get(attributeKey);

            if ("color".equals(attributeKey)) {
               attrs.applyValue(GraphConstants.BACKGROUND, Color.decode(attributeValue.toString()));
            }
         }
      }
      return attrs;
   }

   protected AttributeMap getFaceAttributeMap(DirectedEdge<V> face, DefaultGraphCell cell) {
      AttributeMap attrs = cell.getAttributes();
      attrs.putAll(attrs.cloneEntries(getDefaultFaceAttributes()));
      if (faceAttributeProvider != null) {
         Map<String, String> faceAttributes = faceAttributeProvider.getComponentAttributes(face);
         for (String attributeKey : faceAttributes.keySet()) {
            Object attributeValue = faceAttributes.get(attributeKey);

            if ("color".equals(attributeKey)) {
               attrs.applyValue(GraphConstants.BORDERCOLOR, Color.decode(attributeValue.toString()));
            } else if ("fillcolor".equals(attributeKey)) {
               attrs.applyValue(GraphConstants.BACKGROUND, Color.decode(attributeValue.toString()));
            }
         }
      }
      return attrs;
   }

   public DefaultGraphCell getFaceCell(DirectedEdge<V> face) {
      return (DefaultGraphCell) faceToCell.get(face);
   }

   void handleJGraphTAddedFace(DirectedEdge<V> jtFace) {
      
      if (graph.isBoundary(jtFace.getSource(), jtFace.getTarget())) return;

      List<V> vertices = PlanarGraphs.getVerticesOnFace((PlanarGraph<V, E>) graph, jtFace.getSource(), jtFace.getTarget());
      List<Point> boundary = new LinkedList<Point>();
      for (V vertex : vertices) {
         DefaultGraphCell vertexCell = getVertexCell(vertex);
         AttributeMap vertexAttrs = vertexCell.getAttributes();
         Rectangle2D vertexBounds = GraphConstants.getBounds(vertexAttrs);
         boundary.add(new Point((int) vertexBounds.getCenterX(), (int) vertexBounds.getCenterY()));
      }

      DefaultGraphCell faceCell = new FaceCell(jtFace, boundary);

      faceToCell.put(jtFace, faceCell);
      cellToFace.put(faceCell, jtFace);

      AttributeMap attributeMap = new AttributeMap();
      attributeMap.put(faceCell, getFaceAttributeMap(jtFace, faceCell).clone());
      insert(new Object[]{faceCell}, attributeMap, null, null, null);
   }

   void handleJGraphTRemoveFace(DirectedEdge<V> jtFace) {
      DefaultGraphCell faceCell =
              (DefaultGraphCell) faceToCell.remove(jtFace);
      cellToFace.remove(faceCell);

      remove(new Object[]{faceCell});
   }
}
