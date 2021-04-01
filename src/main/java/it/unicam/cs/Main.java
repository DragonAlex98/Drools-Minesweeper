package it.unicam.cs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.BoolVar;

import it.unicam.cs.controller.DroolsUtils;
import it.unicam.cs.enumeration.Difficulty;
import it.unicam.cs.enumeration.GameState;
import it.unicam.cs.enumeration.SquareState;
import it.unicam.cs.enumeration.SquareType;
import it.unicam.cs.model.Grid;
import it.unicam.cs.model.Location;
import it.unicam.cs.model.Number;

public class Main {
	
	private static Location getLocationFromString(String locationString) {
		int start = locationString.indexOf("(");
		int middle = locationString.indexOf(",");
		int end = locationString.indexOf(")");
		Integer row = Integer.parseInt(locationString.substring(start+1, middle));
		Integer column = Integer.parseInt(locationString.substring(middle+1, end));
		return new Location(row, column);
	}
	
	private static void solveByStep(Grid grid) {
		HashMap<Location, BoolVar> map = new HashMap<Location, BoolVar>();
		Model model = new Model("Problemone");
		grid.getGridAsStream().filter(s -> s.getType() == SquareType.NUMBER && s.getState() == SquareState.UNCOVERED).forEach(s -> {
			grid.getNeighboursAsStream(s.getLocation()).filter(n -> n.getState() == SquareState.COVERED).forEach(n -> {
				String varName = String.format("(%d,%d)", n.getLocation().getRow(), n.getLocation().getColumn());
				if (!map.containsKey(n.getLocation())) {
					BoolVar boolVar = model.boolVar(varName);
					map.put(n.getLocation(), boolVar);
				}
			});
		});
		
		if (map.size() == 0) {
			return;
		}

		grid.getGridAsStream().filter(s -> s.getType() == SquareType.NUMBER && s.getState() == SquareState.UNCOVERED).forEach(s -> {
			List<BoolVar> boolVars = grid.getNeighboursAsStream(s.getLocation()).filter(n -> n.getState() == SquareState.COVERED).map(n -> map.get(n.getLocation())).collect(Collectors.toList());
			if (boolVars.size() == 0) {
				return;
			}
			int flaggedNeighbors = (int)grid.getNeighboursAsStream(s.getLocation()).filter(n -> n.getState() == SquareState.FLAGGED).count();
			int sum = ((Number)s).getNeighbourBombsCount() - flaggedNeighbors;
			if (sum == 0) {
				return;
			}
			BoolVar[] vars = boolVars.toArray(new BoolVar[] {});
			model.sum(vars, "=", sum).post();
		});

		//System.out.println(grid);
		//System.out.println(model);

		Map<Location, Integer> mySolution = null;
		Solver chocoSolver = model.getSolver();
		int count = 0;
		while (chocoSolver.solve()) {
			count++;
			if (count == 20000) {
				// STOP after 20000 solutions found
				return;
			}
			Solution solution = new Solution(model, map.values().toArray(new BoolVar[] {}));
			solution.record();

			if (mySolution == null) {
				// always save the first solution
				mySolution = new HashMap<Location, Integer>();
				for (BoolVar bv : solution.retrieveBoolVars()) {
					mySolution.put(getLocationFromString(bv.getName()), bv.getValue());
				}
			} else {
				if (mySolution.size() == 0) {
					// if no variables are always equal, then there is certain solution
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
		// no solution has been found
		if (mySolution == null || mySolution.size() == 0) {
			return;
		}
		
		// we have calculated a solution containing only the variables that remained the same through all the solutions
		mySolution.entrySet().forEach(e -> {
			if (e.getValue() == 1) {
				grid.flagSquare(e.getKey());
			} else {
				grid.uncoverSquare(e.getKey());
			}
		});
		
		//OLD DEBUG
		//System.out.println("N. solutions: " + allSolutions.size());
		//System.out.println("N. initial variables: " + map.size());
		//System.out.println("N. solved variables: " + realSolution.getVariables().size());
		//System.out.println(realSolution);
		
		//System.out.println("COVERED before: " + grid.getGridAsStream().filter(s -> s.getState() == SquareState.COVERED).count());
		//System.out.println("UNCOVERED before: " + grid.getGridAsStream().filter(s -> s.getState() == SquareState.UNCOVERED).count());
		//System.out.println("FLAGGED before: " + grid.getGridAsStream().filter(s -> s.getState() == SquareState.FLAGGED).count());
		/*realSolution.getVariables().entrySet().forEach(e -> {
			if (e.getValue() == 1) {
				grid.flagSquare(e.getKey());
			} else {
				grid.uncoverSquare(e.getKey());
			}
		});*/
		//System.out.println("COVERED after: " + grid.getGridAsStream().filter(s -> s.getState() == SquareState.COVERED).count());
		//System.out.println("UNCOVERED after: " + grid.getGridAsStream().filter(s -> s.getState() == SquareState.UNCOVERED).count());
		//System.out.println("FLAGGED after: " + grid.getGridAsStream().filter(s -> s.getState() == SquareState.FLAGGED).count());
	}
	
	private static void firstStep(Grid grid) {
		grid.getGridAsStream().filter(s -> s.getState() == SquareState.UNCOVERED && s.getType() == SquareType.NUMBER).forEach(s -> {
			long neighbours = grid.getNeighboursAsStream(s.getLocation()).filter(n -> n.getState() == SquareState.FLAGGED || n.getState() == SquareState.COVERED).count();
			if (neighbours == ((Number)s).getNeighbourBombsCount()) {
				grid.getNeighboursAsStream(s.getLocation()).filter(n -> n.getState() == SquareState.COVERED).forEach(n -> {
					grid.flagSquare(n.getLocation());
				});
			}
		});
	}
	
	private static void secondStep(Grid grid) {
		grid.getGridAsStream().filter(s -> s.getState() == SquareState.UNCOVERED && s.getType() == SquareType.NUMBER).forEach(s -> {
			long neighbours = grid.getNeighboursAsStream(s.getLocation()).filter(n -> n.getState() == SquareState.FLAGGED).count();
			if (neighbours == ((Number)s).getNeighbourBombsCount()) {
				grid.getNeighboursAsStream(s.getLocation()).filter(n -> n.getState() == SquareState.COVERED).forEach(n -> {
					grid.uncoverSquare(n.getLocation());
				});
			}
		});
	}
	
	private static void finalStep(Grid grid) {
		// to solve situations like this
		// [COVERED][FLAGGED]...
		// [FLAGGED][FLAGGED]...
		// [.......][.......]...
		// when all the bombs have been flagged
		if (grid.getConfig().getN_BOMBS() == grid.getGridAsStream().filter(s -> s.getState() == SquareState.FLAGGED).count()) {
			grid.getGridAsStream().filter(s -> s.getState() == SquareState.COVERED).forEach(s -> grid.uncoverSquare(s.getLocation()));
		}
	}
	
	private static void solve(Grid grid) {
		if (grid.getGameState() != GameState.ONGOING) {
			return;
		}
		do {
			String oldGrid = grid.toString();
			firstStep(grid);   // AMN
			secondStep(grid);  // AFN
			solveByStep(grid); // CSP
			finalStep(grid);   // final edge case
			String newGrid = grid.toString();
			if (oldGrid.equals(newGrid)) {
				Location randomLocation;
				do {
					randomLocation = grid.getRandomPoint();
				} while(grid.getSquareAt(randomLocation).getState() != SquareState.COVERED);
				grid.uncoverSquare(randomLocation);
			}
		} while(grid.getGameState() == GameState.ONGOING);
	}
	
	public static void main(String[] args) {
		int win = 0;
		int lose = 0;
		for (int i = 0; i < 1000; i++) {
			DroolsUtils.getInstance().clear();
			Grid grid = new Grid(Difficulty.BEGINNER.getConfiguration());
			grid.populate();
			Location randomLocation;
			do {
				randomLocation = grid.getRandomPoint();
			} while(grid.getSquareAt(randomLocation).getType() == SquareType.BOMB);
			grid.uncoverSquare(randomLocation);
			solve(grid);
			if (grid.getGameState() == GameState.WIN) {
				win++;
			} else {
				lose++;
			}
			System.out.println("Win: " + win + ", Lose: " + lose + ", Win Rate: " + (double)win/(win+lose)*100f);
		}
		System.out.println("Win: " + win + ", Lose: " + lose + ", Win Rate: " + (double)win/(win+lose)*100f);
	}
}
