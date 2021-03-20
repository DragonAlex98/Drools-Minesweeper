package it.unicam.cs.model;

import it.unicam.cs.enumeration.SquareType;

/**
 * Class to represent a Bomb in the Minesweeper game.
 *
 */
public class Bomb extends Square {

	public Bomb(Location location) {
		super(SquareType.BOMB, location);
	}
	
	@Override
	public String toString() {
		return String.format("[%-9s B]", this.getState());
	}
}
