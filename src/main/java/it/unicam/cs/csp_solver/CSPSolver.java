package it.unicam.cs.csp_solver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
		
		List<Variable> frontierVariables = VariableUtils.getInstance().getFrontierVariables(variables, this.grid);
		
		for (Variable variable : frontierVariables) {
			List<Square> coveredNeighbors = this.grid.getNeighboursAsStream(variable.getSquare().getLocation()).filter(neighbor -> neighbor.getState() == SquareState.COVERED).collect(Collectors.toList());
			
			List<Variable> scope = VariableUtils.getInstance().getVariablesFromSquares(coveredNeighbors, variables.stream().collect(Collectors.toSet()));
			
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
	
	/**
	 * This method reduces the scope of constraints.
	 * 
	 * Example:
	 * 
	 * c1=[v1,v2,v3] and c2=[v1,v2] then c1-->[v3]
	 * 
	 * However in the case:
	 * 
	 * c1=[v1,v2,v3] and c2=[v3,v4] then nothing happens
	 * c1=[v1,v2,v3] and c2=[v2,v3,v4] then nothing happens
	 * 
	 * @param constraints
	 * @return
	 */
	private List<Constraint> reduceConstraintsScope(List<Constraint> constraints) {
		for (Constraint constraint : constraints) {
			// Take only the constraint that are connected to this one (that contain some element of its scope in themselves).
			List<Constraint> connectedConstraints = constraints.stream()
															   .filter(constr -> !constr.equals(constraint) && !Collections.disjoint(constraint.getScope(), constr.getScope()))
															   .sorted()
															   .collect(Collectors.toList());
			
			for (Constraint connectedConstraint : connectedConstraints) {
				
				Set<Variable> scopesUnion = new HashSet<Variable>();

				scopesUnion.addAll(constraint.getScope());
				scopesUnion.addAll(connectedConstraint.getScope());
				
				/* DEBUG ONLY
		        StringBuffer sBuffer = new StringBuffer();
		        
		        sBuffer.append("c1 = [");
		        constraint.getScope().stream().forEach(var -> sBuffer.append("(" + var.getSquare().getLocation().getRow() + ", " + var.getSquare().getLocation().getColumn() + "); "));
		        sBuffer.append("]\n");
		        
		        sBuffer.append("c2 = [");
		        connectedConstraint.getScope().stream().forEach(var -> sBuffer.append("(" + var.getSquare().getLocation().getRow() + ", " + var.getSquare().getLocation().getColumn() + "); "));
		        sBuffer.append("]\n");
		        
		        System.out.println(sBuffer.toString());
		        
				System.out.println("Is c1 reducible? " + (scopesUnion.equals(constraint.getScope().stream().collect(Collectors.toSet())) ? "Yes" : "No" ));
				*/
				
				if (constraint.getScope().equals(connectedConstraint.getScope()))
					continue;
				if (scopesUnion.equals(constraint.getScope().stream().collect(Collectors.toSet()))) {
					constraint.setScope(constraint.getScope().stream()
													.filter(elem -> !connectedConstraint.getScope().contains(elem))
													.collect(Collectors.toList()));
					constraint.setEffectiveLabel(constraint.getEffectiveLabel() - connectedConstraint.getEffectiveLabel());
					//System.out.println("Thus, c1 has become " + constraint);
				} else if (scopesUnion.equals(connectedConstraint.getScope().stream().collect(Collectors.toSet()))) {
					connectedConstraint.setScope(connectedConstraint.getScope().stream()
							.filter(elem -> !constraint.getScope().contains(elem))
							.collect(Collectors.toList()));
					connectedConstraint.setEffectiveLabel(connectedConstraint.getEffectiveLabel() - constraint.getEffectiveLabel());
					//System.out.println("However, c2 has become " + connectedConstraint);
				}
			}
		}
		
		Collections.sort(constraints);
		
		return constraints;
	}
	
	/**
	 * Method that aggregates the vars that are shared (minimum 2 shared) between 2 constraints and create a new var and 2 more constraints.
	 * 
	 * EXAMPLE:
	 * 
	 * c1=[v1,v2,v3] c2=[v2,v3,v4] --> creates a new var nV=(v2,v3) and creates 2 more constraints connecting the previous ones: c3=[v1,nV] c4=[nV,v4] 
	 * 
	 * @param constraints
	 * @return
	 */
	private List<MergedVariable> aggregateConstraintsScopes(List<Constraint> constraints) {
		
		List<MergedVariable> newVariables = new ArrayList<MergedVariable>();
		List<Constraint> newConstraints = new ArrayList<Constraint>();

		List<Variable> checkedCouples = new ArrayList<Variable>();
		
		//constraints.stream().forEach(System.out::println);
		
		for (Constraint constraint : constraints) {
			List<Constraint> connectedConstraints = constraints.stream()
					   .filter(constr -> !constr.equals(constraint) && !Collections.disjoint(constraint.getScope(), constr.getScope()))
					   .filter(constr -> VariableUtils.getInstance().getIntersection(constraint.getScope(), constr.getScope()).size() > 1)
					   .sorted()
					   .collect(Collectors.toList());
			
			if (connectedConstraints.isEmpty())
				continue;
			
			for (Constraint connectedConstraint : connectedConstraints) {
				
				if (checkedCouples.contains(constraint.getVariable()) && checkedCouples.contains(connectedConstraint.getVariable()))
					continue;
				if (!checkedCouples.contains(constraint.getVariable()))
					checkedCouples.add(constraint.getVariable());
				if (!checkedCouples.contains(connectedConstraint.getVariable()))
					checkedCouples.add(connectedConstraint.getVariable());
				
				List<Variable> scopesIntersection = VariableUtils.getInstance().getIntersection(constraint.getScope(), connectedConstraint.getScope());
				List<Variable> constraintDifference = VariableUtils.getInstance().getDifference(constraint.getScope(), connectedConstraint.getScope());
				List<Variable> connConstraintDifference = VariableUtils.getInstance().getDifference(connectedConstraint.getScope(), constraint.getScope());
				Integer constraintEffLabel = constraint.getEffectiveLabel();
				Integer connConstrEffLabel = connectedConstraint.getEffectiveLabel();
												
				MergedVariable mVar = new MergedVariable(scopesIntersection);
				
				if (!newVariables.contains(mVar)) {					
					newVariables.add(mVar);
				} else {
					mVar = newVariables.get(newVariables.indexOf(mVar));
				}
				
				constraintDifference.add(mVar);
				connConstraintDifference.add(mVar);
				newConstraints.add(new Constraint(constraint.getVariable(), constraintDifference, constraintEffLabel));
				newConstraints.add(new Constraint(connectedConstraint.getVariable(), connConstraintDifference, connConstrEffLabel));
				
				/* DEBUG ONLY
				System.out.println("\n");
				newConstraints.stream().forEach(System.out::println);
				*/
			}
			
		}
		
		constraints.addAll(newConstraints);
		
		return newVariables;
	}
	
	private CSP initCSP() {
		List<Variable> variables = this.initVariables();
		List<Constraint> constraints = this.initConstraints(variables);
		constraints = this.reduceConstraintsScope(constraints);
		List<MergedVariable> mergedVars = this.aggregateConstraintsScopes(constraints);
		Collections.sort(constraints);
		return new CSP("alfredino", variables, constraints);
	}

	public static void main(String[] args) {
		Grid grid = new Grid(new Configuration(10, 10, 20));
		grid.populate();
		Location randomEmptyLocation = grid.getGridAsStream().filter(sq -> sq.getState() == SquareState.COVERED && sq.getType() == SquareType.EMPTY).findAny().get().getLocation();
		grid.uncoverSquare(randomEmptyLocation);
		randomEmptyLocation = grid.getGridAsStream().filter(sq -> sq.getState() == SquareState.COVERED && sq.getType() == SquareType.EMPTY).findAny().get().getLocation();
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
