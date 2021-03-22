package it.unicam.cs.model;

import it.unicam.cs.enumeration.MinesweeperEventAction;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class MinesweeperEvent {
	private final MinesweeperEventAction ACTION;
	private Location location;
}
