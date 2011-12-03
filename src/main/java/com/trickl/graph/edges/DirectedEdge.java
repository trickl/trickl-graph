package com.trickl.graph.edges;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlIDREF;


public class DirectedEdge<V> implements Serializable {
   protected V source;
   protected V target;

   private DirectedEdge() {
      this(null, null);
   }

   public DirectedEdge(V source, V target) {
      this.source = source;
      this.target = target;
   }

   @XmlIDREF
   @XmlAttribute(name="source-vertex-id")
   public V getSource() {
      return source;
   }

   public void setSource(V source) {
      this.source = source;
   }

   @XmlIDREF
   @XmlAttribute(name="target-vertex-id")
   public V getTarget() {
      return target;
   }

   public void setTarget(V target) {
      this.target = target;
   }

   @Override
   public int hashCode() {
      int hash = 5;
      hash = 73 * hash + (this.source != null ? this.source.hashCode() : 0);
      hash = 73 * hash + (this.target != null ? this.target.hashCode() : 0);
      return hash;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }
      if (getClass() != obj.getClass()) {
         return false;
      }
      final DirectedEdge<V> other = (DirectedEdge<V>) obj;

      if (this.source != other.source && (this.source == null || !this.source.equals(other.source))) {
         return false;
      }
      if (this.target != other.target && (this.target == null || !this.target.equals(other.target))) {
         return false;
      }
      return true;
   }

   @Override
   public String toString() {
      return ((source == null) ? "null" : source.toString()) + "-"
           + ((target == null) ? "null" : target.toString());
   }
}
