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
