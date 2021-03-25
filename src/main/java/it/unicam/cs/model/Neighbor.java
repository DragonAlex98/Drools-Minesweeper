package it.unicam.cs.model;

import lombok.Getter;

@Getter
public class Neighbor {
	private Square square;
    private Square neighbor;
    
    public Neighbor(Square cell, Square neighbor) {
        this.square = cell;
        this.neighbor = neighbor;
    }
    
    public String toString() {
        return "square '"+ this.square + "' neighbour '" + this.neighbor + "'";
    }
}
