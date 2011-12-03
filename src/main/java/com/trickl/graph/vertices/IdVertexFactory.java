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
package com.trickl.graph.vertices;

import com.trickl.graph.AbstractIdFactory;
import java.io.Serializable;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.jgrapht.VertexFactory;

@XmlType(name = "id-vertex-factory")
@XmlRootElement(name = "id-vertex-factory")
public class IdVertexFactory extends AbstractIdFactory<IdVertex>
        implements VertexFactory<IdVertex>,
        Serializable {

   @Override
   public IdVertex createVertex() {
      IdVertex vertex = new IdVertex(nextId++);
      vertices.put(vertex.getId(), vertex);
      return vertex;
   }
}
