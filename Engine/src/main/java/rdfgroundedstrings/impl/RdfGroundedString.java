package rdfgroundedstrings.impl;

import inputmanagement.candidates.impl.RelationCandidate;

import java.util.List;

public class RdfGroundedString {

	private String name;
	private String pattern;
	private List<RelationCandidate> relations;

	public RdfGroundedString(String name, String pattern,
			List<RelationCandidate> relations) {
		this.setName(name);
		this.setPattern(pattern);
		this.setRelations(relations);
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public List<RelationCandidate> getRelations() {
		return relations;
	}

	public void setRelations(List<RelationCandidate> relations) {
		this.relations = relations;
	}

	public void setScores(double score) {
		this.relations.forEach(relationCandidate -> relationCandidate
				.setScore(score));
	}

	@Override
	public String toString() {
		return "Pattern: '" + this.pattern + "' implies " + this.relations;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
