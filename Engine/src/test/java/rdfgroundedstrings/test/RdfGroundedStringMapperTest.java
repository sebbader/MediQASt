package rdfgroundedstrings.test;

import inputmanagement.candidates.impl.RelationCandidate;
import inputmanagement.impl.GenerateSparqlException;
import inputmanagement.impl.QueryTriple;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.junit.Test;

import configuration.ConfigManager;
import configuration.impl.ConfigManagerImpl;
import rdfgroundedstrings.RdfGroundedStringAnalyzer;
import rdfgroundedstrings.RdfGroundedStringMapper;
import rdfgroundedstrings.impl.RdfGroundedStringAnalyzerImpl;
import rdfgroundedstrings.impl.RdfGroundedStringMapperImpl;

public class RdfGroundedStringMapperTest {

	/*
	 * @Test public void findRelationTest() { String text =
	 * "are side effects of";
	 * 
	 * RdfGroundedStringMapper analyzer = new RdfGroundedStringMapperImpl();
	 * 
	 * List<RelationCandidate> relationCandidates =
	 * analyzer.findRelationCandidates(text);
	 * 
	 * System.out.println();
	 * System.out.println("Found rdf-grounded-strings for '" + text + "':"); for
	 * (RelationCandidate relationCandidate : relationCandidates) {
	 * System.out.println(relationCandidate); } }
	 */

	@Test
	public void completeTest() throws GenerateSparqlException {
		ConfigManager configManager = new ConfigManagerImpl();
		configManager.getLogger().setLevel(Level.DEBUG);

		String text = "Who is the first black president of the United States of America?";

		RdfGroundedStringAnalyzer analyzer = new RdfGroundedStringAnalyzerImpl();
		List<ArrayList<QueryTriple>> queryTripleSets = analyzer
				.getQueryTriples(text);

		System.out.println();
		System.out.println("Found QueryTriples for '" + text + "':");
		for (ArrayList<QueryTriple> queryTriples : queryTripleSets) {
			System.out.println();
			System.out.println("Found Set of QueryTriples for '" + text + "':");
			for (QueryTriple triple : queryTriples) {
				System.out.println(triple.toString());
			}
		}

		System.out.println();
		System.out.println("Map predicates using RdfGroundedStrings:");
		RdfGroundedStringMapper mapper = new RdfGroundedStringMapperImpl();
		for (ArrayList<QueryTriple> queryTriples : queryTripleSets) {
			System.out.println();
			for (QueryTriple triple : queryTriples) {
				List<RelationCandidate> relationCandidates = mapper
						.findRelationCandidates(triple.getPredicate());
				triple.addRelationCandidates(relationCandidates);

				if (relationCandidates != null) {
					System.out.println("For predicate of triple '"
							+ triple.toString() + "':");
					for (RelationCandidate relationCandidate : relationCandidates) {
						System.out.println(relationCandidate.toString());
					}
				}

			}
		}

	}

}
