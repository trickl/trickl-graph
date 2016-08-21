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

import com.trickl.graph.vertices.IdCoordinateVertex;
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
import org.springframework.test.context.ContextConfiguration;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes=JsonConfiguration.class)
@JsonTest
public class IdCoordinateVertexJsonTest {

    @Autowired
    private JacksonTester<IdCoordinateVertex> json;

    @Test
    public void testSerialize() throws Exception {
        IdCoordinateVertex vertex = new IdCoordinateVertex(123, new Coordinate(0, 1, -1));
        URL controlFile = this.getClass().getResource("idcoordinatevertex.json");
        try (InputStream inputStream = controlFile.openStream()) {
            assertThat(this.json.write(vertex)).isEqualToJson(inputStream);
        }
    }

    @Test
    public void testDeserialize() throws Exception {
        URL controlFile = this.getClass().getResource("idcoordinatevertex.json");
        try (InputStream inputStream = controlFile.openStream()) {
            IdCoordinateVertex vertex =  this.json.read(inputStream).getObject();
            assertNotNull(vertex);
            assertEquals(123, (long) vertex.getId());
            assertEquals(new Coordinate(0, 1, -1), vertex.getCoordinate());
        }
    }
}
