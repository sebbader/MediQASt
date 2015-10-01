package naivemapper;

import inputmanagement.impl.GenerateSparqlException;
import inputmanagement.impl.QueryTriple;

import java.util.ArrayList;
import java.util.List;

/**
 * Used for the Brute Force approach
 * 
 * @author Sebastian Bader (sebastian.bader@student.kit.edu)
 *
 */
public interface NaiveMapper {

	/**
	 * find query triples by partitioning the question
	 * 
	 * @param question
	 * @return
	 * @throws GenerateSparqlException
	 */
	public List<ArrayList<QueryTriple>> getQueryTriples(String question)
			throws GenerateSparqlException;

}
