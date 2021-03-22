package it.unicam.cs;

import it.unicam.cs.controller.DroolsUtils;
import it.unicam.cs.controller.GridController;
import it.unicam.cs.model.Grid;
import it.unicam.cs.view.MainFrame;

public class RulesTest {
	public static void main(String[] args) {
		
		Grid grid = DroolsUtils.getInstance().getGrid();
		
		new MainFrame("Minesweeper", grid, new GridController(grid));
		
	}
}
