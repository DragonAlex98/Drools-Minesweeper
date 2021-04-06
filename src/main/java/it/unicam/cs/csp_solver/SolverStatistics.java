package it.unicam.cs.csp_solver;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
public class SolverStatistics {

	private double totalSolvingTime = 0;
	
	private double averageSolvingTime = 0;
	
	private double winPercentage = 0;

	private double losePercentage = 0;
	
	@Setter
	private int winNumber = 0;
	
	@Setter
	private int loseNumber = 0;
	
	@Setter
	private int numberOfRuns = 0;
	
	private int totalNumberOfRandomDecisions = 0;
	
	private double averageNumberOfRandomDecisions = 0;
	
	private int numberOfRandomDecisionsLeadingToLose = 0;
	
	private double percentageOfLoseCausedByRandomDecisions = 0; 
	
	public SolverStatistics() { }
	
	public void increaseTotalSolvingTime(double totalTime) {
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
		this.winPercentage = (double) this.winNumber / this.numberOfRuns;
		this.losePercentage = 1 - this.winPercentage;
		this.averageNumberOfRandomDecisions = (double) this.totalNumberOfRandomDecisions / this.numberOfRuns;
		this.percentageOfLoseCausedByRandomDecisions = (this.loseNumber == 0) ? 0 : (double) this.numberOfRandomDecisionsLeadingToLose / this.loseNumber;
	}
}
