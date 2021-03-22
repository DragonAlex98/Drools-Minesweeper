package it.unicam.cs.model;

import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

/**
 * Class to represent the location of a Square, expressed in terms of row and column.
 *
 */
@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class Location {
	/** The row of a Square **/
	private int row;
	/** The column of a Square **/
	private int column;
}
