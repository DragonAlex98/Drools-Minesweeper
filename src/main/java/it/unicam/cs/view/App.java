package it.unicam.cs.view;

import javax.swing.SwingUtilities;

import it.unicam.cs.controller.GridController;
import it.unicam.cs.model.Configuration;
import it.unicam.cs.model.Grid;

public class App {

	public static void main(String[] args) {
		Configuration config = new Configuration(7, 9, 10);
		Grid grid = new Grid(config);
		grid.populate();
		System.out.println(grid);
		GridController controller = new GridController(grid);

		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				new MainFrame("Minesweeper", grid, controller);
			}
		});
	}
}
