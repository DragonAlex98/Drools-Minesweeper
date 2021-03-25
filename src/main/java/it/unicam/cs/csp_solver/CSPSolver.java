package it.unicam.cs.csp_solver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


import it.unicam.cs.controller.GridController;
import it.unicam.cs.enumeration.SquareState;
import it.unicam.cs.enumeration.SquareType;
import it.unicam.cs.model.Configuration;
import it.unicam.cs.model.Empty;
import it.unicam.cs.model.Grid;
import it.unicam.cs.model.Location;
import it.unicam.cs.model.Number;
import it.unicam.cs.model.Square;

/**
 * This is the main class that manages the solver that uses CSP. 
 * 
 * @author RICCARDO
 *
 */
public class CSPSolver {
	private Grid grid;
	
	private CSP csp;
	
	public CSPSolver(Grid grid) {
		this.grid = grid;
		this.csp = this.initCSP();
	}

	/**
	 * Initialize the variables.
	 * 
	 * For each square in the grid i check its state and i create a variable:
	 * 
	 * If the square is flagged then its domain has to be 1.
	 * If the square is uncovered then its domain has to be 0, if the square is empty, otherwise it is its label.
	 * 
	 * If it is none of the above then its domain has to be [0,1].
	 * 
	 * @return
	 */
	private List<Variable> initVariables() {
		List<Variable> variables = new ArrayList<Variable>();
		
		for (int r = 0; r < this.grid.getConfig().getN_ROWS(); r++) {
			for (int c = 0; c < this.grid.getConfig().getN_COLUMNS(); c++) {
				Square square = this.grid.getSquareAt(new Location(r, c));
				Variable variable = null;
				if (square.getState() == SquareState.FLAGGED) {
					variable = new Variable(square, 1);
				} else if (square.getState() == SquareState.UNCOVERED) {
					variable = new Variable(square, 0);
					if (square instanceof Empty) {
						variable.setAssignedValue(0);
					} else if (square instanceof Number) {
						variable.setAssignedValue(((Number) square).getNeighbourBombsCount());
					}
				} else {
					variable = new Variable(square, 0, 1);
				}
				variables.add(variable);
			}
		}
		
		return variables;
	}
	
	/**
	 * Initialize the constraints.
	 * 
	 * First of all retrieve only the variables that are in the frontier.
	 * 
	 * The scope of each frontier variable is composed by all the covered variables in their neighbor.
	 * 
	 * Then calculate the effective label of the variable related to this constraint, that is the label - number of flag placed in its neighbor.
	 * 
	 * At the end all the constraints are sorted considering the number of variables in their scope.
	 * 
	 * @param variables
	 * @return
	 */
	private List<Constraint> initConstraints(List<Variable> variables) {
		List<Constraint> constraints = new ArrayList<Constraint>();
		
		List<Variable> frontierVariables = VariableUtils.getInstance().getFrontierVariables(variables);
		
		for (Variable variable : frontierVariables) {
			List<Square> coveredNeighbors = this.grid.getNeighboursAsStream(variable.getSquare().getLocation()).filter(neighbor -> neighbor.getState() == SquareState.COVERED).collect(Collectors.toList());
			
			List<Variable> scope = VariableUtils.getInstance().getVariablesFromSquares(coveredNeighbors, variables);
			
			if (scope.isEmpty())
				continue;
			
			Integer sum = ((Number) variable.getSquare()).getNeighbourBombsCount();
			
			Long flaggedNeighborNumber = this.grid.getNeighboursAsStream(variable.getSquare().getLocation()).filter(neighbor -> neighbor.getState() == SquareState.FLAGGED).count();
			
			sum -= flaggedNeighborNumber.intValue();
			
			constraints.add(new Constraint(variable, scope, sum));
		}
		
		Collections.sort(constraints);
		
		return constraints;
	}
	
	private CSP initCSP() {
		List<Variable> variables = this.initVariables();
		List<Constraint> constraints = this.initConstraints(variables);
		return new CSP("alfredino", variables, constraints);
	}
	
	public static void main(String[] args) {
		Grid grid = new Grid(new Configuration(9, 9, 10));
		grid.populate();
		GridController controller = new GridController(grid);
		Location randomEmptyLocation = grid.getGridAsStream().filter(sq -> sq.getState() == SquareState.COVERED && sq.getType() == SquareType.EMPTY).findAny().get().getLocation();
		grid.uncoverSquare(randomEmptyLocation);
		System.out.println(grid);
		CSPSolver solver = new CSPSolver(grid);
		
		/*
		do {
			randomLocation = grid.getRandomPoint();
		} while(grid.getSquareAt(randomLocation).getType() == SquareType.BOMB);
		controller.uncoverSquare(randomLocation);
		solver.nextState();
		if (grid.getState() == GameState.WIN) {
			win++;
		} else {
			lose++;
		}
		*/
	}
}
