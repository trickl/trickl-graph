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
package com.trickl.graph.ext;

import com.trickl.graph.Labeller;
import com.trickl.graph.edges.DirectedEdge;
import java.util.HashMap;
import java.util.Map;
import org.jgrapht.ext.ComponentAttributeProvider;

/**
 *
 * @author tgee
 */
public class FaceLabellerAttributeProvider<V> implements ComponentAttributeProvider<DirectedEdge<V>> {

   final private Labeller<DirectedEdge<V>> faceLabeller;
   
   final private Map<Integer, ComponentAttributeProvider<DirectedEdge<V>>> labelAttributeProviders;
   
   final private ComponentAttributeProvider<DirectedEdge<V>> chainProvider;
   
   /**
    * Create a component provider that gives fixed attributes, regardless
    * of the component.
    * @param attributes
    * @param chainedProvider Another provider to use
    */
   public FaceLabellerAttributeProvider(Labeller<DirectedEdge<V>> faceLabeller, Map<Integer, ComponentAttributeProvider<DirectedEdge<V>>> labelAttributes, ComponentAttributeProvider<DirectedEdge<V>> chainedProvider) {      
      this.faceLabeller = faceLabeller;
      this.labelAttributeProviders = labelAttributes;
      this.chainProvider = chainedProvider;
   }
   
   public FaceLabellerAttributeProvider(Labeller<DirectedEdge<V>> faceLabeller, Map<Integer, ComponentAttributeProvider<DirectedEdge<V>>> labelAttributeProviders) {
      this(faceLabeller, labelAttributeProviders, null);
   }
   
   @Override
   public Map<String, String> getComponentAttributes(DirectedEdge<V> face) {
      Map<String, String> attributes = new HashMap<String, String>();
      int label = faceLabeller.getLabel(face);
      if (labelAttributeProviders.containsKey(label)) {
         attributes.putAll(labelAttributeProviders.get(label).getComponentAttributes(face));  
      }
      if (chainProvider != null) {
         attributes.putAll(chainProvider.getComponentAttributes(face));
      }
      return attributes;
   }
   
}
