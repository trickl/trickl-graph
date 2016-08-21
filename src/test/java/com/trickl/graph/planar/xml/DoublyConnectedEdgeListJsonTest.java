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
package com.trickl.graph.planar.xml;

import com.trickl.graph.edges.UndirectedIdEdge;
import com.trickl.graph.edges.UndirectedIdEdgeFactory;
import com.trickl.graph.planar.DoublyConnectedEdgeList;
import com.trickl.graph.planar.PlanarGraph;
import com.trickl.graph.planar.faces.IdFace;
import com.trickl.graph.planar.faces.IdFaceFactory;
import com.trickl.graph.planar.generate.PlanarSquareGraphGenerator;
import com.trickl.graph.vertices.IdCoordinateVertex;
import com.trickl.graph.vertices.IdVertex;
import com.trickl.graph.vertices.IdVertexFactory;
import com.vividsolutions.jts.geom.Coordinate;
import java.io.InputStream;
import java.net.URL;
import org.junit.Test;
import org.junit.runner.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.json.*;
import org.springframework.boot.test.json.*;
import org.springframework.test.context.junit4.*;

import static org.junit.Assert.*;
import static org.assertj.core.api.Assertions.*;
import org.jgrapht.EdgeFactory;
import org.springframework.test.context.ContextConfiguration;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes=JsonConfiguration.class)
@JsonTest
public class DoublyConnectedEdgeListJsonTest {

    @Autowired
    private JacksonTester<DoublyConnectedEdgeList<IdVertex, UndirectedIdEdge<IdVertex>, IdFace>> json;

    @Test
    public void testSerialize() throws Exception {
        
      EdgeFactory<IdVertex, UndirectedIdEdge<IdVertex>> edgeFactory = new UndirectedIdEdgeFactory<IdVertex>();
      DoublyConnectedEdgeList<IdVertex, UndirectedIdEdge<IdVertex>, IdFace> dcel = new DoublyConnectedEdgeList(edgeFactory, new IdFaceFactory());
      PlanarSquareGraphGenerator<IdVertex, UndirectedIdEdge<IdVertex>> generator = new PlanarSquareGraphGenerator<>(4, 1);
      generator.generateGraph(dcel, new IdVertexFactory(), null);      
        URL controlFile = this.getClass().getResource("dcel.square.json");
        try (InputStream inputStream = controlFile.openStream()) {
            //System.out.println(this.json.write(dcel).getJson());
            assertThat(this.json.write(dcel)).isEqualToJson(inputStream);
        }
    }

    @Test
    public void testDeserialize() throws Exception {
        URL controlFile = this.getClass().getResource("dcel.square.json");
        try (InputStream inputStream = controlFile.openStream()) {
            DoublyConnectedEdgeList<IdVertex, UndirectedIdEdge<IdVertex>, IdFace> dcel =  this.json.read(inputStream).getObject();
            assertNotNull(dcel);
        assertEquals(2, dcel.getFaceMap().size());
        assertEquals(4, dcel.getEdgeMap().size());
        assertEquals(4, dcel.getVertexMap().size());
        assertTrue(dcel.getEdgeFactory() instanceof UndirectedIdEdgeFactory);
        assertTrue(dcel.getFaceFactory() instanceof IdFaceFactory);
        }
    }
}
