package it.unicam.cs.csp_solver;

import java.util.List;
import java.util.Map;

import it.unicam.cs.model.Location;

public interface MinesweeperSolver {
	public void solveByStep();
	
	public void solveComplete();
	
	public void solveCompleteNTimes();
}
