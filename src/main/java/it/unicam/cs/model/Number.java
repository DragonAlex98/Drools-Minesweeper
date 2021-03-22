package it.unicam.cs.model;

import it.unicam.cs.enumeration.SquareType;
import lombok.Getter;

/**
 * Class to represent a Number Square in the Minesweeper game (at least one Bomb
 * in its neighbors).
 *
 */
public class Number extends Square {

	/** Number of bombs in my neighbors **/
	@Getter
	private int neighbourBombsCount = 0;

	public Number(Location location, int neighbourBombsCount) {
		super(SquareType.NUMBER, location);
		this.neighbourBombsCount = neighbourBombsCount;
	}

	/**
	 * Method to return the number representing this Square as a String.
	 * 
	 * @return The number representing this Square.
	 */
	public String getNumber() {
		return String.format("%d", neighbourBombsCount);
	}

	@Override
	public String toString() {
		return String.format("[%-9s %d]", this.getState(), neighbourBombsCount);
	}
}
