package it.unicam.cs.solver;

import it.unicam.cs.controller.DroolsUtils;
import it.unicam.cs.enumeration.GameState;
import it.unicam.cs.enumeration.SquareState;
import it.unicam.cs.enumeration.SquareType;
import it.unicam.cs.model.Configuration;
import it.unicam.cs.model.Grid;
import it.unicam.cs.model.Location;
import it.unicam.cs.model.Number;

/**
 * Class used to represent a Minesweeper Solver that uses the Single Point Strategy.
 *
 */
public class Solver {

	/**	Grid of used by the Solver to solve the game**/
	private Grid grid;

	public Solver(Grid grid) {
		this.grid = grid;
	}

	/**
	 * Method to check all mine neighbors (AMN) and possibly flag all of them.
	 */
	private void firstStep() {
		grid.getGridAsStream().filter(s -> s.getState() == SquareState.UNCOVERED && s.getType() == SquareType.NUMBER).forEach(s -> {
			long neighbours = grid.getNeighboursAsStream(s.getLocation()).filter(n -> n.getState() == SquareState.FLAGGED || n.getState() == SquareState.COVERED).count();
			if (neighbours == ((Number)s).getNeighbourBombsCount()) {
				grid.getNeighboursAsStream(s.getLocation()).filter(n -> n.getState() == SquareState.COVERED).forEach(n -> {
					insertAndFire("FLAG", n.getLocation());
				});
			}
		});
	}
	
	/**
	 * Method to check all free neighbors (AFN) and possibly uncover all of them.
	 */
	private void secondStep() {
		grid.getGridAsStream().filter(s -> s.getState() == SquareState.UNCOVERED && s.getType() == SquareType.NUMBER).forEach(s -> {
			long neighbours = grid.getNeighboursAsStream(s.getLocation()).filter(n -> n.getState() == SquareState.FLAGGED).count();
			if (neighbours == ((Number)s).getNeighbourBombsCount()) {
				grid.getNeighboursAsStream(s.getLocation()).filter(n -> n.getState() == SquareState.COVERED).forEach(n -> {
					insertAndFire("UNCOVER", n.getLocation());
				});
			}
		});
	}

	/**
	 * Method to solve the Minesweeper game using the Single Point Strategy.
	 * 
	 * @param isByStep True if the Solver should perform only one resolution step, False otherwise.
	 */
	public void solve(boolean isByStep) {
		if (!grid.isPopulated()) { // populate if not
			Location randomLocation = grid.getRandomPoint();
			grid.populateSafeGrid(randomLocation);
			insertAndFire("UNCOVER", randomLocation);
			if(isByStep) {
				return;
			}
		}
		if (grid.getGameState() != GameState.ONGOING) {
			return;
		}
		do {
			String oldGrid = grid.toString();
			firstStep();  // AMN
			secondStep(); // AFN
			String newGrid = grid.toString();
			if (oldGrid.equals(newGrid)) {
				Location randomLocation;
				do {  // if no action is performed, select random Covered Square
					randomLocation = grid.getRandomPoint();
				} while(grid.getSquareAt(randomLocation).getState() != SquareState.COVERED);
				insertAndFire("UNCOVER", randomLocation);
			}
		} while(!isByStep && grid.getGameState() == GameState.ONGOING);
	}
	
	/**
	 * Method to insert an Object into the working memory and fire the rules corresponding to its agendaGroup.
	 * 
	 * @param agendaGroup The agendGroup to use to fire the rules.
	 * @param object The object to insert into the working memory.
	 */
	private void insertAndFire(String agendaGroup, Object object) {
		DroolsUtils.getInstance().getKSession().getAgenda().getAgendaGroup(agendaGroup).setFocus();
		DroolsUtils.getInstance().getKSession().insert(object);
		DroolsUtils.getInstance().getKSession().fireAllRules();
	}

	public static void main(String[] args) {
		int win = 0;
		int lose = 0;
		for (int i = 0; i < 1000; i++) {
			DroolsUtils.getInstance().clear();
			Grid grid = new Grid(new Configuration(16, 16, 40));
			Solver solver = new Solver(grid);
			solver.solve(false);
			if (grid.getGameState() == GameState.WIN) {
				win++;
			} else {
				lose++;
			}
		}
		System.out.println("Win: " + win + ", Lose: " + lose + ", Win Rate: " + (double)win/(win+lose)*100f);
	}
}
