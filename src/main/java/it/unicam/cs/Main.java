package it.unicam.cs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.variables.BoolVar;

import it.unicam.cs.enumeration.Difficulty;
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
	
	private static MySolution calculateRealSolution(Set<MySolution> solutions) {
		MySolution realSolution = null;

		Iterator<MySolution> iter = solutions.iterator();
		while (iter.hasNext() && (realSolution == null || !realSolution.getVariables().isEmpty())) {
			if (realSolution == null) {
				realSolution = iter.next();
				continue;
			}
			MySolution currentSolution = iter.next();
			for (Entry<Location, Integer> entry : currentSolution.getVariables().entrySet()) {
				if (realSolution.getVariables().containsKey(entry.getKey())) {
					if (!realSolution.getVariables().get(entry.getKey()).equals(entry.getValue())) {
						realSolution.getVariables().remove(entry.getKey());
					}					
				}
			}
		}
		return realSolution;
	}

	public static void main(String[] args) {
		Grid grid = new Grid(Difficulty.EXPERT.getConfiguration());
		//Grid grid = new Grid(new Configuration(100, 100, 1000));
		grid.populate();

		Location location;
		do {
			location = grid.getRandomPoint();
		} while(grid.getSquareAt(location).getType() != SquareType.EMPTY);
		grid.uncoverSquare(location);
		System.out.println(grid);

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

		grid.getGridAsStream().filter(s -> s.getType() == SquareType.NUMBER && s.getState() == SquareState.UNCOVERED).forEach(s -> {
			List<BoolVar> boolVars = grid.getNeighboursAsStream(s.getLocation()).filter(n -> n.getState() == SquareState.COVERED).map(n -> map.get(n.getLocation())).collect(Collectors.toList());
			model.sum(boolVars.toArray(new BoolVar[] {}), "=", ((Number)s).getNeighbourBombsCount()).post();
		});
		
		System.out.println(model);

		Set<MySolution> allSolutions = new HashSet<MySolution>();
		org.chocosolver.solver.Solver chocoSolver = model.getSolver();
		while (chocoSolver.solve()) {
			Solution solution = new Solution(model, map.values().toArray(new BoolVar[] {}));
			solution.record();

			Map<Location, Integer> mySolution = new HashMap<Location, Integer>();
			solution.retrieveBoolVars().stream().forEach(b -> {
				Location solutionLocation = getLocationFromString(b.getName());
				mySolution.put(solutionLocation, b.getValue());
			});
			allSolutions.add(new MySolution(mySolution));
		}
		System.out.println("N. solutions: " + allSolutions.size());
		System.out.println("N. initial variables: " + map.size());
		MySolution realSolution = calculateRealSolution(allSolutions);
		System.out.println("N. solved variables: " + realSolution.getVariables().size());
		System.out.println(realSolution);
		
		System.out.println("COVERED before: " + grid.getGridAsStream().filter(s -> s.getState() == SquareState.COVERED).count());
		System.out.println("UNCOVERED before: " + grid.getGridAsStream().filter(s -> s.getState() == SquareState.UNCOVERED).count());
		System.out.println("FLAGGED before: " + grid.getGridAsStream().filter(s -> s.getState() == SquareState.FLAGGED).count());
		realSolution.getVariables().entrySet().forEach(e -> {
			if (e.getValue() == 1) {
				grid.flagSquare(e.getKey());
			} else {
				grid.uncoverSquare(e.getKey());
			}
		});
		System.out.println("COVERED after: " + grid.getGridAsStream().filter(s -> s.getState() == SquareState.COVERED).count());
		System.out.println("UNCOVERED after: " + grid.getGridAsStream().filter(s -> s.getState() == SquareState.UNCOVERED).count());
		System.out.println("FLAGGED after: " + grid.getGridAsStream().filter(s -> s.getState() == SquareState.FLAGGED).count());
	}
}
