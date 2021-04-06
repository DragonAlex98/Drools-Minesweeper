package it.unicam.cs.solver;

import it.unicam.cs.controller.DroolsUtils;
import it.unicam.cs.csp_solver.MinesweeperSolver;
import it.unicam.cs.csp_solver.SolverStatistics;
import it.unicam.cs.enumeration.Difficulty;
import it.unicam.cs.enumeration.GameState;
import it.unicam.cs.enumeration.SolveStrategy;
import it.unicam.cs.enumeration.SquareState;
import it.unicam.cs.model.Grid;
import it.unicam.cs.model.Location;

/**
 * Class used to manage the various steps that a specific Solver has to perform,
 * abstracting from the type of the Solver itself.
 *
 */
public class SolverManager {
	/** The Solver used to solve the game **/
	private MinesweeperSolver solver;
	/** The grid used by the Solver **/
	private Grid grid;
	/** SolverStatistics to store statistics **/
	private SolverStatistics solverStatistics;
	/** Whether last SolveStep was chosen randomly **/
	private boolean isLastStepRandom;
	
	public SolverManager(SolveStrategy strategy, Grid grid) {
		try {
			this.solver = strategy.getSolverClass().getConstructor(Grid.class).newInstance(grid);
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.grid = grid;
	}

	/**
	 * Method used to populate the grid and to uncover the first top-left Square.
	 * 
	 */
	private void firstStep() {
		Location location = new Location(0, 0);
		grid.populateSafeGrid(location);
		DroolsUtils.getInstance().insertAndFire("UNCOVER", location);
	}
	
	/**
	 * Method used to perform a single resolution step according to the current
	 * state of the grid.
	 * 
	 */
	public void solveByStep() {
		if (!grid.isPopulated()) {
			this.firstStep();
			return;
		}

		SolveStep step = solver.solveByStep();
		if (step == null) {
			Location location = getCornerOrRandomLocation();
			DroolsUtils.getInstance().insertAndFire("UNCOVER", location);
			if (solverStatistics != null) {
				solverStatistics.increaseTotalNumberOfRandomDecisions();
				isLastStepRandom = true;
			}
			return;
		}
		
		if (step.isStepRandom()) {
			if (solverStatistics != null) {
				solverStatistics.increaseTotalNumberOfRandomDecisions();
				isLastStepRandom = true;
			}
		}

		step.getLocationsToFlag().forEach(f -> {
			DroolsUtils.getInstance().insertAndFire("FLAG", f);
		});
		step.getLocationsToUncover().forEach(f -> {
			DroolsUtils.getInstance().insertAndFire("UNCOVER", f);
		});
	}
	
	/**
	 * Method used to fully solve the Minesweeper game, according to the current
	 * state of the grid.
	 */
	public void complete() {
		if (!grid.isPopulated()) {
			firstStep();
		}
		
		while(grid.getGameState() == GameState.ONGOING) {
			if (solverStatistics != null) {
				isLastStepRandom = false;
			}
			solveByStep();
		}
	}

	/**
	 * Method used to run and solve different game many times (n).
	 * 
	 * @param n Number of times to run and solve a game.
	 */
	public void completeNTimes(int n) throws Exception {
		this.solverStatistics = new SolverStatistics();

		int win = 0;
		int lose = 0;
		for (int i = 0; i < n; i++) {
			DroolsUtils.getInstance().clear();
			grid = new Grid(grid.getConfig());
			solver = solver.getClass().getConstructor(Grid.class).newInstance(grid);
			long runStartTime = System.currentTimeMillis();
			complete();
			long runEndTime = System.currentTimeMillis();
			solverStatistics.increaseTotalSolvingTime((runEndTime - runStartTime)/1000f);
			if (grid.getGameState() == GameState.WIN) {
				win++;
			} else if (grid.getGameState() == GameState.LOSS) {
				if (isLastStepRandom) {
					solverStatistics.increaseNumberOfRandomDecisionsLeadingToLose();
					isLastStepRandom = false;
				}
				lose++;
			}
			System.out.println("Win: " + win + ", Lose: " + lose + ", Win Rate: " + (double) win / (win + lose) * 100f);
		}
		System.out.println("Win: " + win + ", Lose: " + lose + ", Win Rate: " + (double) win / (win + lose) * 100f);
		solverStatistics.setWinNumber(win);
		solverStatistics.setLoseNumber(lose);
		solverStatistics.setNumberOfRuns(n);
		solverStatistics.consolidate();
		System.out.println(solverStatistics);
	}

	/**
	 * Method to get the location corresponding to a covered Square, taken from one
	 * of the corners, if available, or in a random way.
	 * 
	 * @return The location corresponding to a covered Square.
	 */
	private Location getCornerOrRandomLocation() {
		Location location;
		if (grid.getSquareAt(new Location(0, 0)).getState() == SquareState.COVERED) {
			location = new Location(0, 0);
		} else if (grid.getSquareAt(new Location(0, grid.getConfig().getN_COLUMNS()-1)).getState() == SquareState.COVERED) {
			location = new Location(0, grid.getConfig().getN_COLUMNS()-1);
		} else if (grid.getSquareAt(new Location(grid.getConfig().getN_ROWS()-1, grid.getConfig().getN_COLUMNS()-1)).getState() == SquareState.COVERED) {
			location = new Location(grid.getConfig().getN_ROWS()-1, grid.getConfig().getN_COLUMNS()-1);
		} else if (grid.getSquareAt(new Location(grid.getConfig().getN_ROWS()-1, 0)).getState() == SquareState.COVERED) {
			location = new Location(grid.getConfig().getN_ROWS()-1, 0);
		} else {
			do {
				location = grid.getRandomPoint();
			} while(grid.getSquareAt(location).getState() != SquareState.COVERED);
		}
		return location;
	}

	public static void main(String[] args) throws Exception {
		Grid grid = new Grid(Difficulty.EXPERT.getConfiguration());
		SolverManager manager = new SolverManager(SolveStrategy.CSP, grid);
		manager.completeNTimes(1000);
	}
}
