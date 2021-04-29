package it.unicam.cs.model;

import lombok.Getter;

/**
 * Class to represent a Neighbor object composed by two Square (one Square must be close to the other).
 *
 */
@Getter
public class Neighbor {
	/** The main square **/
	private Square square;
	/** The square that is close to the main square **/
	private Square neighbor;
    
    public Neighbor(Square cell, Square neighbor) {
        this.square = cell;
        this.neighbor = neighbor;
    }
    
    public String toString() {
        return "square '"+ this.square + "' neighbour '" + this.neighbor + "'";
    }
}
