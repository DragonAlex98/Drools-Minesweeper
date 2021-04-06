package it.unicam.cs.csp_solver;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class SolverStatistics {

	private double totalSolvingTime = 0;
	
	private double averageSolvingTime = 0;
	
	private double winPercentage = 0;
	
	private int winNumber = 0;

	private int loseNumber = 0;

	private int numberOfRuns = 0;
	
	private int totalNumberOfRandomDecisions = 0;
	
	private double averageNumberOfRandomDecisions = 0;
	
	private int numberOfRandomDecisionsLeadingToLose = 0;
	
	private double percentageOfLoseCausedByRandomDecisions = 0;
	
	public void increaseTotalSolvingTime(double totalTime) {
		this.totalSolvingTime += totalTime;
	}
	
	public void increaseWin() {
		this.winNumber += 1;
	}
	
	public void increaseLose() {
		this.loseNumber += 1;
	}
	
	public void increaseRun() {
		this.numberOfRuns += 1;
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
		this.averageNumberOfRandomDecisions = (double) this.totalNumberOfRandomDecisions / this.numberOfRuns;
		this.percentageOfLoseCausedByRandomDecisions = (this.loseNumber == 0) ? 0 : (double) this.numberOfRandomDecisionsLeadingToLose / this.loseNumber;
	}
	
	@Override
	public String toString() {
		String s = "";
		s += String.format("Total Solving Time: %.2fs", this.totalSolvingTime) + System.lineSeparator();
		s += String.format("Average Solving Time: %.2fs", this.averageSolvingTime) + System.lineSeparator();
		s += System.lineSeparator();
		s += String.format("Win Percentage: %.2f%%", this.winPercentage * 100) + System.lineSeparator();
		s += "Wins: " + this.winNumber + System.lineSeparator();
		s += "Losses: " + this.loseNumber + System.lineSeparator();
		s += "Total Runs: " + this.numberOfRuns + System.lineSeparator();
		s += System.lineSeparator();
		s += "Total random decisions: " + this.totalNumberOfRandomDecisions + System.lineSeparator();
		s += String.format("Average random decisions: %.2f", this.averageNumberOfRandomDecisions) + System.lineSeparator();
		s += String.format("Loss by random decisions: %.2f%%", this.percentageOfLoseCausedByRandomDecisions * 100);
		return s;
	}
}
