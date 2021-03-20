package it.unicam.cs.model;

import it.unicam.cs.enumeration.SquareState;
import it.unicam.cs.enumeration.SquareType;
import lombok.Getter;
import lombok.Setter;

/**
 * Abstract class to represent a Square in the Minesweeper game.
 *
 */
@Getter
public abstract class Square {

	/** The state of this square **/
	@Setter
	private SquareState state;
	/** The type of this square **/
	private SquareType type;
	/** The location of this square **/
	private Location location;

	public Square(SquareType type, Location location) {
		this.state = SquareState.COVERED;
		this.type = type;
		this.location = location;
	}
}
