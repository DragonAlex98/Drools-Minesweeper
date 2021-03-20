package it.unicam.cs.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import it.unicam.cs.enumeration.SquareType;

/**
 * Class to represent the grid of the game containing the Squares.
 *
 */
public class Grid {
	/** Matrix containing the squares **/
	private Square[][] grid;
	private final int N_ROWS, N_COLUMNS, N_BOMBS;
	private final Random RANDOM = new Random();

	public Grid(int rows, int columns, int bombs) {
		this.N_ROWS = rows;
		this.N_COLUMNS = columns;
		this.N_BOMBS = bombs;
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
	 * Methon to obtain a random Location.
	 * 
	 * @return The random Location.
	 */
	private Location getRandomPoint() {
		return new Location(RANDOM.nextInt(N_ROWS), RANDOM.nextInt(N_COLUMNS));
	}

	/**
	 * Method to check if a certain Location is valid (inside the grid).
	 * 
	 * @param location The location to check.
	 * @return True if the location is inside the grid, false otherwise.
	 */
	private boolean isLocationInsideGrid(Location location) {
		return location.getRow() >= 0 && location.getRow() < N_ROWS && location.getColumn() >= 0
				&& location.getColumn() < N_COLUMNS;
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
		this.grid = new Square[N_ROWS][N_COLUMNS];

		for (int i = 0; i < N_BOMBS; i++) {
			Location newPoint;
			do {
				newPoint = getRandomPoint();
			} while (getSquareAt(newPoint) != null);
			setSquareAt(new Bomb(newPoint), newPoint);
		}

		for (int r = 0; r < N_ROWS; r++) {
			for (int c = 0; c < N_COLUMNS; c++) {
				Location location = new Location(r, c);
				if (getSquareAt(location) == null) {
					int numOfBombs = getNeighbourBombsCount(new Location(r, c));
					if (numOfBombs == 0) {
						setSquareAt(new Empty(location), location);
					} else {
						setSquareAt(new Number(location, numOfBombs), location);
					}
				}
			}
		}
	}

	@Override
	public String toString() {
		String s = "";
		for (int r = 0; r < N_ROWS; r++) {
			for (int c = 0; c < N_COLUMNS; c++) {
				s += getSquareAt(new Location(r, c));
			}
			s += "\r\n";
		}
		return s;
	}
}
