package rdfgroundedstrings;

import inputmanagement.candidates.impl.RelationCandidate;

import java.util.List;

public interface RdfGroundedStringMapper {

	/**
	 * find relations based on the learned patterns in folder
	 * /Engine/rdf-grounded-strings/
	 * 
	 * @param text
	 * @return
	 */
	public List<RelationCandidate> findRelationCandidates(String text);

}
