package inputmanagement.candidates.impl;

import inputmanagement.candidates.Candidate;

public class SparqlLineCandidate implements Candidate {

	private String entity1;
	private String relation;
	private String entity2;

	private double score;

	public SparqlLineCandidate(EntityCandidate entity1,
			RelationCandidate relation, EntityCandidate entity2) {
		this.entity1 = entity1.getText();
		this.relation = relation.getText();
		this.entity2 = entity2.getText();
		this.score = entity1.getScore() + relation.getScore()
				+ entity2.getScore();
	}

	public void setEntity1(String entity1) {
		this.entity1 = entity1;
	}

	public String getEntity1() {
		return this.entity1;
	}

	public void setRelation(String relation) {
		this.relation = relation;
	}

	public String getRelation() {
		return this.relation;
	}

	public void setEntity2(String entity2) {
		this.entity2 = entity2;
	}

	public String getEntity2() {
		return this.entity2;
	}

	public void setScore(double score) {
		this.score = score;
	}

	@Override
	public double getScore() {
		return score;
	}

	@Override
	public String getText() {
		return "{" + entity1 + " " + relation + " " + entity2 + " . }"
				+ " union " + "{" + entity2 + " " + relation + " " + entity1
				+ " . }";
	}

	@Override
	public String toString() {
		return getText();

	}

}
