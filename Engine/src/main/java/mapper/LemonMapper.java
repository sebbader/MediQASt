package mapper;

import inputmanagement.candidates.Candidate;
import inputmanagement.candidates.impl.EntityCandidate;
import inputmanagement.candidates.impl.RelationCandidate;

import java.util.List;

public interface LemonMapper {

	public List<RelationCandidate> getRelationCandidates();

	public List<EntityCandidate> getEntityCandidates();

	public List<Candidate> getCandidates();

	public void getCandidates(List<EntityCandidate> entities,
			List<RelationCandidate> relations);

}
