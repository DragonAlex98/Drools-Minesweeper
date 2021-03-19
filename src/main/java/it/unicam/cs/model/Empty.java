package it.unicam.cs.model;

import it.unicam.cs.enumeration.SquareType;

public class Empty extends Square {

	public Empty(Location location) {
		super(SquareType.EMPTY, location);
	}
	
	@Override
	public String toString() {
		return String.format("[%-9s  ]", this.getState());
	}
}
