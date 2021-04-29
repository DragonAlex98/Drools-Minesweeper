package it.unicam.cs.enumeration;

import it.unicam.cs.model.Configuration;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Enumeration to represent the difficulty of the game (Beginner, Intermediate, Expert).
 *
 */
@Getter
@AllArgsConstructor
public enum Difficulty {
	/** Beginner difficulty to represent a 9x9 grid with 10 bombs **/
	BEGINNER(9, 9, 10),
	/** Intermediate difficulty to represent a 16x16 grid with 40 bombs **/
	INTERMEDIATE(16, 16, 40),
	/** Expert difficulty to represent a 16x30 grid with 99 bombs **/
	EXPERT(16, 30, 99);
	
	/** Number of rows **/
	private int nRows;
	/** Number of columns **/
	private int nColumns;
	/** Number of bombs **/
	private int nBombs;
	
	public Configuration getConfiguration() {
		return new Configuration(nRows, nColumns, nBombs);
	}
}
