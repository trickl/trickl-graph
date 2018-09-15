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

import com.trickl.graph.edges.DirectedEdge;
import java.util.*;

/**
 * A simple six-color planar graph vertex labeller detailed in: TWO LINEAR-TIME
 * ALGORITHMS FOR FIVE-COLORING A PLANAR GRAPH by David Matula, Yossi Shiloach,
 * Robert Tarjan O(n) time complexity
 *
 * @author tgee
 */
public class SixColorFaceLabeller<V, E, F> extends SixColorAdjacencyLabeller<
        DirectedEdge<V>, DirectedEdge< DirectedEdge<V> > > 
{    
   private final PlanarFaceGraph<V, E, F> graph;
   
   public SixColorFaceLabeller(PlanarFaceGraph<V, E, F> graph) {
      super(SixColorFaceLabeller.getFaces(graph),
            (face) -> SixColorFaceLabeller.getAdjacentFaces(graph, face),
            DirectedEdge::getOpposite);
      
      this.graph = graph;
   }   
   
   static public <V, E, F> Set<DirectedEdge<V>> getFaces(PlanarFaceGraph<V, E, F> graph) {       
       Set<DirectedEdge<V>> faceEdges = new LinkedHashSet();       
       for (F face : graph.faceSet()) {
           DirectedEdge<V> faceEdge = graph.getAdjacentEdge(face);
           faceEdges.add(faceEdge);           
       }
       return faceEdges;
   }
   
   @Override
   public int getLabel(DirectedEdge<V> faceEdge) {      
      F face = graph.getFace(faceEdge.getSource(), faceEdge.getTarget());
      return super.getLabel(graph.getAdjacentEdge(face));
   }
           
   static public <V, E, F> Set<DirectedEdge<DirectedEdge<V>>> getAdjacentFaces(PlanarFaceGraph<V, E, F> graph, DirectedEdge<V> face) {
       Set<DirectedEdge<DirectedEdge<V>>> adjacentFaces = new LinkedHashSet<>();       
       V source = face.getSource();
       V target = face.getTarget();
        List<V> faceVertices = PlanarGraphs.getVerticesOnFace(graph, source, target);
        for (int i = 0; i < faceVertices.size(); ++i) {
            V current = faceVertices.get(i);
            V next = faceVertices.get((i + 1) % faceVertices.size());       
            F adjacentFace = graph.getFace(next, current);                
            DirectedEdge<V> adjacent = graph.getAdjacentEdge(adjacentFace);
            adjacentFaces.add(new DirectedEdge<>(face, adjacent));                               
        }
       
       return adjacentFaces;
   }   
}