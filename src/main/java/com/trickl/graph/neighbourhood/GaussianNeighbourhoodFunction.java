/*
 * This file is part of the Trickl Open Source Libraries.
 *
 * Trickl Open Source Libraries - http://open.trickl.com/
 *
 * Copyright (C) 2011 Tim Gee.
 *
 * Trickl Open Source Libraries are free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Trickl Open Source Libraries are distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this project.  If not, see <http://www.gnu.org/licenses/>.
 */
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
