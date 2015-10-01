package inputmanagement.candidates.impl;

import inputmanagement.candidates.RdfCandidate;
import inputmanagement.candidates.RdfCandidateTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rdf.impl.RDFPath;

public class EntityCandidate implements RdfCandidate {

	private String uri;
	private double score;
	private String label;
	private RdfCandidateTypes type;
	private List<RDFPath> environment; // TODO
	private List<Integer> position;

	public EntityCandidate(String uri, double score, String label) {
		this.setUri(uri);
		this.setScore(score);
		this.setLabel(label);
		this.setType(RdfCandidateTypes.ENTITY);
	}

	public EntityCandidate(String fragment, double score) {

		this.setScore(score);

		fragment = fragment.replace("<B>", "").replace("</B>", "");
		Pattern pattern = Pattern.compile("http.*");
		Matcher matcher = pattern.matcher(fragment);
		if (matcher.find()) {
			fragment = matcher.group(0);
		}

		pattern = Pattern.compile("http.*>");
		matcher = pattern.matcher(fragment);
		if (matcher.find()) {
			fragment = matcher.group(0);
			fragment = fragment.substring(0, fragment.length() - 1);
		}

		this.setUri(fragment);
		this.setType(RdfCandidateTypes.ENTITY);
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public String getUri() {
		return uri;
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

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getText() {
		return uri;
	}

	public List<RDFPath> getEnvironment() {
		return environment;
	}

	public void setEnvironment(List<RDFPath> environment) {
		this.environment = environment;
	}

	@Override
	public RdfCandidateTypes getType() {
		return this.type;
	}

	public void setType(RdfCandidateTypes type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "[ " + uri + " | " + label + " | " + score + " ]";
	}

	@Override
	public List<Integer> getPosition() {
		if (this.position != null) {
			return this.position;
		} else {
			return new ArrayList<Integer>();
		}
	}

	@Override
	public void setPosition(List<Integer> position) {
		this.position = position;
	}
}
