package it.unicam.cs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.BoolVar;

import it.unicam.cs.csp_solver.MinesweeperSolver;
import it.unicam.cs.enumeration.SquareState;
import it.unicam.cs.enumeration.SquareType;
import it.unicam.cs.model.Grid;
import it.unicam.cs.model.Location;
import it.unicam.cs.model.Number;
import it.unicam.cs.solver.SinglePointSolver;
import it.unicam.cs.solver.SolveStep;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CSPSolver2 implements MinesweeperSolver {

	private Grid grid;

	private static Location getLocationFromString(String locationString) {
		int start = locationString.indexOf("(");
		int middle = locationString.indexOf(",");
		int end = locationString.indexOf(")");
		Integer row = Integer.parseInt(locationString.substring(start+1, middle));
		Integer column = Integer.parseInt(locationString.substring(middle+1, end));
		return new Location(row, column);
	}

	@Override
	public SolveStep solveByStep() {
		SolveStep singlePointStep = new SinglePointSolver(grid).solveByStep();
		if (singlePointStep != null) {
			return singlePointStep;
		}
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
			return null;
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

		Map<Location, Integer> mySolution = null;
		Solver chocoSolver = model.getSolver();
		int count = 0;
		while (chocoSolver.solve()) {
			count++;
			if (count == 20000) {
				// STOP after 20000 solutions found
				return null;
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
					return null;
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
			return null;
		}
		
		List<Location> locationsToFlag = mySolution.entrySet().stream().filter(e -> e.getValue() == 1).map(e -> e.getKey()).collect(Collectors.toList());
		List<Location> locationsToUncover = mySolution.entrySet().stream().filter(e -> e.getValue() == 0).map(e -> e.getKey()).collect(Collectors.toList());
		return new SolveStep(locationsToFlag, locationsToUncover);
	}
}
