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

import com.trickl.graph.Labeller;
import com.trickl.graph.edges.DirectedEdge;
import com.trickl.graph.edges.IntegerEdgeFactory;
import com.trickl.graph.vertices.IntegerVertexFactory;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 *
 * @author tgee
 */
public class DualGraphFaceLabeller<V, E> implements Labeller<DirectedEdge<V>> {

    private Map<Integer, Set<DirectedEdge<V>>> labelFaces = new HashMap<Integer, Set<DirectedEdge<V>>>();
    private Map<DirectedEdge<V>, Integer> faceLabels = new HashMap<DirectedEdge<V>, Integer>();

    public DualGraphFaceLabeller(PlanarGraph<V, E> graph) {
        generateLabels(graph);
    }

    @Override
    public int getLabelCount() {
        return labelFaces.size();
    }

    @Override
    public int getLabel(DirectedEdge<V> face) {
        return faceLabels.get(face);
    }

    @Override
    public Set<DirectedEdge<V>> getMembers(int label) {
        return labelFaces.get(label);
    }

    private void generateLabels(PlanarGraph<V, E> graph) {
        DoublyConnectedEdgeList<Integer, Integer, Object> dualGraph = new DoublyConnectedEdgeList<>(new IntegerEdgeFactory<>(), Object.class);

        DualGraphVisitor<V, E, Integer, Integer> dualGraphVisitor = new DualGraphVisitor(graph, dualGraph, new IntegerVertexFactory());
        PlanarFaceTraversal<V, E> planarFaceTraversal = new CanonicalPlanarFaceTraversal<>(graph);
        planarFaceTraversal.traverse(dualGraphVisitor);

        Labeller<Integer> dualVertexLabeller = new SixColorVertexLabeller(dualGraph);

        labelFaces.clear();
        faceLabels.clear();
        dualGraphVisitor.getFaceToVertexMap().entrySet().stream().forEach((faceToVertex) -> {
            int label = dualVertexLabeller.getLabel(faceToVertex.getValue());
            faceLabels.put(faceToVertex.getKey(), label);

            if (!labelFaces.containsKey(label)) {
                labelFaces.put(label, new HashSet<>());
            }
            labelFaces.get(label).add(faceToVertex.getKey());
        });
    }
}
