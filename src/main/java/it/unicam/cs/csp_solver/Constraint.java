package it.unicam.cs.csp_solver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;

/**
 * A constraint is related to a variable.
 * 
 * It has a scope that comprises the neighbor variables of the one that is related to the constraint.
 * 
 * A constraint has also a map containing all the tuples of assignments that satisfy the constraint, and a map containing
 * the support tuples.
 * 
 * @author RICCARDO
 *
 */
@Getter
public class Constraint implements Comparable<Constraint> {
	
	private Variable variable;
	
	private List<Variable> scope;
	
	private Map<Assignment, Boolean> satisfying_tuples;
	
	private Map<Map<Variable, Integer>, Assignment> support_tuples;
	
	private Integer effectiveLabel;
	
	public Constraint(Variable variable, List<Variable> scope, Integer effectiveLabel) {
		this.variable = variable;
		this.scope = scope;
		this.satisfying_tuples = new HashMap<Assignment, Boolean>();
		this.support_tuples = new HashMap<Map<Variable,Integer>, Assignment>();
		this.effectiveLabel = effectiveLabel;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((scope == null) ? 0 : scope.hashCode());
		return result;
	}



	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Constraint other = (Constraint) obj;
		if (scope == null) {
			if (other.scope != null)
				return false;
		} else if (!scope.equals(other.scope))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		StringBuffer scope = new StringBuffer();
		
		scope.append("[");
		for (Variable variable : this.scope) {
			scope.append("(" + variable.getSquare().getLocation() + "), ");
		}
		scope.append("]");
		
		return "Constraint [variable= [" + variable.getSquare().getLocation() + ", value= " + variable.getAssignedValue() + "], scope=" + scope.toString() + "]";
	}

	@Override
	public int compareTo(Constraint o) {
		Integer scopeSize = this.scope.size();
		return scopeSize.compareTo(o.getScope().size());
	}
}
