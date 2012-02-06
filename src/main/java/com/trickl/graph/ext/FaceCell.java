/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.trickl.graph.ext;

import java.awt.Point;
import java.util.List;
import org.jgraph.graph.DefaultGraphCell;

/**
 * @author tgee
 */
public class FaceCell extends DefaultGraphCell {
   private final List<Point> boundary;
   
   public FaceCell(List<Point> boundary) {      
      super();
      this.boundary = boundary;
   }
   
   public FaceCell(Object userObject, List<Point> boundary) {
      super(userObject);
      this.boundary = boundary;
   }

   /**
    * @return the boundary
    */
   public List<Point> getBoundary() {
      return boundary;
   }
}
