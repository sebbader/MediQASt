package mapper;

import inputmanagement.candidates.impl.EntityCandidate;
import inputmanagement.candidates.impl.RelationCandidate;
import inputmanagement.impl.GenerateSparqlException;

import java.util.List;

public interface RelationMapper {

	public List<RelationCandidate> getRelationCandidates(String relation);

	/**
	 * executes all implemented approaches to find relations
	 * 
	 * @param relation
	 * @param entity1Candidates
	 * @param entity2Candidates
	 * @return List of possible relations
	 * @throws GenerateSparqlException
	 */
	public List<RelationCandidate> findRelationCandidates(String relation,
			List<EntityCandidate> entity1Candidates,
			List<EntityCandidate> entity2Candidates)
			throws GenerateSparqlException;

	/**
	 * finds relations by comparing their labels with the input term
	 * 
	 * @param relation
	 * @return List of possible relations
	 */
	public List<RelationCandidate> findRelationCandidatesWithLucene(
			String relation);

	/**
	 * Uses the relation next to the entity candidates to find relations.
	 * 
	 * @param entityCandidates
	 * @param relation
	 * @return List of possible relations
	 */
	public List<RelationCandidate> findRelationCandidatesFromEntityEnvironment(
			List<EntityCandidate> entityCandidates, String relation);
}
