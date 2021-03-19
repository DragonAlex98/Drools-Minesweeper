package it.unicam.cs;

import java.util.Scanner;

import it.unicam.cs.controller.GridController;
import it.unicam.cs.model.Grid;
import it.unicam.cs.model.Location;

public class Main {

	public static void main(String[] args) {
		Grid grid = new Grid(9, 9, 10);
		grid.populate();
		System.out.println(grid);
		GridController controller = new GridController(grid);
		try (Scanner scanner = new Scanner(System.in)) {
			while (true) {
				boolean flagging = false;
				boolean square = false;
				boolean chord = false;
				System.out.println("Write F for Flagging and S for Square and D for Double Click");
				String s = scanner.next();
				if (s.equals("F")) {
					flagging = true;
					square = false;
					chord = false;
				} else if (s.equals("S")) {
					flagging = false;
					square = true;
					chord = false;
				} else if (s.equals("D")) {
					flagging = false;
					square = false;
					chord = true;
				}
				System.out.println("Next square row");
				int row = scanner.nextInt();
				System.out.println("Next square column");
				int column = scanner.nextInt();
				if (flagging) {
					controller.flagSquare(new Location(row, column));
				} else if (square) {
					controller.uncoverSquare(new Location(row, column));					
				} else if (chord) {
					controller.chordSquare(new Location(row, column));
				}
				System.out.println(grid);
			}
		}
	}
}
