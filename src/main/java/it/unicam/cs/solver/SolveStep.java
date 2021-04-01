package it.unicam.cs.solver;

import java.util.List;

import it.unicam.cs.model.Location;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SolveStep {
	private List<Location> locationsToFlag;
	private List<Location> locationsToUncover;
}
