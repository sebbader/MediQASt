package inputmanagement.candidates.impl;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

import inputmanagement.candidates.Candidate;
import inputmanagement.impl.InputManagerImpl;

public class SparqlCandidate implements Candidate {
	private double score;
	private String sparqlQuery;
	private List<String> solutions;

	public SparqlCandidate(String query, double score) {
		this.score = score;
		this.sparqlQuery = query;
	}

	public String getSparqlQuery() {
		return sparqlQuery;
	}

	public void setSparqlQuery(String sparqlQuery) {
		this.sparqlQuery = sparqlQuery;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public List<String> getSolutions() {
		return solutions;
	}

	public void setSolutions(List<String> solutions) {
		this.solutions = solutions;
	}

	public void addSolution(String solution) {
		if (this.solutions == null)
			this.solutions = new ArrayList<String>();
		this.solutions.add(solution);
	}

	public void addSolution(List<String> result) {
		if (this.solutions == null)
			this.solutions = new ArrayList<String>();
		this.solutions.addAll(result);
	}

	public String getText() {
		return sparqlQuery;
	}

	public String toString() {
		return "[ " + sparqlQuery + " | " + score + " ]";
	}
}
