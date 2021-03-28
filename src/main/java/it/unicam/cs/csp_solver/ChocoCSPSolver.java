package it.unicam.cs.csp_solver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.ListUtils;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;

import it.unicam.cs.enumeration.SquareState;
import it.unicam.cs.enumeration.SquareType;
import it.unicam.cs.model.Configuration;
import it.unicam.cs.model.Empty;
import it.unicam.cs.model.Grid;
import it.unicam.cs.model.Location;
import it.unicam.cs.model.Number;
import it.unicam.cs.model.Square;

public class ChocoCSPSolver {
	
	private Grid grid;
	
	private Model cspModel;
	
	public ChocoCSPSolver(Grid grid) {
		this.grid = grid;
		this.cspModel = new Model("Alfredino CSP");
	}
	

	private List<Variable> customInitVariables() {
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
	
	private List<Constraint> customInitConstraints(List<Variable> variables) {
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

	private void initConstraints(IntVar[][] chochoVars, List<Constraint> constraints) {
		for (Constraint constraint : constraints) {
			
			if (constraint.getScope().stream().anyMatch(MergedVariable.class::isInstance))
				continue;
			
			IntVar[] chocoScopeVars = new IntVar[constraint.getScope().size()];
			
			for (int i = 0; i < chocoScopeVars.length; i++) {
				Variable var = constraint.getScope().get(i);
				chocoScopeVars[i] = chochoVars[var.getSquare().getLocation().getRow()][var.getSquare().getLocation().getColumn()];
			}
			
			this.cspModel.sum(chocoScopeVars, "=", constraint.getEffectiveLabel()).post();
			
		}
	}

	private CSP initCSP() {
		List<Variable> variables = this.customInitVariables();
		List<Constraint> constraints = this.customInitConstraints(variables);
		constraints = this.reduceConstraintsScope(constraints);
		List<MergedVariable> mergedVars = this.aggregateConstraintsScopes(constraints);
		Collections.sort(constraints);
		
		
		IntVar[][] chocoVariables = this.initVariables();
		
		this.initConstraints(chocoVariables, constraints);
		
		Solver solver = this.cspModel.getSolver();
		
		IntVar[] toCheckVariables = null;
		Set<Variable> coveredNeighborsOfFrontierVariables = new HashSet<Variable>();
		
		for (Variable var : VariableUtils.getInstance().getFrontierVariables(variables)) {
			coveredNeighborsOfFrontierVariables.addAll(this.grid.getNeighboursAsStream(var.getSquare().getLocation()).filter(neigh -> neigh.getState() == SquareState.COVERED).map(square -> VariableUtils.getInstance().getVariableFromSquare(square, variables)).collect(Collectors.toSet()));
		}
		
		toCheckVariables = new IntVar[coveredNeighborsOfFrontierVariables.size()];
		
		toCheckVariables = coveredNeighborsOfFrontierVariables.stream().map(covvar -> chocoVariables[covvar.getSquare().getLocation().getRow()][covvar.getSquare().getLocation().getColumn()]).toArray(IntVar[]::new);
		
		Solution solution = new Solution(cspModel, toCheckVariables);
		
		if(solver.solve()){
		    // do something, e.g. print out variable values
			System.out.println(solver.getDecisionCount());
			solution.record();
		}else {
		    System.out.println("The solver has proved the problem has no solution");
		}
		
		return new CSP("alfredino", variables, constraints);
	}


	public IntVar[][] initVariables() {
		IntVar[][] variables = new IntVar[this.grid.getConfig().getN_ROWS()][this.grid.getConfig().getN_COLUMNS()];
		
		for (int r = 0; r < this.grid.getConfig().getN_ROWS(); r++) {
			for (int c = 0; c < this.grid.getConfig().getN_COLUMNS(); c++) {
				Square square = this.grid.getSquareAt(new Location(r, c));
				//Variable variable = null;
				String varName = "v(" + r + "," + c + ")";
				if (square.getState() == SquareState.FLAGGED) {
					variables[r][c] = this.cspModel.intVar(varName, 1);
				} else if (square.getState() == SquareState.UNCOVERED) {
					variables[r][c] = this.cspModel.intVar(varName, 0);
					/*
					variable = new Variable(square, 0);
					if (square instanceof Empty) {
						variable.setAssignedValue(0);
					} else if (square instanceof Number) {
						variable.setAssignedValue(((Number) square).getNeighbourBombsCount());
					}
					*/
				} else {
					variables[r][c] = this.cspModel.intVar(varName, 0, 1);
					//variable = new Variable(square, 0, 1);
				}
				//variables.add(variable);
			}
		}
		
		return variables;
	}
	
	public static void main(String[] args) {
		
		Grid grid = new Grid(new Configuration(10, 10, 20));
		grid.populate();
		Location randomEmptyLocation = grid.getGridAsStream().filter(sq -> sq.getState() == SquareState.COVERED && sq.getType() == SquareType.EMPTY).findAny().get().getLocation();
		grid.uncoverSquare(randomEmptyLocation);
		System.out.println(grid);
		/*
		randomEmptyLocation = grid.getGridAsStream().filter(sq -> sq.getState() == SquareState.COVERED && sq.getType() == SquareType.EMPTY).findAny().get().getLocation();
		grid.uncoverSquare(randomEmptyLocation);
		System.out.println(grid);
		CSPSolver solver = new CSPSolver(grid);
		*/
		
		ChocoCSPSolver cspSolver = new ChocoCSPSolver(grid);
		
		cspSolver.initCSP();
		
		
		System.out.println("");
	}
}
