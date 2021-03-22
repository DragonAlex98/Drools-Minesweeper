package it.unicam.cs;

import it.unicam.cs.controller.GridController;
import it.unicam.cs.model.Configuration;
import it.unicam.cs.model.Grid;
import it.unicam.cs.view.MainFrame;

public class RulesTest {

	public static void main(String[] args) {		
		Configuration configuration = new Configuration(9, 9,  10);
		Grid grid = new Grid(configuration);
		new MainFrame("Minesweeper", grid, new GridController(grid));		
	}
}
