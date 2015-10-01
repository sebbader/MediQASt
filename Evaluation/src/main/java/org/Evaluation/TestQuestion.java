package org.Evaluation;

import java.util.List;

import arq.query;
import arq.sparql;

public class TestQuestion {
	
	private String naturalQuestion;
	private List<String> correctAnswers;
	private String correctSparql;
	
	public TestQuestion(String naturalQuestion, List<String> correctAnswers, String correctSparql) {
		this.naturalQuestion = naturalQuestion;
		this.correctAnswers = correctAnswers;
		this.correctSparql = correctSparql;
	}
	
	public String getNaturalQuestion() {
		return naturalQuestion;
	}
	public void setNaturalQuestion(String naturalQuestion) {
		this.naturalQuestion = naturalQuestion;
	}
	public List<String> getCorrectAnswers() {
		return correctAnswers;
	}
	public void setCorrectAnswers(List<String> correctAnswers) {
		this.correctAnswers = correctAnswers;
	}
	public String getCorrectSparql() {
		return correctSparql;
	}
	public void setCorrectSparql(String correctSparql) {
		this.correctSparql = correctSparql;
	}
	
	@Override
	public String toString() {
		String output = naturalQuestion + " | " + correctSparql + " | ";
		for (String answer : correctAnswers) {
			output += answer + "; ";
		}
		
		return output;
	}
}
