package org.Evaluation;

import java.util.List;

public class EvaluationResult {

	public String query;
	private List<String> true_positives;
	private List<String> total_positives;
	private List<String> total_correct;
	private long elapsedTime;

	public EvaluationResult(List<String> true_positives,
			List<String> total_positives, 
			List<String> total_correct) {
		this.true_positives = true_positives;
		this.total_positives = total_positives;
		this.total_correct = total_correct;
	}


	public EvaluationResult(List<String> true_positives, List<String> results,
			long elapsedTime, List<String> correctAnswers) {
		this.true_positives = true_positives;
		this.total_positives = results;
		this.setElapsedTime(elapsedTime);
		this.total_correct = correctAnswers;
		
	}


	public double getPrecision() {
		return ((double) true_positives.size()) / ((double) total_positives.size());
	}

	public double getRecall() {
		return ((double) true_positives.size()) / ((double) total_correct.size());
	}

	public double getFMeasure() {
		double precision = getPrecision();
		double recall = getRecall();
		
		if (precision + recall > 0.0) {
			return 2 * precision * recall / (precision + recall);
		} else {
			return 0.0;
		}
	}


	public long getElapsedTime() {
		return elapsedTime;
	}


	public void setElapsedTime(long elapsedTime) {
		this.elapsedTime = elapsedTime;
	}

}
