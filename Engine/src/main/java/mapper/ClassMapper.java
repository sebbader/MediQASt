package mapper;

import inputmanagement.candidates.impl.EntityCandidate;
import inputmanagement.impl.GenerateSparqlException;

import java.util.List;

public interface ClassMapper {

	/**
	 * executes all implemented approaches to find classes
	 * 
	 * @param class_term
	 * @return
	 */
	public List<EntityCandidate> getClassCandidates(String class_term);

	/**
	 * finds classes by comparing their labels with the input term only checks
	 * the class index
	 * 
	 * @param class_term
	 * @return
	 */
	public List<EntityCandidate> getClassCandidatesWithLucene(String class_term);

}
