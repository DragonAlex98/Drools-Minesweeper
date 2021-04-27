package it.unicam.cs.solver;

import it.unicam.cs.model.Grid;

public class CSPSolverFactory implements SolverFactory {

	@Override
	public MinesweeperSolver createSolver(Grid grid) {
		return new CSPSolver(grid);
	}
}
