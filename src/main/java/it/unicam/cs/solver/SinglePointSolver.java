package it.unicam.cs.solver;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import it.unicam.cs.csp_solver.MinesweeperSolver;
import it.unicam.cs.enumeration.SquareState;
import it.unicam.cs.enumeration.SquareType;
import it.unicam.cs.model.Grid;
import it.unicam.cs.model.Location;
import it.unicam.cs.model.Number;
import lombok.AllArgsConstructor;

/**
 * Class used to represent a Minesweeper Solver that uses the Single Point Strategy.
 *
 */
@AllArgsConstructor
public class SinglePointSolver implements MinesweeperSolver {

	/**	Grid of used by the Solver to solve the game**/
	private Grid grid;

	/**
	 * Method to check all mine neighbors (AMN) and possibly flag all of them.
	 */
	private List<Location> allMineNeighbors() {
		return grid.getGridAsStream().filter(s -> s.getState() == SquareState.UNCOVERED && s.getType() == SquareType.NUMBER).flatMap(s -> {
			long neighbours = grid.getNeighboursAsStream(s.getLocation()).filter(n -> n.getState() == SquareState.FLAGGED || n.getState() == SquareState.COVERED).count();
			if (neighbours == ((Number)s).getNeighbourBombsCount()) {
				return grid.getNeighboursAsStream(s.getLocation()).filter(n -> n.getState() == SquareState.COVERED).map(n -> n.getLocation());
			} else {
				return Stream.empty();
			}
		}).distinct().collect(Collectors.toList());
	}
	
	/**
	 * Method to check all free neighbors (AFN) and possibly uncover all of them.
	 */
	private List<Location> allFreeNeighbors() {
		return grid.getGridAsStream().filter(s -> s.getState() == SquareState.UNCOVERED && s.getType() == SquareType.NUMBER).flatMap(s -> {
			long neighbours = grid.getNeighboursAsStream(s.getLocation()).filter(n -> n.getState() == SquareState.FLAGGED).count();
			if (neighbours == ((Number)s).getNeighbourBombsCount()) {
				return grid.getNeighboursAsStream(s.getLocation()).filter(n -> n.getState() == SquareState.COVERED).map(n -> n.getLocation());
			} else {
				return Stream.empty();
			}
		}).distinct().collect(Collectors.toList());
	}

	@Override
	public SolveStep solveByStep() {
		List<Location> locationsToFlag = this.allMineNeighbors();
		List<Location> locationsToUncover = this.allFreeNeighbors();
		if (locationsToFlag.size() == 0 && locationsToUncover.size() == 0) {
			return null;
		} else {
			return new SolveStep(locationsToFlag, locationsToUncover);			
		}
	}
}
