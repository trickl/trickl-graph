package com.trickl.graph.neighbourhood;

import com.trickl.graph.NeighbourhoodFunction;
import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.NormalDistribution;
import org.apache.commons.math.distribution.NormalDistributionImpl;

public class GaussianNeighbourhoodFunction implements NeighbourhoodFunction {

   private NormalDistribution normalDistribution = new NormalDistributionImpl(0, 3);
   
   @Override
   public void setNeighbourhoodWidth(double neighbourhoodWidth) {
      normalDistribution.setStandardDeviation(neighbourhoodWidth);
   }

   @Override
   public double getNeighbourhoodWidth() {
      return normalDistribution.getStandardDeviation();
   }

   @Override
   public double evaluate(double distance) {
      double density = 0;
      double delta = 1e-5;
      try {
         // Use a gaussian distribution such that P(0) = 1.
         density = normalDistribution.cumulativeProbability(distance, distance + delta) /
                   normalDistribution.cumulativeProbability(0, delta);
      }
      catch (MathException ex) {
      }
      return density;
   }
}
