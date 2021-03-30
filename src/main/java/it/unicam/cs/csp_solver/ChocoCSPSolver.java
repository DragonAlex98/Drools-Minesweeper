package it.unicam.cs.csp_solver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;

import it.unicam.cs.enumeration.SquareState;
import it.unicam.cs.enumeration.SquareType;
import it.unicam.cs.model.Configuration;
import it.unicam.cs.model.Empty;
import it.unicam.cs.model.Grid;
import it.unicam.cs.model.Location;
import it.unicam.cs.model.Number;
import it.unicam.cs.model.Square;

public class ChocoCSPSolver implements MinesweeperSolver {
	
	private Grid grid;
	
	private Model cspModel;
	
	private Map<Variable, IntVar> vars = new HashMap<Variable, IntVar>();
	
	private List<Variable> variables;
	
	private IntVar[][] chocoVariables;
	
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

	private void initConstraints(List<Constraint> constraints) {
		for (Constraint constraint : constraints) {
			
			if (constraint.getScope().stream().anyMatch(MergedVariable.class::isInstance))
				continue;
			
			IntVar[] chocoScopeVars = new IntVar[constraint.getScope().size()];
			
			for (int i = 0; i < chocoScopeVars.length; i++) {
				Variable var = constraint.getScope().get(i);
				chocoScopeVars[i] = this.vars.get(var);
			}
			
			
			this.cspModel.sum(chocoScopeVars, "=", constraint.getEffectiveLabel()).post();
			
		}
	}
	
	private void insertMergedVariables(List<MergedVariable> mVars) {
		
		for (MergedVariable mergedVariable : mVars) {
			IntVar v1 = this.vars.get(mergedVariable.getVariables().get(0));
			IntVar[] vars = new IntVar[mergedVariable.getVariables().size()-1];
			for (int i = 1; i < mergedVariable.getVariables().size(); i++) {
				vars[i-1] = this.vars.get(mergedVariable.getVariables().get(i));
			}
			
			IntVar newMVar = v1.add(vars).intVar();
			
			// TODO Add the merged vars to a list and create new constraints with this ones. The new constraint has to be like the ones generated by aggregateConstraintsScopes
		}
		
	}

	private CSP initCSP() {
		this.variables = this.customInitVariables();
		List<Constraint> constraints = this.customInitConstraints(variables);
		constraints = this.reduceConstraintsScope(constraints);
		List<MergedVariable> mergedVars = this.aggregateConstraintsScopes(constraints);
		Collections.sort(constraints);
		
		
		this.initVariables(variables);
		
		this.initConstraints(constraints);
		
		this.insertMergedVariables(mergedVars);
				
		return new CSP("alfredino", variables, constraints);
	}
	
	
	private List<IntVar> getSolution(IntVar[] toCheckVariables) {
		Solver solver = this.cspModel.getSolver();
		
		List<Solution> solutions = new ArrayList<Solution>();
		
		solutions.add(new Solution(cspModel, toCheckVariables));
		
		Map<IntVar, Integer> realSolution = new HashMap<>();
		
		for (int i = 0; i < toCheckVariables.length; i++) {
			realSolution.putIfAbsent(toCheckVariables[i], null);
		}
			
		boolean checkEquality = true;
		int checksCounter = 0;
		
		for (int i = 0; i < Math.pow(2, realSolution.size()) ; i++) {
			solutions.add(new Solution(cspModel, toCheckVariables));
			solver.solve();
			solutions.get(i).record();
			
			checkEquality = true;
			checksCounter++;
			
			for (IntVar var : solutions.get(i).retrieveIntVars(true)) {
				if (!realSolution.containsKey(var))
					continue;

				if (i == 0) {
					realSolution.replace(var, var.getValue());
					continue;
				}
				
				if ( i > 0 && realSolution.containsKey(var) && realSolution.get(var).intValue() != var.getValue()) {
					realSolution.remove(var);
					checkEquality = false;
					checksCounter = 0;
				}
			}

			realSolution.keySet().stream().forEach(System.out::print);
			System.out.println("\t" + checksCounter + "\t" + checkEquality);
			System.out.println();
			
			if (checksCounter == 3 && checkEquality == true ) {
				solver.reset();
				try {
					// update delle variabili nel model dopo la solve (solo le var nella soluzione devono essere aggiornate, ci� viene fatto dal restore)
					solutions.get(i).restore();
				} catch (ContradictionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			}
			
			solver.reset();
		}
		
		System.out.println("Solution found!");
		
		List<IntVar> realSolutionsList = new ArrayList<IntVar>();
		
		if (!realSolution.isEmpty()) {
			realSolutionsList.addAll(realSolution.keySet().stream().collect(Collectors.toList()));
			this.updateVariables(realSolutionsList);
		}
		
		return realSolutionsList;
	}

	public void updateVariables(List<IntVar> variables) {
		for (IntVar intVar : variables) {
			Optional<Variable> var = this.vars.entrySet().stream()
														 .filter(entry -> intVar.equals(entry.getValue()))
														 .map(Map.Entry::getKey)
														 .findAny();
			
			if (!var.isPresent())
				continue;
			
			var.get().setAssignedValue(intVar.getValue());			
		}
	}

	public void initVariables(List<Variable> variables) {
		for (Variable variable : variables) {
			int r = variable.getSquare().getLocation().getRow();
			int c = variable.getSquare().getLocation().getColumn();
			String varName = "v(" + r + "," + c + ")";
			this.vars.put(variable, this.cspModel.intVar(varName, variable.getDomain()));
		}
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
		
		
		
		cspSolver.solveByStep();
		
		System.out.println("");
	}


	@Override
	public void solveByStep() {

		IntVar[] toCheckVariables = null;
		Set<Variable> coveredNeighborsOfFrontierVariables = new HashSet<Variable>();
		
		for (Variable var : VariableUtils.getInstance().getFrontierVariables(this.vars.keySet().stream().collect(Collectors.toList()))) {
			coveredNeighborsOfFrontierVariables.addAll(this.grid.getNeighboursAsStream(var.getSquare().getLocation())
																.filter(neigh -> neigh.getState() == SquareState.COVERED)
																.map(square -> VariableUtils.getInstance().getVariableFromSquare(square, variables))
																.collect(Collectors.toSet()));
		}
		
		toCheckVariables = new IntVar[coveredNeighborsOfFrontierVariables.size()];
		
		toCheckVariables = coveredNeighborsOfFrontierVariables.stream().map(covvar -> this.vars.get(covvar)).toArray(IntVar[]::new);
		
		List<IntVar> solution = this.getSolution(toCheckVariables);
		
		if (solution.isEmpty())
			return;

		List<Variable> varSol = new ArrayList<Variable>();
		
		for (IntVar intVar : solution) {
			Optional<Variable> var = this.vars.entrySet().stream()
														 .filter(entry -> intVar.equals(entry.getValue()))
														 .map(Map.Entry::getKey)
														 .findAny();
			
			if (!var.isPresent())
				continue;
			
			varSol.add(var.get());
		}
		
		varSol.stream().forEach(System.out::print);
		
		
	}


	@Override
	public void solveComplete() {
		// TODO Auto-generated method stub
	}


	@Override
	public void solveCompleteNTimes() {
		// TODO Auto-generated method stub
	}
}
