package it.unicam.cs.solver;

import java.util.List;

import it.unicam.cs.model.Location;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 
 * Class to represent a step in terms of locations to flag and uncover that the
 * Solver has to perform.
 *
 */
@Getter
@AllArgsConstructor
public class SolveStep {
	/** List of locations to flag **/
	private List<Location> locationsToFlag;
	/** List of locations to uncover **/
	private List<Location> locationsToUncover;
}
