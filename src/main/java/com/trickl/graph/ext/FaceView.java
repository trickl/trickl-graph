package com.trickl.graph.ext;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import org.jgraph.graph.*;

public class FaceView extends VertexView {
      
   // Define the Renderer for an FaceView
   static class PolygonRenderer extends VertexRenderer {

      @Override
      public void paint(Graphics g) {
         
         List<Point> boundary = ((FaceCell) view.getCell()).getBoundary();
         if (boundary != null) {                     
            int[] xPoints = new int[boundary.size()];
            int[] yPoints = new int[boundary.size()];
            int i = 0;
            for (Point2D pt : boundary) {
               xPoints[i] = (int) pt.getX() - this.getBounds().x;
               yPoints[i] = (int) pt.getY() - this.getBounds().y;
               ++i;
            }

            // if the GraphCell is set opaque (via GraphConstants.setOpaque(),
            // then paint a background. If a gradient color is set and it is not
            // the preview (during drag&drop of the cell) paint a gradient pane         
            if (super.isOpaque()) {
               g.setColor(super.getBackground());       
               g.fillPolygon(xPoints, yPoints, xPoints.length);
            }
         }
      }
   }

   static PolygonRenderer polygonRenderer = new PolygonRenderer();

   // Constructor for Superclass
   public FaceView(FaceCell cell) {
      super(cell);
   }   

   @Override
   public CellViewRenderer getRenderer() {
      return polygonRenderer;
   }
   
   @Override
   public Rectangle2D getBounds() {      
      int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
      for (Point pt : ((FaceCell) cell).getBoundary()) {
         minX = Math.min(pt.x, minX);
         minY = Math.min(pt.y, minY);
         maxX = Math.max(pt.x, maxX);
         maxY = Math.max(pt.y, maxY);         
      }
      return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);      
   }
}
