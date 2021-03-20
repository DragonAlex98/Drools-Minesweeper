package it.unicam.cs.controller;

import java.util.Iterator;

import it.unicam.cs.enumeration.SquareState;
import it.unicam.cs.enumeration.SquareType;
import it.unicam.cs.enumeration.UncoverResult;
import it.unicam.cs.model.Grid;
import it.unicam.cs.model.Location;
import it.unicam.cs.model.Number;
import it.unicam.cs.model.Square;

/**
 * Class to manage the game.
 *
 */
public class GridController {
	/** The grid of the game **/
	private Grid grid;

	public GridController(Grid grid) {
		this.grid = grid;
	}

	/**
	 * Method used to setup the grid.
	 */
	public void initializeGrid() {
		grid.populate();
	}

	/**
	 * Method used to uncover an empty square placed in a certain location (possibly
	 * it also uncovers the neighbors that are not Bomb).
	 * 
	 * @param location The location of the Empty Square to uncover.
	 */
	private void uncoverEmptySquare(Location location) {
		Square square = grid.getSquareAt(location);
		square.setState(SquareState.UNCOVERED);

		grid.getNeighboursAsStream(location).filter(s -> s.getState() == SquareState.COVERED).forEach(s -> {
			if (s.getType() == SquareType.NUMBER) {
				uncoverSquare(s.getLocation());
			}
			if (s.getType() == SquareType.EMPTY) {
				uncoverEmptySquare(s.getLocation());
			}
		});
	}

	/**
	 * Method used to uncover a square placed in a certain location.
	 * 
	 * @param location The location to uncover.
	 * @return The result of the uncovery.
	 */
	public UncoverResult uncoverSquare(Location location) {
		Square square = grid.getSquareAt(location);
		if (square.getState() != SquareState.COVERED) {
			return UncoverResult.NO_ACTION;
		}

		if (square.getType() == SquareType.BOMB) {
			square.setState(SquareState.EXPLODED);
			grid.getGridAsStream().filter(s -> s.getType() == SquareType.BOMB && s.getState() == SquareState.COVERED)
					.forEach(s -> s.setState(SquareState.EXPLODED));
			return UncoverResult.BOMB;
		}

		if (square.getType() == SquareType.NUMBER) {
			square.setState(SquareState.UNCOVERED);
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
	public void flagSquare(Location location) {
		Square square = grid.getSquareAt(location);
		if (square.getState() == SquareState.COVERED) {
			square.setState(SquareState.FLAGGED);
		} else if (square.getState() == SquareState.FLAGGED) {
			square.setState(SquareState.COVERED);
		}
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
		Square square = grid.getSquareAt(location);
		if (square.getState() != SquareState.UNCOVERED || square.getType() != SquareType.NUMBER) {
			return UncoverResult.NO_ACTION;
		}

		Number numberSquare = (Number) square;
		if (numberSquare.getNeighbourBombsCount() != grid.getNeighboursAsStream(location)
				.filter(s -> s.getState() == SquareState.FLAGGED).count()) {
			return UncoverResult.NO_ACTION;
		}

		Iterator<Square> iterator = grid.getNeighboursAsStream(location).iterator();
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
}
