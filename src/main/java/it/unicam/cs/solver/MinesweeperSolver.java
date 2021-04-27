package it.unicam.cs.solver;

/**
 * Interface that represents a Minesweeper Solver.
 */
public interface MinesweeperSolver {
	/**
	 * Method used by the concrete Solver to perform a single resolution step.
	 * 
	 * @return The SolveStep (list of locations) that the Solver has to manage.
	 */
	public SolveStep solveByStep();
}
