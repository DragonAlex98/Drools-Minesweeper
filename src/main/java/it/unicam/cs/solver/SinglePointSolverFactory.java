package it.unicam.cs.solver;

import it.unicam.cs.model.Grid;

public class SinglePointSolverFactory implements SolverFactory {

	@Override
	public MinesweeperSolver createSolver(Grid grid) {
		return new SinglePointSolver(grid);
	}
}
