package it.unicam.cs.csp_solver;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
public class SolverStatistics {
	private static SolverStatistics instance = null;
	
	private long totalSolvingTime = 0;
	
	private double averageSolvingTime = 0;
	
	private double winPercentage = 0;

	private double losePercentage = 0;
	
	private double winLoseRate = 0;
	
	private int winNumber = 0;
	
	private int loseNumber = 0;
	
	@Setter
	private int numberOfRuns = 0;
	
	private int totalNumberOfRandomDecisions = 0;
	
	private double averageNumberOfRandomDecisions = 0;
	
	private int numberOfRandomDecisionsLeadingToLose = 0;
	
	private double percentageOfLoseCausedByRandomDecisions = 0; 
	
	private SolverStatistics() { }
	
	public static SolverStatistics getInstance() {
		if (instance == null)
			instance = new SolverStatistics();
		
		return instance;
	}
	
	public void clear() {
		instance = new SolverStatistics();
	}
	
	public void increaseTotalSolvingTime(long totalTime) {
		this.totalSolvingTime += totalTime;
	}
	
	public void increaseWin() {
		this.winNumber += 1;
	}
	
	public void increaseLose() {
		this.loseNumber += 1;
	}
	
	public void increaseTotalNumberOfRandomDecisions() {
		this.totalNumberOfRandomDecisions += 1;
	}
	
	public void increaseNumberOfRandomDecisionsLeadingToLose() {
		this.numberOfRandomDecisionsLeadingToLose += 1;
	}
	
	public void consolidate() {
		this.averageSolvingTime = this.totalSolvingTime / this.numberOfRuns;
		this.winPercentage = this.winNumber / this.numberOfRuns;
		this.losePercentage = 1 - this.winPercentage;
		this.winLoseRate = this.winNumber / this.loseNumber;
		this.averageNumberOfRandomDecisions = this.totalNumberOfRandomDecisions / this.numberOfRuns;
		this.percentageOfLoseCausedByRandomDecisions = this.numberOfRandomDecisionsLeadingToLose / this.loseNumber;
	}
}
