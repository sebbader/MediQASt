package inputmanagement.candidates.impl;

import java.util.List;

import inputmanagement.candidates.RdfCandidate;
import inputmanagement.candidates.RdfCandidateTypes;

public class GeneralCandidate implements RdfCandidate {

	private double score;
	private String uri;
	private String label;
	private RdfCandidateTypes type;

	public GeneralCandidate(String uri, double score, String label) {
		this.setUri(uri);
		this.setScore(score);
		this.setLabel(label);
		this.setType(RdfCandidateTypes.UNKNOWN);
	}

	@Override
	public RdfCandidateTypes getType() {
		return this.type;
	}

	public void setType(RdfCandidateTypes type) {
		this.type = type;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public double getScore() {
		return score;
	}

	public void setUri(String uri) {
		if (!uri.contains("?")) {
			if (!uri.startsWith("<"))
				uri = "<" + uri;
			if (!uri.endsWith(">"))
				uri = uri + ">";
		}

		this.uri = uri;
	}

	public String getUri() {
		return uri;
	}

	public String getText() {
		return uri;
	}

	@Override
	public List<Integer> getPosition() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPosition(List<Integer> position) {
		// TODO Auto-generated method stub

	}

}
