package rdf;

public interface Triple {

	public String getSubject();

	public void setSubject(String uri);

	public String getPredicate();

	public void setPredicate(String uri);

	public String getObject();

	public void setObjects(String uri);
}
