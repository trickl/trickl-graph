package com.trickl.graph.edges;

import com.trickl.graph.AbstractIdFactory;
import java.io.Serializable;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.jgrapht.EdgeFactory;

@XmlType(name="undirected-id-edge-factory")
@XmlRootElement(name="undirected-id-edge-factory")
public class UndirectedIdEdgeFactory<V> extends AbstractIdFactory<V> 
        implements EdgeFactory<V, UndirectedIdEdge<V>>,
        Serializable {

   @Override
   public UndirectedIdEdge<V> createEdge(V source, V target) {
      return new UndirectedIdEdge<V>(nextId++, source, target);
   }
}
