package it.unicam.cs.model;

import it.unicam.cs.enumeration.MinesweeperEventAction;
import it.unicam.cs.model.Location;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MinesweeperEvent {
	private final MinesweeperEventAction ACTION;
	
	private Location location;
}
