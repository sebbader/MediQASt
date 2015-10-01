package rdf.impl;

import java.util.ArrayList;
import java.util.List;

import rdf.Triple;

public class RDFPath {

	private List<Triple> path = new ArrayList<Triple>();

	private enum status {
		subject, predicate, object
	};

	public RDFPath(List<Triple> path) {
		this.path = path;
	}

	public RDFPath(String[] field) {
		String subject = "";
		String predicate = "";
		String object = "";

		status s = status.subject;
		for (String element : field) {
			switch (s) {
			case subject:
				subject = element;
				s = status.predicate;
				break;
			case predicate:
				predicate = element;
				s = status.object;
			case object:
				object = element;
				path.add(new TripleImpl(subject, predicate, object));
				subject = element;
				s = status.predicate;
			}
		}
	}

	public List<Triple> getPath() {
		return path;
	}
}
