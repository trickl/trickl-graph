package com.trickl.graph.ext;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.jgraph.graph.*;

public class HexagonVertexView extends VertexView {
   // Define the Renderer for an HexagonView

   static class HexagonRenderer extends VertexRenderer {

      @Override
      public void paint(Graphics g) {

         Graphics2D g2D = (Graphics2D) g;
         Dimension size = getSize();
         boolean wasSelected = selected;

         int interiorWidth = size.width + 2 * borderWidth;
         int interiorHeight = size.height - borderWidth;
         int[] xPoints = {-borderWidth,
            -borderWidth + interiorWidth / 4,
            -borderWidth + (interiorWidth * 3) / 4,
            -borderWidth + interiorWidth,
            -borderWidth +(interiorWidth * 3) / 4,
            -borderWidth + interiorWidth / 4};
         int[] yPoints = {interiorHeight / 2,
            0,
            0,
            interiorHeight / 2,
            interiorHeight,
            interiorHeight};

         // if the GraphCell is set opaque (via GraphConstants.setOpaque(),
         // then paint a background. If a gradient color is set and it is not
         // the preview (during drag&drop of the cell) paint a gradient pane
         if (super.isOpaque()) {
            g.setColor(super.getBackground());
            if (gradientColor != null && !preview) {
               setOpaque(false);
               g2D.setPaint(new GradientPaint(0, 0, getBackground(),
                       getWidth(), getHeight(), gradientColor, true));
            }
            g.fillPolygon(xPoints, yPoints, xPoints.length);
         }

         try {
            setBorder(null);
            setOpaque(false);
            selected = false;
            super.paint(g);
         } finally {
            selected = wasSelected;
         }

         if (!selected)
         {
            if (bordercolor == null) bordercolor = Color.BLACK;

            g.setColor(bordercolor);
            g2D.setStroke(new BasicStroke(borderWidth));
            g.drawPolygon(xPoints, yPoints, xPoints.length);
         }
         else {
            g2D.setStroke(GraphConstants.SELECTION_STROKE);
            g.setColor(highlightColor);
            g.drawPolygon(xPoints, yPoints, xPoints.length);
         }
      }

      /**
       * Returns the intersection of the bounding rectangle and the straight line
       * between the source and the specified point p. The specified point is
       * expected not to intersect the bounds.
       */
      @Override
      public Point2D getPerimeterPoint(VertexView view, Point2D source, Point2D p) {
         Rectangle2D bounds = view.getBounds();
         double x = bounds.getX();
         double y = bounds.getY();
         double width = bounds.getWidth();
         double height = bounds.getHeight();
         double xCenter = x + width / 2;
         double yCenter = y + height / 2;
         double dx = p.getX() - xCenter; // Compute Angle
         double dy = p.getY() - yCenter;
         double alpha = Math.atan2(dy, dx);
         double beta = (Math.PI / 2.) - alpha;
         double theta = Math.atan2(height, width / 2);
         double gamma = Math.PI - alpha - Math.PI / 3;
         double delta = Math.PI + alpha - Math.PI / 3;
         double xout = 0, yout = 0;
         if (alpha > Math.PI - theta) { // Bottom Left
            xout = x - width * Math.sin(alpha) / (4 * Math.sin(delta));
            yout = yCenter - Math.sqrt(3) * width * Math.sin(alpha) / (4 * Math.sin(delta));
         } else if (alpha < -Math.PI + theta) { // Top Left
            xout = x + width * Math.sin(alpha) / (4 * Math.sin(gamma));
            yout = yCenter - Math.sqrt(3) * width * Math.sin(alpha) / (4 * Math.sin(gamma));
         } else if (alpha < -theta) { // Top
            xout = xCenter - height * Math.tan(beta) / 2;
            yout = y;
         } else if (alpha > theta) { // Bottom
            xout = xCenter + height * Math.tan(beta) / 2;
            yout = y + height;
         } else if (alpha > 0) { // Bottom Right
            xout = x + width - width * Math.sin(alpha) / (4 * Math.sin(gamma));
            yout = yCenter + Math.sqrt(3) * width * Math.sin(alpha) / (4 * Math.sin(gamma));
         } else if (alpha < 0) { // Top Right
            xout = x + width + width * Math.sin(alpha) / (4 * Math.sin(delta));
            yout = yCenter + Math.sqrt(3) * width * Math.sin(alpha) / (4 * Math.sin(delta));
         }
         
         return new Point2D.Double(xout, yout);
      }
   }

   static HexagonRenderer hexagonRenderer = new HexagonRenderer();

   // Constructor for Superclass
   public HexagonVertexView(Object cell) {
      super(cell);
   }
   // Returns Perimeter Point for Hexagons

   @Override
   public Point2D getPerimeterPoint(EdgeView edge, Point2D source, Point2D p) {
      return hexagonRenderer.getPerimeterPoint(this, source, p);
   }
   // Returns the Renderer for this View

   @Override
   public CellViewRenderer getRenderer() {
      return hexagonRenderer;
   }
}
