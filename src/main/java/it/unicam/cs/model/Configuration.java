package it.unicam.cs.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Class to represent the parameters useful to setup the grid of the game.
 *
 */
@Getter
@AllArgsConstructor
public class Configuration {
	/** Number of rows to use for the grid. **/
	private final int N_ROWS;
	/** Number of columns to use for the grid. **/
	private final int N_COLUMNS;
	/** Number of bombs to place in the grid. **/
	private final int N_BOMBS;
}
