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

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import com.flipkart.zjsonpatch.JsonDiff;
import com.trickl.graph.edges.UndirectedIdEdge;
import com.trickl.graph.edges.UndirectedIdEdgeFactory;
import com.trickl.graph.planar.DoublyConnectedEdgeList;
import com.trickl.graph.planar.faces.IdFace;
import com.trickl.graph.planar.faces.IdFaceFactory;
import com.trickl.graph.planar.xml.XmlDcelDocument;
import com.trickl.graph.vertices.IdVertex;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import javax.xml.bind.JAXBContext;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.ElementNameAndAttributeQualifier;
import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import org.jgrapht.EdgeFactory;
import static org.junit.Assert.*;
import org.junit.Test;

public class XmlDoublyConnectedEdgeListAdapterTest {

    @Test
    public void transformToXML() throws Exception {
        XmlDcelDocument<IdVertex, UndirectedIdEdge<IdVertex>, IdFace> document = new XmlDcelDocument<IdVertex, UndirectedIdEdge<IdVertex>, IdFace>();
        document.setDoublyConnectedEdgeList(createSquareDcel());
        StringWriter writer = new StringWriter();
        JAXBContext context = JAXBContext.newInstance(XmlDcelDocument.class,
                IdVertex.class,
                UndirectedIdEdge.class,
                UndirectedIdEdgeFactory.class,
                IdFace.class,
                IdFaceFactory.class);
        document.write(writer, context);

        // DEBUG
        //System.out.println(writer.toString());
        
        URL controlFile = this.getClass().getResource("dcel.square.xml");
        InputStreamReader controlReader = new InputStreamReader(controlFile.openStream());
        StringReader reader = new StringReader(writer.toString());
        Diff xmlDiff = new Diff(controlReader, reader);
        xmlDiff.overrideElementQualifier(new ElementNameAndAttributeQualifier("id"));
        assertXMLEqual("Generated XML not as expected", xmlDiff, true);
    }

    @Test
    public void transformFromXML() throws Exception {
        JAXBContext context = JAXBContext.newInstance(XmlDcelDocument.class,
                IdVertex.class,
                UndirectedIdEdge.class,
                UndirectedIdEdgeFactory.class,
                IdFace.class,
                IdFaceFactory.class);
        XmlDcelDocument<IdVertex, UndirectedIdEdge<IdVertex>, IdFace> document
                = new XmlDcelDocument<IdVertex, UndirectedIdEdge<IdVertex>, IdFace>();

        URL controlFile = this.getClass().getResource("dcel.square.xml");
        InputStreamReader reader = new InputStreamReader(controlFile.openStream());
        document.read(reader, context);

        assertNotNull(document);
        assertNotNull(document.getDoublyConnectedEdgeList());
        assertEquals(3, document.getDoublyConnectedEdgeList().getFaceMap().size());
        assertEquals(5, document.getDoublyConnectedEdgeList().getEdgeMap().size());
        assertEquals(4, document.getDoublyConnectedEdgeList().getVertexMap().size());
        assertTrue(document.getDoublyConnectedEdgeList().getEdgeFactory() instanceof UndirectedIdEdgeFactory);
        assertTrue(document.getDoublyConnectedEdgeList().getFaceFactory() instanceof IdFaceFactory);
    }

    @Test
    public void transformToJson() throws Exception {
        DoublyConnectedEdgeList<IdVertex, UndirectedIdEdge<IdVertex>, IdFace> dcel = createSquareDcel();
        StringWriter writer = new StringWriter();
        ObjectMapper mapper = new ObjectMapper();

        AnnotationIntrospector annotationIntrospector
                = new JaxbAnnotationIntrospector(mapper.getTypeFactory());
        mapper.setAnnotationIntrospector(annotationIntrospector);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.writeValue(writer, dcel);

        // DEBUG
        System.out.println(writer.toString());

        URL controlFile = this.getClass().getResource("dcel.square.json");
        InputStreamReader controlReader = new InputStreamReader(controlFile.openStream());
        StringReader reader = new StringReader(writer.toString());        
        
        JsonNode patch = JsonDiff.asJson(mapper.readTree(controlReader), mapper.readTree(reader));
        
        assertEquals("Expected zero json differences.", patch.toString(), "[]");
    }
    
    @Test
    public void transformFromJson() throws Exception {
        XmlDcelDocument<IdVertex, UndirectedIdEdge<IdVertex>, IdFace> document
                = new XmlDcelDocument<>();

        URL controlFile = this.getClass().getResource("dcel.square.json");
        InputStreamReader reader = new InputStreamReader(controlFile.openStream());
        ObjectMapper mapper = new ObjectMapper();

        AnnotationIntrospector annotationIntrospector
                = new JaxbAnnotationIntrospector(TypeFactory.defaultInstance());
        mapper.setAnnotationIntrospector(annotationIntrospector);
        
        DoublyConnectedEdgeList<IdVertex, UndirectedIdEdge<IdVertex>, IdFace>  root =
                mapper.readValue(reader, DoublyConnectedEdgeList.class);
        document.setDoublyConnectedEdgeList(root);

        assertNotNull(document);
        assertNotNull(document.getDoublyConnectedEdgeList());
        assertEquals(3, document.getDoublyConnectedEdgeList().getFaceMap().size());
        assertEquals(5, document.getDoublyConnectedEdgeList().getEdgeMap().size());
        assertEquals(4, document.getDoublyConnectedEdgeList().getVertexMap().size());
        assertTrue(document.getDoublyConnectedEdgeList().getEdgeFactory() instanceof UndirectedIdEdgeFactory);
        assertTrue(document.getDoublyConnectedEdgeList().getFaceFactory() instanceof IdFaceFactory);
    }

    private DoublyConnectedEdgeList<IdVertex, UndirectedIdEdge<IdVertex>, IdFace> createSquareDcel() {
        EdgeFactory<IdVertex, UndirectedIdEdge<IdVertex>> edgeFactory = new UndirectedIdEdgeFactory<IdVertex>();
        DoublyConnectedEdgeList<IdVertex, UndirectedIdEdge<IdVertex>, IdFace> square = new DoublyConnectedEdgeList(edgeFactory, new IdFaceFactory());
        IdVertex a = new IdVertex(0);
        IdVertex b = new IdVertex(1);
        IdVertex c = new IdVertex(2);
        IdVertex d = new IdVertex(3);

        square.addEdge(a, b);
        square.addEdge(b, d, a, null);
        square.addEdge(d, a, b, null);
        square.addEdge(b, c, d, null);
        square.addEdge(c, d, b, null);

        return square;
    }
}
