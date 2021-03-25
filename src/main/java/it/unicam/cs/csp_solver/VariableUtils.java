package it.unicam.cs.csp_solver;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import it.unicam.cs.enumeration.SquareState;
import it.unicam.cs.model.Square;

/**
 * This is just a utility class to help using the variables since it is often necessary to "bind" Squares and Variables. 
 * 
 * @author RICCARDO
 *
 */
public class VariableUtils {
	private final static VariableUtils INSTANCE = null;
	
	private VariableUtils() {
		
	}
	
	public static VariableUtils getInstance() {
		if (INSTANCE == null) {
			return new VariableUtils();
		}
		
		return INSTANCE;
	}
	
	/**
	 * Given a list of variables returns those ones that are in the frontier.
	 * 
	 * @param variables
	 * @return
	 */
	public List<Variable> getFrontierVariables(List<Variable> variables) {
		List<Variable> frontierVariables = variables.stream().filter(var -> var.getSquare().getState() == SquareState.UNCOVERED && var.getAssignedValue() != 0).collect(Collectors.toList());
		return frontierVariables;
	}
	
	/**
	 * Given a square returns the associated variable.
	 * 
	 * @param square
	 * @param variables
	 * @return
	 */
	public Variable getVariableFromSquare(Square square, List<Variable> variables) {
		Optional<Variable> var = variables.stream().filter(variable -> variable.getSquare().equals(square)).findAny();
		
		if (var.isPresent())
			return var.get();
		
		return null;
		
	}
	
	/**
	 * Given a list of squares returns a list of associated variable.
	 * 
	 * @param squares
	 * @param variables
	 * @return
	 */
	public List<Variable> getVariablesFromSquares(List<Square> squares, List<Variable> variables) {
		List<Variable> foundVars = new ArrayList<Variable>();
		
		for (Square square : squares) {
			try {
				foundVars.add(this.getVariableFromSquare(square, variables));
			} catch (NullPointerException e) {
				continue;
			}
		}
		
		return foundVars;
	}
	
}
