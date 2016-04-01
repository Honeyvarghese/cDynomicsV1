/* Copyright 2009-2015 David Hadka
 *
 * This file is part of the MOEA Framework.
 *
 * The MOEA Framework is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * The MOEA Framework is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the MOEA Framework.  If not, see <http://www.gnu.org/licenses/>.
 */
package iDynoOptimizer.MOEAFramework26.src.org.moeaframework.core.fitness;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import iDynoOptimizer.MOEAFramework26.src.org.moeaframework.core.FitnessEvaluator;
import iDynoOptimizer.MOEAFramework26.src.org.moeaframework.core.Population;
import iDynoOptimizer.MOEAFramework26.src.org.moeaframework.core.Problem;
import iDynoOptimizer.MOEAFramework26.src.org.moeaframework.core.Solution;
import iDynoOptimizer.MOEAFramework26.src.org.moeaframework.core.indicator.Hypervolume;

public class HypervolumeContributionFitnessEvaluator implements FitnessEvaluator {
	
	private final Problem problem;
	
	private final double offset;
	
	public HypervolumeContributionFitnessEvaluator(Problem problem) {
		this(problem, 100.0);
	}
	
	public HypervolumeContributionFitnessEvaluator(Problem problem, double offset) {
		super();
		this.problem = problem;
		this.offset = offset;
	}

	@Override
	public void evaluate(Population population) {
		if (population.size() <= 2) {
			for (Solution solution : population) {
				solution.setAttribute(FITNESS_ATTRIBUTE, 0.0);
			}
		} else {
			int numberOfObjectives = problem.getNumberOfObjectives();
			List<Solution> solutions = normalize(population);
			List<Solution> solutionsCopy = new ArrayList<Solution>(solutions);
			double totalVolume = Hypervolume.calculateHypervolume(solutionsCopy, solutionsCopy.size(), numberOfObjectives);
			
			for (int i = 0; i < population.size(); i++) {
				solutionsCopy = new ArrayList<Solution>(solutions);
				solutionsCopy.remove(i);
				
				double volume = Hypervolume.calculateHypervolume(solutionsCopy, solutionsCopy.size(), numberOfObjectives);
				population.get(i).setAttribute(FITNESS_ATTRIBUTE, totalVolume - volume);
			}
		}
	}
	
	private List<Solution> normalize(Population population) {
		List<Solution> result = new ArrayList<Solution>();
		
		double[] min = new double[problem.getNumberOfObjectives()];
		double[] max = new double[problem.getNumberOfObjectives()];
		
		Arrays.fill(min, Double.POSITIVE_INFINITY);
		Arrays.fill(max, Double.NEGATIVE_INFINITY);
		
		for (Solution solution : population) {
			for (int i = 0; i < problem.getNumberOfObjectives(); i++) {
				min[i] = Math.min(min[i], solution.getObjective(i));
				max[i] = Math.max(max[i], solution.getObjective(i));
			}
		}
		
		for (Solution solution : population) {
			Solution newSolution = solution.copy();
			
			for (int i = 0; i < problem.getNumberOfObjectives(); i++) {
				newSolution.setObjective(i, (max[i] - (newSolution.getObjective(i) - min[i]) + offset) / (max[i] - min[i]));
			}

			result.add(newSolution);
		}
		
		return result;
	}

	@Override
	public boolean areLargerValuesPreferred() {
		return true;
	}

}
