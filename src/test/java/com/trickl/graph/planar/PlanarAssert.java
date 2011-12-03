package com.trickl.graph.planar;

import com.trickl.graph.planar.PlanarGraph;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import static org.junit.Assert.*;

public class PlanarAssert {
   static public <V, E> void assertEmbeddingEquals(PlanarGraph<V, E> graph,
                                       V vertex,
                                       String ids) {
      String[] idArray = ids.split(",");

      LinkedList<String> embeddingIds = new LinkedList<String>();
      for (E edge : graph.edgesOf(vertex))
      {
         V other = graph.getEdgeSource(edge) == vertex ?
                                 graph.getEdgeTarget(edge) :
                                 graph.getEdgeSource(edge);
         embeddingIds.add(other.toString());
      }
      
      if (idArray.length > 0)
      {
         int firstIndex = embeddingIds.indexOf(idArray[0]);
         if (firstIndex < 0) {
            assertEquals(Arrays.deepToString(idArray), Arrays.deepToString(embeddingIds.toArray()));
         }
         else {
            Collections.rotate(embeddingIds, -firstIndex);
            assertEquals(Arrays.deepToString(idArray), Arrays.deepToString(embeddingIds.toArray()));
         }
      }
      else {
         assertEquals(Arrays.deepToString(idArray), Arrays.deepToString(embeddingIds.toArray()));
      }
   }
}
