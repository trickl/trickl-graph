package com.trickl.graph;

import cern.colt.function.IntIntDoubleFunction;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import org.jgrapht.Graph;

/**
 * Defined as s * I - D + A where s is a constant to ensure K is positive definite,
 * D is the degree and A the adjacencies (affinity) of the graph.
 * This is a sparse kernel with non-zero elements for each edge and each vertex.
 */
public class RatioCutKernelGenerator<V, E> implements VertexKernelGenerator<V, E> {
   
   private DoubleMatrix2D kernel;
   private LaplacianGenerator<V, E> laplacian;   

   public RatioCutKernelGenerator() {      
   }

   @Override
   public DoubleMatrix2D getKernel(Graph<V, E> graph) {
      if (kernel == null) {         
         laplacian = new LaplacianGenerator<V, E>(graph);

         // The laplacian has rank n-1, i.e. it is rank-deficient. So
         // We need the Moore-Penrose pseudo-inverse
         DoubleMatrix2D L = laplacian.getLaplacian();

         double maxDegree = 0;
         for (int i = 0; i < L.rows(); ++i) {
            maxDegree = Math.max(maxDegree, L.getQuick(i, i));
         }

         final double fMaxDegree = maxDegree;
         kernel = new SparseDoubleMatrix2D(L.rows(), L.columns());
         L.forEachNonZero(new IntIntDoubleFunction() {

            @Override
            public double apply(int i, int j, double value) {
               kernel.setQuick(i, j, i == j ? 1 + fMaxDegree - value : -value);
               return value;
            }
         });
      }

      return kernel;
   }

   @Override
   public Integer getIndex(V vertex) {
      return laplacian.getIndex(vertex);
   }

   @Override
   public V getVertex(int index) {
      return laplacian.getVertex(index);
   }
}
