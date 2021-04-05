package it.unicam.cs.solver;

import java.util.ArrayList;
import java.util.List;

import it.unicam.cs.CSPSolver2;
import it.unicam.cs.controller.DroolsUtils;
import it.unicam.cs.csp_solver.MinesweeperSolver;
import it.unicam.cs.enumeration.Difficulty;
import it.unicam.cs.enumeration.GameState;
import it.unicam.cs.enumeration.SquareState;
import it.unicam.cs.model.Grid;
import it.unicam.cs.model.Location;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SolverManager {
	private MinesweeperSolver solver;
	private Grid grid;
	
	private SolveStep firstStep() {
		//Location randomLocation = grid.getRandomPoint();
		Location randomLocation = new Location(0, 0);
		grid.populateSafeGrid(randomLocation);
		DroolsUtils.getInstance().getKSession().getAgenda().getAgendaGroup("UNCOVER").setFocus();
		DroolsUtils.getInstance().getKSession().insert(randomLocation);
		DroolsUtils.getInstance().getKSession().fireAllRules();
		List<Location> uncoveredLocation = new ArrayList<Location>();
		uncoveredLocation.add(randomLocation);
		return new SolveStep(new ArrayList<Location>(), uncoveredLocation);
	}
	
	public SolveStep solveByStep() {
		if (!grid.isPopulated()) {
			return this.firstStep();
		}

		SolveStep step = solver.solveByStep();
		if (step == null) {
			return null;
		}

		step.getLocationsToFlag().forEach(f -> {
			DroolsUtils.getInstance().getKSession().getAgenda().getAgendaGroup("FLAG").setFocus();
			DroolsUtils.getInstance().getKSession().insert(f);
			DroolsUtils.getInstance().getKSession().fireAllRules();
		});
		step.getLocationsToUncover().forEach(f -> {
			DroolsUtils.getInstance().getKSession().getAgenda().getAgendaGroup("UNCOVER").setFocus();
			DroolsUtils.getInstance().getKSession().insert(f);
			DroolsUtils.getInstance().getKSession().fireAllRules();
		});
		return step;
	}
	
	public void complete() {
		if (!grid.isPopulated()) {
			this.firstStep();
		}

		while(grid.getGameState() == GameState.ONGOING) {
			SolveStep step = this.solveByStep();
			if (step == null) {
				Location randomLocation;
				if (grid.getSquareAt(new Location(0, grid.getConfig().getN_COLUMNS()-1)).getState() == SquareState.COVERED) {
					randomLocation = new Location(0, grid.getConfig().getN_COLUMNS()-1);
				} else if (grid.getSquareAt(new Location(grid.getConfig().getN_ROWS()-1, grid.getConfig().getN_COLUMNS()-1)).getState() == SquareState.COVERED) {
					randomLocation = new Location(grid.getConfig().getN_ROWS()-1, grid.getConfig().getN_COLUMNS()-1);
				} else if (grid.getSquareAt(new Location(grid.getConfig().getN_ROWS()-1, 0)).getState() == SquareState.COVERED) {
					randomLocation = new Location(grid.getConfig().getN_ROWS()-1, 0);
				} else {
					do {
						randomLocation = grid.getRandomPoint();
					} while(grid.getSquareAt(randomLocation).getState() != SquareState.COVERED);
				}
				DroolsUtils.getInstance().getKSession().getAgenda().getAgendaGroup("UNCOVER").setFocus();
				DroolsUtils.getInstance().getKSession().insert(randomLocation);
				DroolsUtils.getInstance().getKSession().fireAllRules();
			}
		}
	}

	public void completeNTimes(int n) throws Exception {
		int win = 0;
		int lose = 0;
		for (int i = 0; i < n; i++) {
			DroolsUtils.getInstance().clear();
			grid = new Grid(grid.getConfig());
			solver = solver.getClass().getConstructor(Grid.class).newInstance(grid);
			complete();
			if (grid.getGameState() == GameState.WIN) {
				win++;
			} else if (grid.getGameState() == GameState.LOSS) {
				lose++;
			}
			System.out.println("Win: " + win + ", Lose: " + lose + ", Win Rate: " + (double)win/(win+lose)*100f);
		}
		System.out.println("Win: " + win + ", Lose: " + lose + ", Win Rate: " + (double)win/(win+lose)*100f);
	}

	public static void main(String[] args) throws Exception {
		Grid grid = new Grid(Difficulty.BEGINNER.getConfiguration());
		SolverManager manager = new SolverManager(new CSPSolver2(grid), grid);
		manager.completeNTimes(10000);
	}
}
