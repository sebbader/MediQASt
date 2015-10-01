package mapper;

import inputmanagement.candidates.impl.EntityCandidate;
import inputmanagement.impl.GenerateSparqlException;

import java.util.List;

public interface EntityMapper {

	/**
	 * executes all implemented approaches to find entities
	 * 
	 * @param entity
	 * @return
	 */
	public List<EntityCandidate> getEntityCandidates(String entity);

}
