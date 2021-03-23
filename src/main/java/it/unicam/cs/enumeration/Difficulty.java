package it.unicam.cs.enumeration;

import it.unicam.cs.model.Configuration;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Difficulty {
	BEGINNER(9, 9, 10), INTERMEDIATE(16, 16, 40), EXPERT(16, 30, 99);
	
	private int nRows;
	private int nColumns;
	private int nBombs;
	
	public Configuration getConfiguration() {
		return new Configuration(nRows, nColumns, nBombs);
	}
}
