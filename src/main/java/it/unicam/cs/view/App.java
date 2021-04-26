package it.unicam.cs.view;

import it.unicam.cs.enumeration.Difficulty;
import it.unicam.cs.model.Grid;

public class App {

	public static void main(String[] args) {
		Grid grid = new Grid(Difficulty.BEGINNER.getConfiguration());
		new MainFrame("Minesweeper", grid);
	}
}
