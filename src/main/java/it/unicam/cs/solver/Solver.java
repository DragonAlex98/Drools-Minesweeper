package it.unicam.cs.solver;

import it.unicam.cs.controller.DroolsUtils;
import it.unicam.cs.enumeration.GameState;
import it.unicam.cs.enumeration.SquareState;
import it.unicam.cs.enumeration.SquareType;
import it.unicam.cs.model.Configuration;
import it.unicam.cs.model.Grid;
import it.unicam.cs.model.Location;
import it.unicam.cs.model.Number;

public class Solver {

	private Grid grid;

	public Solver(Grid grid) {
		this.grid = grid;
	}

	private void firstStep() {
		grid.getGridAsStream().filter(s -> s.getState() == SquareState.UNCOVERED && s.getType() == SquareType.NUMBER).forEach(s -> {
			long neighbours = grid.getNeighboursAsStream(s.getLocation()).filter(n -> n.getState() == SquareState.FLAGGED || n.getState() == SquareState.COVERED).count();
			if (neighbours == ((Number)s).getNeighbourBombsCount()) {
				grid.getNeighboursAsStream(s.getLocation()).filter(n -> n.getState() == SquareState.COVERED).forEach(n -> {
					DroolsUtils.getInstance().getKSession().getAgenda().getAgendaGroup("FLAG").setFocus();
					DroolsUtils.getInstance().getKSession().insert(n.getLocation());
					DroolsUtils.getInstance().getKSession().fireAllRules();
				});
			}
		});
	}

	private void secondStep() {
		grid.getGridAsStream().filter(s -> s.getState() == SquareState.UNCOVERED && s.getType() == SquareType.NUMBER).forEach(s -> {
			long neighbours = grid.getNeighboursAsStream(s.getLocation()).filter(n -> n.getState() == SquareState.FLAGGED).count();
			if (neighbours == ((Number)s).getNeighbourBombsCount()) {
				grid.getNeighboursAsStream(s.getLocation()).filter(n -> n.getState() == SquareState.COVERED).forEach(n -> {
					DroolsUtils.getInstance().getKSession().getAgenda().getAgendaGroup("UNCOVER").setFocus();
					DroolsUtils.getInstance().getKSession().insert(n.getLocation());
					DroolsUtils.getInstance().getKSession().fireAllRules();
				});
			}
		});
	}

	public void solve() {
		if (!grid.isPopulated()) {
			Location randomLocation = grid.getRandomPoint();
			grid.populateSafeGrid(randomLocation);
			DroolsUtils.getInstance().getKSession().getAgenda().getAgendaGroup("UNCOVER").setFocus();
			DroolsUtils.getInstance().getKSession().insert(randomLocation);
			DroolsUtils.getInstance().getKSession().fireAllRules();
		}
		if (grid.getGameState() != GameState.ONGOING) {
			return;
		}
		do {
			String oldGrid = grid.toString();
			firstStep();
			secondStep();
			String newGrid = grid.toString();
			if (oldGrid.equals(newGrid)) {
				Location randomLocation;
				do {
					randomLocation = grid.getRandomPoint();
				} while(grid.getSquareAt(randomLocation).getState() != SquareState.COVERED);
				DroolsUtils.getInstance().getKSession().getAgenda().getAgendaGroup("UNCOVER").setFocus();
				DroolsUtils.getInstance().getKSession().insert(randomLocation);
				DroolsUtils.getInstance().getKSession().fireAllRules();
			}
		} while(grid.getGameState() == GameState.ONGOING);
	}

	public static void main(String[] args) {
		int win = 0;
		int lose = 0;
		for (int i = 0; i < 1000; i++) {
			DroolsUtils.getInstance().clear();
			Grid grid = new Grid(new Configuration(16, 16, 40));
			Solver solver = new Solver(grid);
			solver.solve();
			if (grid.getGameState() == GameState.WIN) {
				win++;
			} else {
				lose++;
			}
		}
		System.out.println("Win: " + win + ", Lose: " + lose + ", Win Rate: " + (double)win/(win+lose)*100f);
	}
}
