package rdfgroundedstrings;

import inputmanagement.impl.GenerateSparqlException;
import inputmanagement.impl.QueryTriple;

import java.util.ArrayList;
import java.util.List;

public interface RdfGroundedStringAnalyzer {

	public List<ArrayList<QueryTriple>> getQueryTriples(String query)
			throws GenerateSparqlException;
}
