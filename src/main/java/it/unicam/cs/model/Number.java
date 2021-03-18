package it.unicam.cs.model;

import it.unicam.cs.enumeration.SquareType;
import lombok.Getter;
import lombok.Setter;

public class Number extends Square {
	@Getter
	@Setter
	private int neighbourBombsCount = 0;

	public Number(Location location, int neighbourBombsCount) {
		super(SquareType.NUMBER, location);
		this.neighbourBombsCount = neighbourBombsCount;
	}
	
	@Override
	public String toString() {
		return String.format(" [%d]", neighbourBombsCount);
	}
}
