package rdfgroundedstrings.test;

import inputmanagement.impl.GenerateSparqlException;
import inputmanagement.impl.QueryTriple;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import rdfgroundedstrings.RdfGroundedStringAnalyzer;
import rdfgroundedstrings.impl.RdfGroundedStringAnalyzerImpl;

public class RdfGroundedStringAnalyzerTest {

	private RdfGroundedStringAnalyzer analyzer = new RdfGroundedStringAnalyzerImpl();

	@Test
	public void getQueryTriplesTest() throws GenerateSparqlException {
		// String text =
		// "Barack Obama is the first black president of the United States of America";
		String text = "Who is the first black president of the United States of America?";

		List<ArrayList<QueryTriple>> queryTriples = analyzer
				.getQueryTriples(text);

		System.out.println("Found QueryTriples:");
		for (List<QueryTriple> queryTripleSet : queryTriples) {

			System.out.println("");
			System.out.println("Query Triple Set:");
			for (QueryTriple queryTriple : queryTripleSet) {
				System.out.println(queryTriple);
			}

		}

	}

}
