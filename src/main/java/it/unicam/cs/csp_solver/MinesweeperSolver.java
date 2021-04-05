package it.unicam.cs.csp_solver;

import it.unicam.cs.solver.SolveStep;

/**
 * Interface that represent a Minesweeper Solver.
 */
public interface MinesweeperSolver {
	/**
	 * Method used by the concrete Solver to perform a single resolution step.
	 * 
	 * @return The SolveStep (list of locations) that the Solver has to manage.
	 */
	public SolveStep solveByStep();
}
