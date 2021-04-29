package it.unicam.cs.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import it.unicam.cs.enumeration.GameState;
import it.unicam.cs.enumeration.SquareState;
import it.unicam.cs.enumeration.SquareType;
import it.unicam.cs.utils.DroolsUtils;
import lombok.Getter;

/**
 * Class to represent the grid of the game containing the Squares.
 *
 */
public class Grid {
	private final Random RANDOM = new Random();
	/** Matrix containing the squares **/
	private Square[][] grid;
	/** The configuration of the grid **/
	@Getter
	private Configuration config;

	public Grid(Configuration config) {
		this.config = config;
	}

	/**
	 * Method to obtain all the elements (squares) of the grid as a Stream.
	 * 
	 * @return The Stream of squares contained in the grid.
	 */
	public Stream<Square> getGridAsStream() {
		return Arrays.stream(grid).flatMap(Arrays::stream);
	}

	/**
	 * Method to obtain all the neighbors of a Square having a certain location.
	 * 
	 * @param location The location of the considered Square.
	 * @return The Stream containing all the neighbors of the considered Square.
	 */
	public Stream<Square> getNeighboursAsStream(Location location) {
		List<Square> neighbours = new ArrayList<Square>();
		for (int r = -1; r <= 1; r++) {
			for (int c = -1; c <= 1; c++) {
				Location newLocation = new Location(location.getRow() + r, location.getColumn() + c);
				if (!(r == 0 && c == 0) && isLocationInsideGrid(newLocation)) {
					neighbours.add(getSquareAt(newLocation));
				}
			}
		}
		return neighbours.stream();
	}

	/**
	 * Method to obtain a Square having a certain location.
	 * 
	 * @param location The location of the considered Square.
	 * @return The Square at the considered location.
	 */
	public Square getSquareAt(Location location) {
		return grid[location.getRow()][location.getColumn()];
	}

	/**
	 * Method to place a given Square in a certain location.
	 * 
	 * @param square   The Square to place.
	 * @param location The location where to place the Square.
	 */
	private void setSquareAt(Square square, Location location) {
		this.grid[location.getRow()][location.getColumn()] = square;
	}

	/**
	 * Method to obtain a random Location.
	 * 
	 * @return The random Location.
	 */
	public Location getRandomPoint() {
		return new Location(RANDOM.nextInt(config.getN_ROWS()), RANDOM.nextInt(config.getN_COLUMNS()));
	}

	/**
	 * Method to check if a certain Location is valid (inside the grid).
	 * 
	 * @param location The location to check.
	 * @return True if the location is inside the grid, false otherwise.
	 */
	private boolean isLocationInsideGrid(Location location) {
		return location.getRow() >= 0 && location.getRow() < config.getN_ROWS() && location.getColumn() >= 0
				&& location.getColumn() < config.getN_COLUMNS();
	}

	/**
	 * Method to get the number of bombs in the neighbors of a Square placed in a
	 * certain Location.
	 * 
	 * @param location The location of the Square.
	 * @return The number of bombs in the neighbors of a Square placed in the
	 *         considered Location.
	 */
	private int getNeighbourBombsCount(Location location) {
		return (int) getNeighboursAsStream(location).filter(s -> s != null && s.getType() == SquareType.BOMB).count();
	}
	
	/**
	 * Method to populate the grid according to the Configuration, avoiding the
	 * presence of a bomb in the first clicked Square
	 * 
	 * @param location The location represent the first clicked Square.
	 */
	public void populateSafeGrid(Location location) {
		do {
			populate();
		} while (getSquareAt(location).getType() == SquareType.BOMB);
		getGridAsStream().forEach(s -> DroolsUtils.getInstance().getKSession().insert(s));
		DroolsUtils.getInstance().getKSession().getAgenda().getAgendaGroup("register neighbor").setFocus();
		DroolsUtils.getInstance().getKSession().fireAllRules();
	}

	/**
	 * Method to populate the grid according to the Configuration.
	 */
	private void populate() {
		this.grid = new Square[config.getN_ROWS()][config.getN_COLUMNS()];
		// insert bombs
		for (int i = 0; i < config.getN_BOMBS(); i++) {
			Location newPoint;
			do {
				newPoint = getRandomPoint();
			} while (getSquareAt(newPoint) != null);
			Bomb bomb = new Bomb(newPoint);
			setSquareAt(bomb, newPoint);
		}
		// insert empty and number
		for (int r = 0; r < config.getN_ROWS(); r++) {
			for (int c = 0; c < config.getN_COLUMNS(); c++) {
				Location location = new Location(r, c);
				if (getSquareAt(location) == null) {
					int numOfBombs = getNeighbourBombsCount(new Location(r, c));
					if (numOfBombs == 0) {
						Empty empty = new Empty(location);
						setSquareAt(empty, location);
					} else {
						Number number = new Number(location, numOfBombs);
						setSquareAt(number, location);
					}
				}
			}
		}
	}

	/**
	 * Method to check if the Grid is populated.
	 * 
	 * @return True if the Grid is populated, false otherwise.
	 */
	public boolean isPopulated() {
		return this.grid == null ? false : true;
	}
	
	/**
	 * Method to check the State of the Game.
	 * 
	 * @return The state of the Game (Ongoing, Loss or Win).
	 */
	public GameState getGameState() {
		if (!isPopulated()) {
			return GameState.ONGOING;
		}
		if (this.getGridAsStream().filter(s -> s.getState() == SquareState.EXPLODED).findAny().isPresent()) {
			return GameState.LOSS;
		}
		if (this.config.getN_BOMBS() == getGridAsStream().filter(s -> s.getState() == SquareState.COVERED || s.getState() == SquareState.FLAGGED).count()) {
			return GameState.WIN;
		}
		return GameState.ONGOING;
	}

	@Override
	public String toString() {
		String s = "";
		for (int r = 0; r < config.getN_ROWS(); r++) {
			for (int c = 0; c < config.getN_COLUMNS(); c++) {
				s += grid == null ? "[           ]" : getSquareAt(new Location(r, c));
			}
			s += "\r\n";
		}
		return s;
	}
}
