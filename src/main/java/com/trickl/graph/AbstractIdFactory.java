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
package com.trickl.graph;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlAttribute;

public abstract class AbstractIdFactory<T> implements Serializable {

   protected int nextId;

   protected final transient Map<Integer, T> vertices = new HashMap<Integer, T>();

   @XmlAttribute(name="next-id")
   public int getNextId() {
      return nextId;
   }

   public void setNextId(int nextId) {
      this.nextId = nextId;
   }

   public T get(int id) {
      return vertices.get(id);
   }
}
