package inputmanagement;

import inputmanagement.candidates.impl.EntityCandidate;
import inputmanagement.candidates.impl.RelationCandidate;
import inputmanagement.candidates.impl.SparqlCandidate;
import inputmanagement.impl.GenerateSparqlException;
import inputmanagement.impl.QueryTriple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface SparqlGenerator {

	public void setNumberOfCandidates(int option);

	public List<SparqlCandidate> getSparqlCanidates(
			List<EntityCandidate> entity1Candidates,
			List<EntityCandidate> entity2Candidates,
			List<RelationCandidate> relationCandidates);

	public List<SparqlCandidate> getSparqlCanidates(
			List<QueryTriple> queryTriples) throws GenerateSparqlException;

	public List<SparqlCandidate> getSparqlCanidates(
			List<QueryTriple> queryTriples, int numberOfTriplesPerSparql);

	public List<SparqlCandidate> getSparqlCanidatesForQueryTripleSet(
			List<ArrayList<QueryTriple>> queryTriplesSet)
			throws GenerateSparqlException;

}
