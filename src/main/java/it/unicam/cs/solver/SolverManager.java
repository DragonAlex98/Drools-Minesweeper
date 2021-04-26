package it.unicam.cs.solver;

import it.unicam.cs.enumeration.Difficulty;
import it.unicam.cs.enumeration.GameState;
import it.unicam.cs.enumeration.SolveStrategy;
import it.unicam.cs.enumeration.SquareState;
import it.unicam.cs.model.Grid;
import it.unicam.cs.model.Location;
import it.unicam.cs.utils.DroolsUtils;
import lombok.Getter;

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
	@Getter
	private SolverStatistics solverStatistics;
	/** Whether last SolveStep was chosen randomly **/
	private boolean isLastStepRandom;
	/** Whether to record statistics **/
	private boolean shouldRecordStatistics;

	public SolverManager(SolveStrategy strategy, Grid grid) {
		this(strategy, grid, false);
	}

	public SolverManager(SolveStrategy strategy, Grid grid, boolean shouldRecordStatistics) {
		try {
			this.solver = strategy.getSolverClass().getConstructor(Grid.class).newInstance(grid);
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.grid = grid;
		this.shouldRecordStatistics = shouldRecordStatistics;
		if (shouldRecordStatistics) {
			this.solverStatistics = new SolverStatistics();
		}
	}
	
	public void updateSolver(Grid grid) {
		try {
			this.grid = grid;
			this.solver = solver.getClass().getConstructor(Grid.class).newInstance(grid);
		} catch (Exception e) {
			e.printStackTrace();
		}
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
			if (shouldRecordStatistics) {
				solverStatistics.increaseTotalNumberOfRandomDecisions();
				isLastStepRandom = true;
			}
			return;
		}
		
		if (step.isStepRandom()) {
			if (shouldRecordStatistics) {
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
		long runStartTime = 0;
		long runEndTime = 0;
		if (shouldRecordStatistics) {
			runStartTime = System.currentTimeMillis();
		}
		if (!grid.isPopulated()) {
			firstStep();
		}
		
		while (grid.getGameState() == GameState.ONGOING) {
			if (shouldRecordStatistics) {
				isLastStepRandom = false;
			}
			solveByStep();
		}
		if (shouldRecordStatistics) {
			runEndTime = System.currentTimeMillis();
			solverStatistics.increaseTotalSolvingTime((runEndTime - runStartTime)/1000f);
			if (grid.getGameState() == GameState.WIN) {
				solverStatistics.increaseWin();
			} else if (grid.getGameState() == GameState.LOSS) {
				if (isLastStepRandom) {
					solverStatistics.increaseNumberOfRandomDecisionsLeadingToLose();
					isLastStepRandom = false;
				}
				solverStatistics.increaseLose();
			}
			solverStatistics.increaseRun();
		}
		//System.out.println("Win: " + win + ", Lose: " + lose + ", Win Rate: " + (double) win / (win + lose) * 100f);
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
		SolverManager manager = new SolverManager(SolveStrategy.CSP, grid, true);
		for (int i = 0; i < 1000; i++) {
			DroolsUtils.getInstance().clear();
			Grid newGrid = new Grid(manager.grid.getConfig());
			manager.updateSolver(newGrid);
			manager.complete();
		}
		manager.getSolverStatistics().consolidate();
		System.out.println(manager.getSolverStatistics());
	}
}
