package it.unicam.cs.solver;

import it.unicam.cs.model.Grid;

public interface SolverFactory {
	
	public MinesweeperSolver createSolver(Grid grid);
}
