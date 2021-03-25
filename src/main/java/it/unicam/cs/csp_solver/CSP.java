package it.unicam.cs.csp_solver;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

/**
 * This is the CSP containing all the variables and all the constraints on the variables
 * 
 * @author RICCARDO
 *
 */
@Getter
public class CSP {
	private String name;
	
	private List<Variable> variables;
	
	private List<Constraint> constraint;
	
	public CSP(String name, List<Variable> variables) {
		this.name = name;
		this.variables = variables;
		this.constraint = new ArrayList<Constraint>();
	}
	
	public CSP(String name, List<Variable> variables, List<Constraint> constraint) {
		this.name = name;
		this.variables = variables;
		this.constraint = constraint;
	}
	
	/**
	 * Add a constraint to the csp
	 * 
	 * @param constraint
	 */
	public void add_constraint(Constraint constraint) {
		this.constraint.add(constraint);
	}
	
	/**
	 * Add a list of constraints to the csp.
	 * 
	 * @param constraint
	 */
	public void add_constraints(List<Constraint> constraint) {
		this.constraint.addAll(constraint);
	}
}
