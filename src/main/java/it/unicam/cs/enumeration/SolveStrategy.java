package it.unicam.cs.enumeration;

import java.awt.event.KeyEvent;

import it.unicam.cs.solver.CSPSolver;
import it.unicam.cs.solver.MinesweeperSolver;
import it.unicam.cs.solver.SinglePointSolver;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Enumeration to represent the possible strategies used by a Solver to solve the game.
 *
 */
@Getter
@AllArgsConstructor
public enum SolveStrategy {
	/** Single Point Solver (all mine neighbors / all free neighbors) **/
	SINGLE_POINT("SPSolver", SinglePointSolver.class, KeyEvent.VK_Q, KeyEvent.VK_A, KeyEvent.VK_Z),
	/** Constraint Satisfaction Problem Solver **/
	CSP("CSPSolver", CSPSolver.class, KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_X);
	
	/** The name of the Solver **/
	private String name;
	/** The class of the concrete Solver**/
	private Class<? extends MinesweeperSolver> solverClass;
	/** Shortcut to perform a single resolution step **/
	private int singleStepKey;
	/** Shortcut to solve a game **/
	private int solveKey;
	/** Shortcut to solve a game N times **/
	private int solveNTimesKey;
}
