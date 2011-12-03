package com.trickl.graph.edges;

import com.trickl.graph.edges.DirectedEdge;
import java.io.Serializable;

public class UndirectedEdge<V> extends DirectedEdge<V> 
        implements Serializable {

   private UndirectedEdge() {
      this(null, null);
   }

   public UndirectedEdge(V source, V target) {
      super(source, target);
   }

   @Override
   public int hashCode() {
      int hash = 5;

      hash = 73 * hash + (this.source != null ? this.source.hashCode() : 0)
                       + (this.target != null ? this.target.hashCode() : 0);
      return hash;
   }

   @Override
   public boolean equals(Object obj) {
      if (super.equals(obj)) return true;

      if (obj == null) {
         return false;
      }
      if (getClass() != obj.getClass()) {
         return false;
      }

      // Check the other direction
      final UndirectedEdge<V> other = (UndirectedEdge<V>) obj;
      if (this.source != other.target && (this.source == null || !this.source.equals(other.target))) {
         return false;
      }
      if (this.target != other.source && (this.target == null || !this.target.equals(other.source))) {
         return false;
      }

      return true;
   }
}
