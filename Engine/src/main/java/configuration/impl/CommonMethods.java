package configuration.impl;

import inputmanagement.InputManager;
import inputmanagement.candidates.Candidate;
import inputmanagement.candidates.RdfCandidate;
import inputmanagement.impl.InputManagerImpl;
import inputmanagement.impl.QueryTriple;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.query.Query;
import com.sun.media.jfxmedia.logging.Logger;

import connector.SPARQLEndpointConnector;
import connector.impl.SPARQLEndpointConnectorImpl;

public class CommonMethods {

	/**
	 * removes duplicates from a list of Candidates the remaining Candidate
	 * object has the highest score
	 * 
	 * @param candidates
	 * @return
	 */
	public static List<? extends RdfCandidate> removeDuplicates(
			List<? extends RdfCandidate> candidates) {
		List<RdfCandidate> unique_candidates = new ArrayList<RdfCandidate>();

		if (candidates == null)
			return unique_candidates;

		for (RdfCandidate candidate1 : candidates) {
			boolean isDuplicate = false;
//			if (candidate1.getText().equalsIgnoreCase("<http://wifo5-04.informatik.uni-mannheim.de/sider/resource/side_effects/C0002622>")) {
//				int i = 1 + 1;
//			}
			for (RdfCandidate candidate2 : unique_candidates) {
				if (candidate1.getText().equalsIgnoreCase(candidate2.getText())) {
					
					List<Integer> can1Positions = candidate1.getPosition();
					List<Integer> can2Positions = candidate2.getPosition();
					List<Integer> overlap = new ArrayList<Integer>();
					for (int pos : can1Positions) {
						if (can2Positions.contains(pos)) {
							overlap.add(pos);
						}
					}
					if (candidate1.getScore() > candidate2.getScore()) {
						candidate2.setScore(candidate1.getScore());
						candidate2.setPosition(can1Positions);
						
					} else if(!overlap.isEmpty() && (candidate1.getScore() >= candidate2.getScore())) {
						candidate2.setPosition(can1Positions);
					}
					
					
					
					isDuplicate = true;
				}
			}
			if (!isDuplicate && candidate1 != null) {
				unique_candidates.add(candidate1);
			}
		}
		return unique_candidates;
	}

	/**
	 * removes duplicate entries in the list. Kepps only the ones with the
	 * highest score. Attention: does not regard candidate lists!
	 * 
	 * @param queryTriples
	 * @return a list of unique query triples
	 */
	public static List<QueryTriple> removeTripleDuplicates(
			List<QueryTriple> queryTriples) {
		List<QueryTriple> unique_queryTriples = new ArrayList<QueryTriple>();

		if (queryTriples == null)
			return unique_queryTriples;

		for (QueryTriple triple1 : queryTriples) {
			boolean isDuplicate = false;
			for (QueryTriple triple2 : unique_queryTriples) {
				if (triple1.getTripleWithCandidates().equalsIgnoreCase(
						triple2.getTripleWithCandidates())) {
					if (triple1.getScore() > triple2.getScore())
						triple2.setScore(triple1.getScore());
					isDuplicate = true;
				}
			}
			if (!isDuplicate && triple1 != null) {
				unique_queryTriples.add(triple1);
			}
		}
		return unique_queryTriples;
	}

	public static boolean isVariable(String text) {
		text = text.toLowerCase();

		if (text.matches(".*(\\?).+"))
			return true;
		if (text.matches(".*(who).*"))
			return true;
		if (text.matches(".*how.*(many|tall|long).*"))
			return true;
		if (text.matches(".*(when|how often|since when|how long|for how long).*"))
			return true;
		if (text.matches(".*(what|which|where).*"))
			return true;

		return false;
	}

	/**
	 * many found candidates are too far away from each other. Therefore a query
	 * combining these entities must result in a empty set. By removing such candidates
	 * reasonable queries shall achieve a position in the SPARQL queries list.
	 * @param candidates
	 * @return only candidates that are actually connected to one other candidate
	 */
	public static List<RdfCandidate> filterCandidates(
			List<RdfCandidate> candidates) {
		List<RdfCandidate> tmp_candidates = new ArrayList<RdfCandidate>(
				candidates);

		for (RdfCandidate candidate : tmp_candidates) {
			String uri1 = candidate.getText();
			if (uri1.startsWith("?"))
				continue;

			boolean candidateIsConnected = false;

			for (RdfCandidate other_candidate : tmp_candidates) {

				String uri2 = other_candidate.getText();

				if (uri1.equalsIgnoreCase(uri2))
					continue;
				if (uri2.startsWith("?"))
					continue;

				String pattern = "{ "
						+ uri1
						+ " "
						+ uri2
						+ " ?x . }\n"
						+ "union\n"
						+ "{ ?x "
						+ uri2
						+ " "
						+ uri1
						+ " . }\n\n"

						+ "union\n"
						+ "{ "
						+ uri2
						+ " "
						+ uri1
						+ " ?x . }\n"
						+ "union\n"
						+ "{ ?x "
						+ uri1
						+ " "
						+ uri2
						+ " . }\n\n"

						+ "union\n"
						+ "{ "
						+ uri1
						+ " ?x "
						+ uri2
						+ " . }\n"
						+ "union\n"
						+ "{ "
						+ uri2
						+ " ?x "
						+ uri1
						+ " . }\n\n"

						+ "union\n"
						+ "{ "
						+ uri1
						+ " ?x ?y .  ?y "
						+ uri2
						+ " ?z . }\n"
						+ "union\n"
						+ "{ "
						+ uri1
						+ " ?x ?y .  ?z "
						+ uri2
						+ " ?y . }\n"
						+ "union\n"
						+ "{ "
						+ uri1
						+ " ?x ?y .  ?y ?z "
						+ uri2
						+ " . }\n"
						+ "union\n"
						+ "{ "
						+ uri1
						+ " ?x ?y .  "
						+ uri2
						+ " ?z ?y .  "
						+ "FILTER ( ?x != <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ) . }\n\n"

						+ "union\n"
						+ "{ ?x "
						+ uri1
						+ " ?y .  ?y "
						+ uri2
						+ " ?z . }\n"
						+ "union\n"
						+ "{ ?x "
						+ uri1
						+ " ?y .  ?z "
						+ uri2
						+ " ?y . }\n"
						+ "union\n"
						+ "{ ?x "
						+ uri1
						+ " ?y .  ?y ?z "
						+ uri2
						+ " . }\n"
						+ "union\n"
						+ "{ ?x "
						+ uri1
						+ " ?y .  "
						+ uri2
						+ " ?z ?y . }\n\n"

						+ "union\n"
						+ "{ ?y "
						+ uri1
						+ " ?x .  ?y "
						+ uri2
						+ " ?z . }\n"
						+ "union\n"
						+ "{ ?y "
						+ uri1
						+ " ?x .  ?z "
						+ uri2
						+ " ?y . }\n"
						+ "union\n"
						+ "{ ?y "
						+ uri1
						+ " ?x .  ?y ?z "
						+ uri2
						+ " . }\n"
						+ "union\n"
						+ "{ ?y "
						+ uri1
						+ " ?x .  "
						+ uri2
						+ " ?z ?y . }\n\n"

						+ "union\n"
						+ "{ ?y ?x "
						+ uri1
						+ " .  ?y "
						+ uri2
						+ " ?z . }\n"
						+ "union\n"
						+ "{ ?y ?x "
						+ uri1
						+ " .  ?z "
						+ uri2
						+ " ?y . }\n"
						+ "union\n"
						+ "{ ?y ?x "
						+ uri1
						+ " .  ?y ?z "
						+ uri2
						+ " . "
						+ "FILTER ( ?x != <http://dbpedia.org/ontology/wikiPageDisambiguates> ) . }\n"
						+ "union\n" + "{ ?y ?x " + uri1 + " .  " + uri2
						+ " ?z ?y . }\n\n";

				SPARQLEndpointConnector connector = InputManagerImpl.endpointConnector;
				candidateIsConnected = candidateIsConnected
						|| connector.executeAsk(pattern);
				if (candidateIsConnected) {
					break;
				}
			}

			if (!candidateIsConnected) {
				candidates.remove(candidate);
			}
			candidateIsConnected = false;
		}

		if (containsNoEntities(candidates)) {
			// filter is useless
			return tmp_candidates;
		} else {
			return candidates;
		}
	}

	private static boolean containsNoEntities(List<RdfCandidate> candidates) {
		for (RdfCandidate candidate : candidates) {
			if (candidate == null)
				continue;
			if (!candidate.getText().startsWith("?"))
				return false;
		}

		return true;
	}

	public static double getTriplesScore(List<QueryTriple> triples) {
		if (triples.isEmpty()) return 0.0;
		
		double score = 0.0;
		for (QueryTriple triple : triples) {
			score += triple.getScore();
		}
		
		double final_score = score / ((double) triples.size());
		return final_score;
	}
}
