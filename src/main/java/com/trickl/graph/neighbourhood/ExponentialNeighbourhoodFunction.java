package com.trickl.graph.neighbourhood;

import com.trickl.graph.NeighbourhoodFunction;

class ExponentialNeighbourhoodFunction implements NeighbourhoodFunction {

   private double neighbourhoodWidth = 8.0;

   @Override
   public void setNeighbourhoodWidth(double neighbourhoodWidth) {
      this.neighbourhoodWidth = neighbourhoodWidth;
   }

   @Override
   public double getNeighbourhoodWidth() {
      return neighbourhoodWidth;
   }

   @Override
   public double evaluate(double distance) {
      return Math.exp(-distance / neighbourhoodWidth);
   }
}
