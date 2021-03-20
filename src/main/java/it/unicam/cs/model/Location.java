package it.unicam.cs.model;

import lombok.Getter;
import lombok.AllArgsConstructor;

/**
 * Class to represent the location of a Square, expressed in terms of row and column.
 *
 */
@Getter
@AllArgsConstructor
public class Location {
	/** The row of a Square **/
	private int row;
	/** The column of a Square **/
	private int column;
}
