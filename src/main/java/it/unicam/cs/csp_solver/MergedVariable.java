package it.unicam.cs.csp_solver;

import java.util.List;
import java.util.stream.IntStream;

import lombok.Getter;

@Getter
public class MergedVariable extends Variable {
	
	private List<Variable> variables;

	public MergedVariable(List<Variable> variables) {
		// Create the domain of the merged var.
		// The domain is an array from 0 to the number of vars. EX: V3 = V1, V2 --> [0, 1, 2]
		super(null, IntStream.rangeClosed(0, variables.size()).boxed().toArray(Integer[]::new));
		
		this.variables = variables;
	}
	
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((variables == null) ? 0 : variables.hashCode());
		return result;
	}



	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		MergedVariable other = (MergedVariable) obj;
		if (variables == null) {
			if (other.variables != null)
				return false;
		} else if (variables.stream().filter(var -> !other.variables.contains(var)).count() > 0)
			return false;
		return true;
	}



	@Override
	public String toString() {
		StringBuffer sBuffer = new StringBuffer();
		
		sBuffer.append("MergedVariable = [");
		sBuffer.append("Variables = [");
		for (Variable var : this.variables) {
			sBuffer.append("{ " + var + "}, ");
		}
		sBuffer.append("]");
		sBuffer.append("; domain= " + super.getDomain() + "; currentDomain= " + super.getCurrentDomain() + "; assigned value= " + super.getAssignedValue() + "]");
		
		return sBuffer.toString();
	}

}
