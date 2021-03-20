package it.unicam.cs.model;

import it.unicam.cs.enumeration.SquareType;

/**
 * Class to represent an Empty Square in the Minesweeper game (no Bomb in its neighbors).
 *
 */
public class Empty extends Square {

	public Empty(Location location) {
		super(SquareType.EMPTY, location);
	}
	
	@Override
	public String toString() {
		return String.format("[%-9s  ]", this.getState());
	}
}
