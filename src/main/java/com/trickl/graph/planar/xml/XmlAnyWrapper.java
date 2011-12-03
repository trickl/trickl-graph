package com.trickl.graph.planar.xml;

import javax.xml.bind.annotation.XmlAnyElement;

public class XmlAnyWrapper<V> {

   private V value;

   @XmlAnyElement(lax=true)
   public V getValue() {
      return value;
   }

   public void setValue(V value) {
      this.value = value;
   }
}
