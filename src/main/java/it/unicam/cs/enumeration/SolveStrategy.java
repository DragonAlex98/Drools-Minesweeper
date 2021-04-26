package it.unicam.cs.enumeration;

import java.awt.event.KeyEvent;

import it.unicam.cs.solver.CSPSolver;
import it.unicam.cs.solver.MinesweeperSolver;
import it.unicam.cs.solver.SinglePointSolver;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SolveStrategy {
	SINGLE_POINT("SPSolver", SinglePointSolver.class, KeyEvent.VK_Q, KeyEvent.VK_A, KeyEvent.VK_Z), CSP("CSPSolver", CSPSolver.class, KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_X);
	
	private String name;
	private Class<? extends MinesweeperSolver> solverClass;
	private int singleStepKey;
	private int solveKey;
	private int solveNTimesKey;
}
