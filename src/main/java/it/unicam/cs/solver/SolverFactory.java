package it.unicam.cs.solver;

import it.unicam.cs.model.Grid;

/**
 * Interface that represent a factory for a Solver.
 * Each Solver need its own factory in order to be instantiated using the inherited factory method.
 *
 */
public interface SolverFactory {
	
	/**
	 * Method used by the concrete factory to instantiate the specific Solver.
	 * 
	 * @param grid The grid used by the Solver.
	 * @return The concrete Minesweeper Solver.
	 */
	public MinesweeperSolver createSolver(Grid grid);
}
