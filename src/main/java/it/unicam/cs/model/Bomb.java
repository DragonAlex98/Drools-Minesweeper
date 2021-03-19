package it.unicam.cs.model;

import it.unicam.cs.enumeration.SquareType;

public class Bomb extends Square {

	public Bomb(Location location) {
		super(SquareType.BOMB, location);
	}
	
	@Override
	public String toString() {
		return String.format("[%-9s B]", this.getState());
	}
}
