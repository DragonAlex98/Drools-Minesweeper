package it.unicam.cs.solver;

import java.util.Optional;

import it.unicam.cs.controller.GridController;
import it.unicam.cs.enumeration.SquareState;
import it.unicam.cs.enumeration.SquareType;
import it.unicam.cs.enumeration.UncoverResult;
import it.unicam.cs.model.Configuration;
import it.unicam.cs.model.Grid;
import it.unicam.cs.model.Location;
import it.unicam.cs.model.Number;
import it.unicam.cs.model.Square;

public class Solver {
	private Grid grid;

	public Solver(Grid grid) {
		this.grid = grid;
	}

	private void firstStep() {
		grid.getGridAsStream().filter(s -> s.getState() == SquareState.UNCOVERED && s.getType() == SquareType.NUMBER).forEach(s -> {
			long neighbours = grid.getNeighboursAsStream(s.getLocation()).filter(n -> n.getState() == SquareState.FLAGGED || n.getState() == SquareState.COVERED).count();
			if (neighbours == ((Number)s).getNeighbourBombsCount()) {
				grid.getNeighboursAsStream(s.getLocation()).filter(n -> n.getState() == SquareState.COVERED).forEach(n -> grid.flagSquare(n.getLocation()));
			}
		});
	}

	private void secondStep() {
		grid.getGridAsStream().filter(s -> s.getState() == SquareState.UNCOVERED && s.getType() == SquareType.NUMBER).forEach(s -> {
			long neighbours = grid.getNeighboursAsStream(s.getLocation()).filter(n -> n.getState() == SquareState.FLAGGED).count();
			if (neighbours == ((Number)s).getNeighbourBombsCount()) {
				grid.getNeighboursAsStream(s.getLocation()).filter(n -> n.getState() == SquareState.COVERED).forEach(n -> grid.uncoverSquare(n.getLocation()));
			}
		});
	}

	public void nextState() {
		do {
			String oldGrid = grid.toString();
			firstStep();
			secondStep();
			String newGrid = grid.toString();
			if (oldGrid.equals(newGrid)) {
				Optional<Square> square = grid.getGridAsStream().filter(s -> s.getState() == SquareState.COVERED).findAny();
				if (square.isPresent() && grid.uncoverSquare(square.get().getLocation()) == UncoverResult.BOMB) {
					break;
				} else {
					if (grid.getConfig().getN_ROWS() * grid.getConfig().getN_COLUMNS() - grid.getConfig().getN_BOMBS() == grid.getGridAsStream().filter(s -> s.getState() == SquareState.UNCOVERED).count()) {
						break;
					} else {
						continue;
					}
				}
			}
		} while(true);
	}

	public static void main(String[] args) {
		int win = 0;
		int lose = 0;
		for (int i = 0; i < 1000; i++) {
			Grid grid = new Grid(new Configuration(9, 9, 10));
			grid.populate();
			GridController controller = new GridController(grid);
			Solver solver = new Solver(grid);
			Location randomLocation;
			do {
				randomLocation = grid.getRandomPoint();
			} while(grid.getSquareAt(randomLocation).getType() == SquareType.BOMB);
			controller.uncoverSquare(randomLocation);
			solver.nextState();
			if (grid.getConfig().getN_ROWS() * grid.getConfig().getN_COLUMNS() - grid.getConfig().getN_BOMBS() == grid.getGridAsStream().filter(s -> s.getState() == SquareState.UNCOVERED).count()) {
				win++;
			} else {
				lose++;
			}
		}
		System.out.println("Win: " + win + ", Lose: " + lose + ", Win Rate: " + (double)win/(win+lose)*100f);
	}
}
