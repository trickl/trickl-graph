package com.trickl.graph;

import org.jgrapht.Graph;

public class GraphArgumentException extends IllegalArgumentException {
    
    private final Graph graph;
    
    public GraphArgumentException(Graph graph, Throwable cause) {
        super(cause.getMessage(), cause);
        this.graph = graph;        
    }
    
    public Graph getGraph() {
        return graph;
    }
}
