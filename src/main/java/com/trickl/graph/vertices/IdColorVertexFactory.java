package com.trickl.graph.vertices;

import com.trickl.graph.AbstractIdFactory;
import java.awt.Color;
import java.util.Random;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.jgrapht.VertexFactory;

@XmlType(name = "color-vertex-factory")
@XmlRootElement(name = "color-vertex-factory")
public class IdColorVertexFactory extends AbstractIdFactory<IdColorVertex>
        implements VertexFactory<IdColorVertex> {

   Random random = new Random();

   public IdColorVertexFactory() {
   }

   public IdColorVertexFactory(int seed) {
      random.setSeed(seed);
   }

   @Override
   public IdColorVertex createVertex() {

      int rgb = random.nextInt(256 * 256 * 256);
      IdColorVertex vertex = new IdColorVertex(vertices.size(), new Color(rgb));
      vertices.put(vertex.getId(), vertex);
      return vertex;
   }
}
