package it.unicam.cs.view;

import it.unicam.cs.controller.GridController;
import it.unicam.cs.model.Configuration;
import it.unicam.cs.model.Grid;

public class App {

	public static void main(String[] args) {
		Configuration config = new Configuration(9, 9, 10);
		Grid grid = new Grid(config);
		GridController controller = new GridController(grid);

		new MainFrame("Minesweeper", grid, controller);
	}
}
