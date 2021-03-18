package it.unicam.cs.model;

import it.unicam.cs.enumeration.SquareState;
import it.unicam.cs.enumeration.SquareType;
import lombok.Getter;
import lombok.Setter;

@Getter
public abstract class Square {
	@Setter
	private SquareState state;
	private SquareType type;
	private Location location;
	
	public Square(SquareType type, Location location) {
		this.state = SquareState.COVERED;
		this.type = type;
		this.location = location;
	}
}
