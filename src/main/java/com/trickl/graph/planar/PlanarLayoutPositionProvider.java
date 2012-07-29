/*
 * This file is part of the Trickl Open Source Libraries.
 *
 * Trickl Open Source Libraries - http://open.trickl.com/
 *
 * Copyright (C) 2011 Tim Gee.
 *
 * Trickl Open Source Libraries are free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Trickl Open Source Libraries are distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this project.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.trickl.graph.planar;

import com.vividsolutions.jts.geom.Coordinate;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;
import org.jgrapht.ext.ComponentAttributeProvider;

/**
 *
 * @author tgee
 */
public class PlanarLayoutPositionProvider<V> implements ComponentAttributeProvider<V> {

   final private PlanarLayout<V> planarLayout;
   
   final private ComponentAttributeProvider<V> chainProvider;
   
   final private AffineTransform affineTransform;
   
   /**
    * Create a position provider that uses a planar layout
    * @param planarLayout
    * @param chainedProvider Another provider to use
    */
   public PlanarLayoutPositionProvider(PlanarLayout planarLayout, AffineTransform affineTransform, ComponentAttributeProvider<V> chainedProvider) {
      this.planarLayout = planarLayout;
      this.affineTransform = affineTransform;
      this.chainProvider = chainedProvider;
   }
   
   public PlanarLayoutPositionProvider(PlanarLayout planarLayout, AffineTransform affineTransform) {
      this(planarLayout, affineTransform, null);
   }
   
   public PlanarLayoutPositionProvider(PlanarLayout planarLayout) {
      this(planarLayout, new AffineTransform(), null);
   }
   
   @Override
   public Map<String, String> getComponentAttributes(V vertex) {
      Map<String, String> attributes = new HashMap<String, String>();
      Coordinate coord = planarLayout.getCoordinate(vertex);
      if (coord != null) {
         Point2D.Double source = new Point2D.Double(coord.x, coord.y);
         Point2D.Double target = new Point2D.Double();
         affineTransform.transform(source, target);
         attributes.put("pos", String.format("%f,%f!", target.x, target.y));
      }
      if (chainProvider != null) {
         attributes.putAll(chainProvider.getComponentAttributes(vertex));
      }
      return attributes;
   }
   
}
