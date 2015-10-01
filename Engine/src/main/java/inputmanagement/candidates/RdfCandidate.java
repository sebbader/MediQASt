package inputmanagement.candidates;

import java.util.List;

public interface RdfCandidate extends Candidate {

	/**
	 * get the position of the candidate in the entry text
	 * 
	 * @return
	 */
	public List<Integer> getPosition();

	public void setPosition(List<Integer> position);

	/**
	 * 
	 * @return the type of the rdf candidate object
	 */
	public RdfCandidateTypes getType();
}
