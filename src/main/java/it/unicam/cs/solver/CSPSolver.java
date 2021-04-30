package it.unicam.cs.solver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.BoolVar;

import it.unicam.cs.enumeration.SquareState;
import it.unicam.cs.enumeration.SquareType;
import it.unicam.cs.model.Grid;
import it.unicam.cs.model.Location;
import it.unicam.cs.model.Number;

/**
 * Class used to represent a Minesweeper Solver that uses the Constraint Satisfaction Problem Strategy.
 *
 */
public class CSPSolver implements MinesweeperSolver {

	/**	Grid used by the Solver to solve the game**/
	private Grid grid;
	
	public CSPSolver(Grid grid) {
		this.grid = grid;
	}

	/**
	 * Return the location from a BoolVar name
	 * 
	 * @param locationString the name of a BoolVar
	 * @return the location extracted from the string
	 */
	private static Location getLocationFromString(String locationString) {
		int start = locationString.indexOf("(");
		int middle = locationString.indexOf(",");
		int end = locationString.indexOf(")");
		Integer row = Integer.parseInt(locationString.substring(start+1, middle));
		Integer column = Integer.parseInt(locationString.substring(middle+1, end));
		return new Location(row, column);
	}

	// support variable to calculate partitions
	private Set<Location> frontierUncovered;
	
	private Set<Location> calculateSinglePartition(Location location) {
		Set<Location> partition = new HashSet<Location>();
		partition.addAll(grid.getNeighboursAsStream(location).filter(s -> s.getState() == SquareState.COVERED).map(n -> n.getLocation()).collect(Collectors.toList()));
		frontierUncovered.remove(location);
		grid.getNeighboursAsStream(location).filter(s -> frontierUncovered.contains(s.getLocation())).map(n -> n.getLocation()).forEach(n -> {
			partition.addAll(calculateSinglePartition(n));
		});
		return partition;
	}
	
	private List<Set<Location>> calculatePartitions() {
		this.frontierUncovered = grid.getGridAsStream().filter(s -> s.getType() == SquareType.NUMBER && s.getState() == SquareState.UNCOVERED).filter(s -> grid.getNeighboursAsStream(s.getLocation()).anyMatch(n -> n.getState() == SquareState.COVERED)).map(n -> n.getLocation()).collect(Collectors.toSet());
		List<Set<Location>> partitions = new ArrayList<Set<Location>>();
		
		while (!frontierUncovered.isEmpty()) {
			partitions.add(calculateSinglePartition(frontierUncovered.stream().findFirst().get()));
		}
		return partitions;
	}

	@Override
	public SolveStep solveByStep() {
		// first check if SinglePoint can be applied
		SolveStep singlePointStep = new SinglePointSolver(grid).solveByStep();
		if (singlePointStep != null) {
			return singlePointStep;
		}
		
		// find all the partitions of covered squares
		List<Set<Location>> partitions = calculatePartitions();
		if (partitions.size() == 0) {
			return null;
		}
		
		Set<Location> locationsToFlag = new HashSet<Location>();
		Set<Location> locationsToUncover = new HashSet<Location>();
		
		// counters for the eventual bestGuess
		Map<Location, Integer> locationWithMineCount = new HashMap<Location, Integer>();
		Map<Location, Integer> locationWithoutMineCount = new HashMap<Location, Integer>();

		// find solutions for each partition, starting from the biggest one
		partitions.stream().sorted((x, y) -> Integer.compare(y.size(), x.size())).forEach(p -> {
			HashMap<Location, BoolVar> map = new HashMap<Location, BoolVar>();
			Model model = new Model("CSP_Model");

			// create a BoolVar for each covered square in the partition
			p.forEach(n -> {
				// set a name for the BoolVars such that it is possible to
				// obtain the location from it
				String varName = String.format("(%d,%d)", n.getRow(), n.getColumn());
				if (!map.containsKey(n)) {
					BoolVar boolVar = model.boolVar(varName);
					map.put(n, boolVar);
				}
			});
			
			if (map.size() == 0) {
				return;
			}
			
			// create model constraints
			grid.getGridAsStream()
					.filter(s -> s.getType() == SquareType.NUMBER && s.getState() == SquareState.UNCOVERED)
					.filter(s -> grid.getNeighboursAsStream(s.getLocation()).filter(n -> n.getState() == SquareState.COVERED).allMatch(n -> p.contains(n.getLocation())))
					.forEach(s -> {
						List<BoolVar> boolVars = grid.getNeighboursAsStream(s.getLocation()).filter(n -> n.getState() == SquareState.COVERED).map(n -> map.get(n.getLocation())).collect(Collectors.toList());
						if (boolVars.size() == 0) {
							return;
						}
						int flaggedNeighbors = (int)grid.getNeighboursAsStream(s.getLocation()).filter(n -> n.getState() == SquareState.FLAGGED).count();
						int sum = ((Number)s).getNeighbourBombsCount() - flaggedNeighbors;
						BoolVar[] vars = boolVars.toArray(new BoolVar[] {});
						model.sum(vars, "=", sum).post();
			});
			
			Map<Location, Integer> mySolution = null;
			Solver chocoSolver = model.getSolver();
			while (chocoSolver.solve()) {
				// for each solution to the model
				Solution solution = new Solution(model, map.values().toArray(new BoolVar[] {}));
				solution.record();
				// increase counters in case no safe solution is found
				// and a guess has to be taken
				for (BoolVar bv : solution.retrieveBoolVars()) {
					Location location = getLocationFromString(bv.getName());
					if (bv.getValue() == 1) {
						if (!locationWithMineCount.containsKey(location)) {
							locationWithMineCount.put(location, 1);
						} else {
							locationWithMineCount.put(location, locationWithMineCount.get(location)+1);
						}
					} else {
						if (!locationWithoutMineCount.containsKey(location)) {
							locationWithoutMineCount.put(location, 1);
						} else {
							locationWithoutMineCount.put(location, locationWithoutMineCount.get(location)+1);
						}
					}
				}
	
				if (mySolution == null) {
					// always save the first solution found
					mySolution = new HashMap<Location, Integer>();
					for (BoolVar bv : solution.retrieveBoolVars()) {
						mySolution.put(getLocationFromString(bv.getName()), bv.getValue());
					}
				} else {
					if (mySolution.size() == 0) {
						// if no variables are always equal, then there is no safe solution
						return;
					} else {
						for (BoolVar bv : solution.retrieveBoolVars()) {
							// for each new solution found, only keep the variables that remain the same
							Location location = getLocationFromString(bv.getName());
							if (mySolution.containsKey(location)) {
								if (!mySolution.get(location).equals(bv.getValue())) {
									mySolution.remove(location);
								}
							}
						}
					}
				}
			}
			// no solution for the partition has been found
			if (mySolution == null || mySolution.size() == 0) {
				return;
			}
			
			// add all the safe solutions for the partition to the final list
			locationsToFlag.addAll(mySolution.entrySet().stream().filter(e -> e.getValue() == 1).map(e -> e.getKey()).collect(Collectors.toList()));
			locationsToUncover.addAll(mySolution.entrySet().stream().filter(e -> e.getValue() == 0).map(e -> e.getKey()).collect(Collectors.toList()));
		});
		
		// if no safe solution has been found while iterating the partitions
		if (locationsToFlag.isEmpty() && locationsToUncover.isEmpty()) {
			// if there is still a covered corner, let the SolverManager uncover it
			if (grid.getSquareAt(new Location(0, 0)).getState() == SquareState.COVERED
					|| grid.getSquareAt(new Location(0, grid.getConfig().getN_COLUMNS()-1)).getState() == SquareState.COVERED
					|| grid.getSquareAt(new Location(grid.getConfig().getN_ROWS()-1, grid.getConfig().getN_COLUMNS()-1)).getState() == SquareState.COVERED
					|| grid.getSquareAt(new Location(grid.getConfig().getN_ROWS()-1, 0)).getState() == SquareState.COVERED) {
				return null;
			}
			
			Set<Location> keys = new HashSet<Location>();
			keys.addAll(locationWithMineCount.keySet());
			keys.addAll(locationWithoutMineCount.keySet());
			Map<Location, Double> finalKeys = new HashMap<Location, Double>();
			keys.forEach(k -> {
				int withMineCount = locationWithMineCount.containsKey(k) ? locationWithMineCount.get(k) : 0;
				int withoutMineCount = locationWithoutMineCount.containsKey(k) ? locationWithoutMineCount.get(k) : 0;
				if (withMineCount == 0 && withoutMineCount == 0) {
					finalKeys.put(k, 0.0);
				} else {
					finalKeys.put(k, (double)withoutMineCount / (withMineCount+withoutMineCount));
				}
			});

			// find the location with the highest probability of being without a mine, if it
			// exists, given by the percentage of solutions in which a BoolVar is not a mine
			Optional<Map.Entry<Location, Double>> bestGuess = finalKeys.entrySet().stream().sorted((x, y) -> Double.compare(y.getValue(), x.getValue())).findFirst();
			if (bestGuess.isPresent()) {
				// if a best guess is found, also find how many guesses are there with the same probability
				List<Map.Entry<Location, Double>> bestGuesses = finalKeys.entrySet().stream().filter(e -> e.getValue().equals(bestGuess.get().getValue())).collect(Collectors.toList());
				if (bestGuesses.size() == 1) {
					// if there is only 1, then add it as a solution
					locationsToUncover.add(bestGuess.get().getKey());
				} else {
					// otherwise randomly choose a bestGuess
					int randomGuess = new Random().nextInt(bestGuesses.size());
					locationsToUncover.add(bestGuesses.get(randomGuess).getKey());
					return new SolveStep(new ArrayList<Location>(locationsToFlag), new ArrayList<Location>(locationsToUncover), true);
				}
			} else {
				// if a best guess can't be calculated, let the SolverManager uncover a random location
				return null;
			}
			
			return new SolveStep(new ArrayList<Location>(locationsToFlag), new ArrayList<Location>(locationsToUncover));
		}

		return new SolveStep(new ArrayList<Location>(locationsToFlag), new ArrayList<Location>(locationsToUncover));
	}
}
