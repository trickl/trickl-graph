package com.trickl.graph;

import java.util.Map;
import org.jgrapht.Graph;

public interface PartitionAlgorithm<V, E> {
   void partition(Graph<V, E> graph, int partitions);
   Map<V, Integer> getPartition();
}
