// DERIVATIVE WORK - IF DISTRIBUTED USE THE FOLLOWING LICENSE
// Distributed under the Boost Software License, Version 1.0.
//    (See accompanying file LICENSE_1_0.txt or copy at
//          http://www.boost.org/LICENSE_1_0.txt)

package com.trickl.graph;

public interface SpanningSearchVisitor<V, E> {
    void initializeVertex(V u);
    void startVertex(V u);
    void discoverVertex(V u);
    void examineEdge(V source, V target);
    void treeEdge(V source, V target);
    void backEdge(V source, V target);
    void forwardOrCrossEdge(V source, V target);
    void finishVertex(V u);
}
