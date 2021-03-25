package it.unicam.cs.csp_solver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import it.unicam.cs.model.Square;
import lombok.Getter;
import lombok.Setter;

/**
 * A variable is represented by the associated square and can have a domain of 0, 1 or [0, 1].
 * A current domain is used to represent the actual domain of the variable, it is used during the propagation.
 * 
 * @author RICCARDO
 *
 */
@Getter
public class Variable {
	
	private Square square;
	
	private List<Boolean> currentDomain;
	
	private List<Integer> domain;
	
	@Setter
	private Integer assignedValue = null;
	
	public Variable(Square square, Integer ... domain) {
		this.square = square;
		this.domain = Arrays.asList(domain);
		this.currentDomain = new ArrayList<Boolean>();
		this.currentDomain.addAll(this.domain.stream().map(element -> true).collect(Collectors.toList()));
	}

	@Override
	public String toString() {
		return "Variable [name= Square(" + this.square.getLocation().getRow() + ", " + this.square.getLocation().getColumn() + "); domain= " + this.domain + "; currentDomain= " + this.currentDomain + "; assigned value= " + this.assignedValue + "]";
	}
}
