package rdf.impl;

import rdf.Triple;

public class TripleImpl implements Triple {

	private String subject;
	private String predicate;
	private String object;

	public TripleImpl(String subject, String predicate, String object) {
		setSubject(subject);
		setPredicate(predicate);
		setObjects(object);
	}

	public String getSubject() {
		// TODO Auto-generated method stub
		return subject;
	}

	public void setSubject(String uri) {
		// TODO Auto-generated method stub
		this.subject = uri;
	}

	public String getPredicate() {
		// TODO Auto-generated method stub
		return predicate;
	}

	public void setPredicate(String uri) {
		// TODO Auto-generated method stub
		this.predicate = uri;
	}

	public String getObject() {
		// TODO Auto-generated method stub
		return object;
	}

	public void setObjects(String uri) {
		// TODO Auto-generated method stub
		this.object = uri;
	}

}
