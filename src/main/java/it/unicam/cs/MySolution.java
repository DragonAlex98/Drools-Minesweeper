package it.unicam.cs;

import java.util.Map;

import it.unicam.cs.model.Location;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(of = {"variables"})
@AllArgsConstructor
public class MySolution {
	private Map<Location, Integer> variables;
}
