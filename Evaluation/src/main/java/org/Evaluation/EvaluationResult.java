package org.Evaluation;

import java.util.List;

public class EvaluationResult {

	private List<String> true_positives;
	private List<String> total_positives;
	private List<String> total_correct;

	public EvaluationResult(List<String> true_positives,
			List<String> total_positives, 
			List<String> total_correct) {
		this.true_positives = true_positives;
		this.total_positives = total_positives;
		this.total_correct = total_correct;
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
		return 2 * precision * recall / (precision + recall);
	}

}
