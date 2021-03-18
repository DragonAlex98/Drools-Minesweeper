package it.unicam.cs;

import it.unicam.cs.model.Grid;

public class Main {

	public static void main(String[] args) {
		Grid grid = new Grid(10, 10, 25);
		grid.populate();
		System.out.println(grid);
	}
}
