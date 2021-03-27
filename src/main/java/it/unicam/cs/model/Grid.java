package it.unicam.cs.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Stream;

import it.unicam.cs.controller.DroolsUtils;
import it.unicam.cs.enumeration.GameState;
import it.unicam.cs.enumeration.SquareState;
import it.unicam.cs.enumeration.SquareType;
import it.unicam.cs.enumeration.UncoverResult;
import lombok.Getter;

/**
 * Class to represent the grid of the game containing the Squares.
 *
 */
public class Grid {
	/** Matrix containing the squares **/
	private Square[][] grid;
	/** The configuration of the grid **/
	@Getter
	private Configuration config;
	private final Random RANDOM = new Random();
	@Getter
	private GameState state;

	public Grid(Configuration config) {
		this.config = config;
		this.state = GameState.ONGOING;
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
	 * Method to populate the grid according to the Configuration.
	 */
	public void populate() {
		this.grid = new Square[config.getN_ROWS()][config.getN_COLUMNS()];

		for (int i = 0; i < config.getN_BOMBS(); i++) {
			Location newPoint;
			do {
				newPoint = getRandomPoint();
			} while (getSquareAt(newPoint) != null);
			Bomb bomb = new Bomb(newPoint);
			setSquareAt(bomb, newPoint);
			DroolsUtils.getInstance().getKSession().insert(bomb);
		}

		for (int r = 0; r < config.getN_ROWS(); r++) {
			for (int c = 0; c < config.getN_COLUMNS(); c++) {
				Location location = new Location(r, c);
				if (getSquareAt(location) == null) {
					int numOfBombs = getNeighbourBombsCount(new Location(r, c));
					if (numOfBombs == 0) {
						Empty empty = new Empty(location);
						setSquareAt(empty, location);
						DroolsUtils.getInstance().getKSession().insert(empty);
					} else {
						Number number = new Number(location, numOfBombs);
						setSquareAt(number, location);
						DroolsUtils.getInstance().getKSession().insert(number);
					}
				}
			}
		}
		DroolsUtils.getInstance().getKSession().getAgenda().getAgendaGroup( "register neighbor" ).setFocus();
		DroolsUtils.getInstance().getKSession().fireAllRules();
		System.out.println(toString());
	}

	public boolean isPopulated() {
		return this.grid == null ? false : true;
	}
	
	public GameState getGameState() {
		if (this.getGridAsStream().filter(s -> s.getState() == SquareState.EXPLODED).findAny().isPresent()) {
			return GameState.LOSS;
		}
		if (this.config.getN_BOMBS() == getGridAsStream().filter(s -> s.getState() == SquareState.COVERED || s.getState() == SquareState.FLAGGED).count()) {
			return GameState.WIN;
		}
		return GameState.ONGOING;
	}

	public void updateGameState() {
		if (this.config.getN_BOMBS() == getGridAsStream().filter(s -> s.getState() == SquareState.COVERED || s.getState() == SquareState.FLAGGED).count()) {
			this.state = GameState.WIN;
		}
	}

	private Set<Location> emptySquaresSet = new HashSet<Location>();
	private Set<Location> tempSet = new HashSet<Location>();

	public void uncoverEmptySquare(Location loc) {
		emptySquaresSet.add(loc);

		while (!emptySquaresSet.isEmpty() || !tempSet.isEmpty()) {
			emptySquaresSet.addAll(tempSet);
			tempSet.clear();
			emptySquaresSet.forEach(l -> getSquareAt(l).setState(SquareState.UNCOVERED));
			updateGameState();
			emptySquaresSet.forEach(l -> {
				System.out.println(l);
				getNeighboursAsStream(l).filter(s -> s.getState() == SquareState.COVERED).forEach(s -> {
					if (s.getType() == SquareType.NUMBER) {
						uncoverSquare(s.getLocation());
					}
					if (s.getType() == SquareType.EMPTY) {
						tempSet.add(s.getLocation());
					}
				});
			});
			emptySquaresSet.clear();
		}
	}

	public UncoverResult newUncoverEmpty(Location location) {
		Square square = getSquareAt(location);
		square.setState(SquareState.UNCOVERED);
		updateGameState();
		return UncoverResult.SAFE;
	}

	public UncoverResult uncoverBombSquare(Location location) {
		Square square = getSquareAt(location);
		square.setState(SquareState.EXPLODED);
		getGridAsStream().filter(s -> s.getType() == SquareType.BOMB && s.getState() == SquareState.COVERED)
				.forEach(s -> s.setState(SquareState.UNCOVERED));
		this.state = GameState.LOSS;
		return UncoverResult.BOMB;
	}

	public UncoverResult uncoverNumberSquare(Location location) {
		Square square = getSquareAt(location);
		square.setState(SquareState.UNCOVERED);
		updateGameState();
		return UncoverResult.SAFE;
	}

	/**
	 * Method used to uncover a square placed in a certain location.
	 * 
	 * @param location The location to uncover.
	 * @return The result of the uncovery.
	 */
	public UncoverResult uncoverSquare(Location location) {
		Square square = getSquareAt(location);
		if (square.getState() != SquareState.COVERED) {
			return UncoverResult.NO_ACTION;
		}

		if (square.getType() == SquareType.BOMB) {
			square.setState(SquareState.EXPLODED);
			getGridAsStream().filter(s -> s.getType() == SquareType.BOMB && s.getState() == SquareState.COVERED)
					.forEach(s -> s.setState(SquareState.UNCOVERED));
			this.state = GameState.LOSS;
			return UncoverResult.BOMB;
		}

		if (square.getType() == SquareType.NUMBER) {
			uncoverNumberSquare(square.getLocation());
			return UncoverResult.SAFE;
		}

		uncoverEmptySquare(square.getLocation());
		return UncoverResult.SAFE;
	}

	/**
	 * Method used to flag/unflag a square placed in a certain location
	 * 
	 * @param location The location to flag/unflag.
	 */
	public void flagUnflagSquare(Location location) {
		Square square = getSquareAt(location);
		if (square.getState() == SquareState.COVERED) {
			square.setState(SquareState.FLAGGED);
		} else if (square.getState() == SquareState.FLAGGED) {
			square.setState(SquareState.COVERED);
		}
	}

	/**
	 * Method used to flag/unflag a square placed in a certain location
	 * 
	 * @param location The location to flag/unflag.
	 */
	public void flagSquare(Location location) {
		Square square = getSquareAt(location);
		square.setState(SquareState.FLAGGED);
	}

	/**
	 * Method used to flag/unflag a square placed in a certain location
	 * 
	 * @param location The location to flag/unflag.
	 */
	public void unflagSquare(Location location) {
		Square square = getSquareAt(location);
		square.setState(SquareState.COVERED);
	}

	/**
	 * Method to uncover the neighbors of an Uncovered Number Square placed in a
	 * certain location if it has a number of flags (in its neighbors) equals to its
	 * number.
	 * 
	 * @param location The location for which the chord action is required.
	 * @return The result of the chord.
	 */
	public UncoverResult chordSquare(Location location) {
		Square square = getSquareAt(location);
		if (square.getState() != SquareState.UNCOVERED || square.getType() != SquareType.NUMBER) {
			return UncoverResult.NO_ACTION;
		}

		Number numberSquare = (Number) square;
		if (numberSquare.getNeighbourBombsCount() != getNeighboursAsStream(location)
				.filter(s -> s.getState() == SquareState.FLAGGED).count()) {
			return UncoverResult.NO_ACTION;
		}

		Iterator<Square> iterator = getNeighboursAsStream(location).iterator();
		while (iterator.hasNext()) {
			Square currentSquare = iterator.next();
			if (currentSquare.getState() != SquareState.COVERED) {
				continue;
			}
			UncoverResult result = uncoverSquare(currentSquare.getLocation());
			if (result == UncoverResult.BOMB) {
				return UncoverResult.BOMB;
			}
		}
		return UncoverResult.SAFE;
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
		s += "State: " + state + "\r\n";
		return s;
	}
}
