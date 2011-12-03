package com.trickl.graph;

public interface NeighbourhoodFunction {
   public void setNeighbourhoodWidth(double neighbourhoodWidth);
   public double getNeighbourhoodWidth();
   public double evaluate(double hopDistance);
}
