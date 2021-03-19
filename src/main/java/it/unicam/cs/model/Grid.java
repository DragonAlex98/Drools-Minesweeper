package it.unicam.cs.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import it.unicam.cs.enumeration.SquareType;

public class Grid {
	private Square[][] grid;
	private final int N_ROWS, N_COLUMNS, N_BOMBS;
	private final Random RANDOM = new Random();

	public Grid(int rows, int columns, int bombs) {
		this.N_ROWS = rows;
		this.N_COLUMNS = columns;
		this.N_BOMBS = bombs;
	}

	public Stream<Square> getGridAsStream() {
		return Arrays.stream(grid).flatMap(Arrays::stream);
	}

	public Stream<Square> getNeighboursAsStream(Location location) {
		List<Square> neighbours = new ArrayList<Square>();
		for (int r = -1; r <= 1; r++) {
			for (int c = - 1; c <= 1; c++) {
				Location newLocation = new Location(location.getRow()+r, location.getColumn()+c);
				if (!(r == 0 && c == 0) && isLocationInsideGrid(newLocation)) {
					neighbours.add(getSquareAt(newLocation));
				}
			}
		}
		return neighbours.stream();
	}

	public Square getSquareAt(Location location) {
		return grid[location.getRow()][location.getColumn()];
	}

	private void setSquareAt(Square square, Location location) {
		this.grid[location.getRow()][location.getColumn()] = square;
	}

	private Location getRandomPoint() {
		return new Location(RANDOM.nextInt(N_ROWS), RANDOM.nextInt(N_COLUMNS));
	}

	private boolean isLocationInsideGrid(Location location) {
		return location.getRow() >= 0 && location.getRow() < N_ROWS && location.getColumn() >= 0 && location.getColumn() < N_COLUMNS;
	}
	
	private int getNeighbourBombsCount(Location location) {
		return (int) getNeighboursAsStream(location).filter(s -> s != null && s.getType() == SquareType.BOMB).count();
	}

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
