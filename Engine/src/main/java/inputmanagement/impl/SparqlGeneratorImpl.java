package inputmanagement.impl;

import inputmanagement.InputManager;
import inputmanagement.SparqlGenerator;
import inputmanagement.candidates.Candidate;
import inputmanagement.candidates.impl.EntityCandidate;
import inputmanagement.candidates.impl.RelationCandidate;
import inputmanagement.candidates.impl.SparqlCandidate;
import inputmanagement.candidates.impl.SparqlLineCandidate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import configuration.ConfigManager;
import configuration.impl.ConfigManagerImpl;

public class SparqlGeneratorImpl implements SparqlGenerator {

	private Logger logger;
	private InputManager inputManager;
	private int limit = 10;
	private int topN = 10;
	private List<SparqlCandidate> sparqlCandidates = new ArrayList<SparqlCandidate>();

	public SparqlGeneratorImpl(InputManager inputManager) {
		ConfigManager configManager = new ConfigManagerImpl();
		this.logger = configManager.getLogger();

		this.inputManager = inputManager;
	}

	public int getNumberOfCandidates() {
		return topN;
	}

	public void setNumberOfCandidates(int topN) {
		this.topN = topN;
	}

	public List<SparqlCandidate> getSparqlCanidates(
			List<EntityCandidate> entity1Candidates,
			List<EntityCandidate> entity2Candidates,
			List<RelationCandidate> relationCandidates) {

		// ------------- default approach ------------------//
		// simple combination of entities and relations to triple queries
		for (EntityCandidate entity1 : entity1Candidates) {
			for (EntityCandidate entity2 : entity2Candidates) {
				for (RelationCandidate rel : relationCandidates) {

					String sparql = "SELECT * WHERE {" + " GRAPH ?g { " + " "
							+ entity1.getUri() + " " + " " + rel.getUri() + " "
							+ " " + entity2.getUri() + " . " + "}} LIMIT "
							+ limit;
					double score = entity1.getScore() + rel.getScore()
							+ entity2.getScore();
					sparqlCandidates.add(new SparqlCandidate(sparql, score));
					logger.info("Created SPARQL: " + sparql + " \nWith score: "
							+ score);

					sparql = "SELECT * WHERE {" + " GRAPH ?g { " + " "
							+ entity2.getUri() + " " + " " + rel.getUri() + " "
							+ " " + entity1.getUri() + " . " + "}} LIMIT "
							+ limit;
					score = entity2.getScore() + rel.getScore()
							+ entity1.getScore();
					sparqlCandidates.add(new SparqlCandidate(sparql, score));
					logger.info("Created SPARQL: " + sparql + " \nWith score: "
							+ score);
				}
			}
		}

		// ------------- advanced approach ------------------//
		// TODO

		return sparqlCandidates;
	}

	@Override
	public List<SparqlCandidate> getSparqlCanidates(List<QueryTriple> queryTriples) throws GenerateSparqlException {
		sparqlCandidates = new ArrayList<SparqlCandidate>();

		// convert queryTriples to SparqlLineCandidates
		ArrayList<ArrayList<SparqlLineCandidate>> sparqlLines = new ArrayList<ArrayList<SparqlLineCandidate>>();
		for (QueryTriple queryTriple : queryTriples) {
			sparqlLines.add(getSparqlLines(queryTriple));
		}

		// find start solution for TopN - Sparql Algorithm
		for (int i = 0; i < topN; i++) {

			// create the SparqlCandidate for THIS iteration
			List<SparqlLineCandidate> lines = new ArrayList<SparqlLineCandidate>();
			for (ArrayList<SparqlLineCandidate> sortedLineCandidates : sparqlLines) {
				if (sortedLineCandidates.size() > 0)
					lines.add(sortedLineCandidates.get(0));
			}
			if (lines.isEmpty())
				break;

			sparqlCandidates.add(getSparqlQuery(lines));

			// prepare the list for the NEXT iteration
			if (inputManager.isActiveOption("sparqlOption", "greedy")) {
				// use the next possible value with minimum difference
				double total_difference = Double.MAX_VALUE;
				int counter = 0;
				int line_to_change = 0;
				for (ArrayList<SparqlLineCandidate> sortedLineCandidates : sparqlLines) {
					if (sortedLineCandidates.size() >= 2) {
						double difference = sortedLineCandidates.get(0)
								.getScore()
								- sortedLineCandidates.get(1).getScore();
						if (difference < total_difference) {
							total_difference = difference;
							line_to_change = counter;
						}
					}
					counter++;
				}

				// delete the old top uri from the list
				if (sparqlLines.get(line_to_change).size() > 0) {
					sparqlLines.get(line_to_change).remove(0);
				}

			} else if (inputManager.isActiveOption("sparqlOption",
					"alternating")) {
				// change the candidate every turn
				sparqlLines.get(i).remove(0);
			} else if (inputManager.isActiveOption("sparqlOption",
					"distribution")) {
				// TODO
			}

		}

		if (sparqlCandidates.isEmpty()) {
			logger.info("No valid SPARQL candidate constructed. Try now to create a keyword query...");
			sparqlCandidates = getKeyWordQuery(queryTriples);
		} else if (inputManager.getParameter().containsKey(
				"KeyWordQuestionThreshold")) {
			double threshold = Double.parseDouble(inputManager.getParameter()
					.get("KeyWordQuestionThreshold"));
			if (sparqlCandidates.get(0).getScore() < threshold) {
				logger.info("No valid SPARQL candidate with score higher than "
						+ threshold
						+ "constructed . Try now to create a keyword query...");
				sparqlCandidates = getKeyWordQuery(queryTriples);
			}
		}
		return sparqlCandidates;
	}

	private SparqlCandidate getSparqlQuery(List<SparqlLineCandidate> lines) {
		try {
			limit = Integer.parseInt(inputManager.getOption("SparqlLimit"));
		} catch (Exception e) {
			logger.warn("Failed reading parameter 'SparqlLimit'. Will be set to "
					+ limit);
		}

		String where_clause = "";
		double score = 0.0;
		for (SparqlLineCandidate sparqlLineCandidate : lines) {
			where_clause += sparqlLineCandidate.getText();
			score += sparqlLineCandidate.getScore();
		}
		String sparql = "SELECT * WHERE {" + where_clause + "} LIMIT " + limit;
		return new SparqlCandidate(sparql, score);
	}

	private List<SparqlCandidate> getKeyWordQuery(List<QueryTriple> queryTriples) {
		List<Candidate> candidates = new ArrayList<Candidate>();
		for (QueryTriple triple : queryTriples) {
			for (EntityCandidate entity1Candidate : triple
					.getEntity1Candidates()) {
				candidates.add(entity1Candidate);
			}
			for (RelationCandidate realtionCandidate : triple
					.getRelationCandidates()) {
				candidates.add(realtionCandidate);
			}
			for (EntityCandidate entity2Candidate : triple
					.getEntity2Candidates()) {
				candidates.add(entity2Candidate);
			}
		}
		candidates.sort(new CustomComparator());

		// find start solution for TopN - Sparql Algorithm
		List<SparqlCandidate> sparqlCandidates = new ArrayList<SparqlCandidate>();
		List<String> oldCandidates = new ArrayList<String>();
		for (int i = 0; i < topN; i++) {
			if (candidates.size() - 1 < i)
				break;

			if (!oldCandidates.contains(candidates.get(i).getText())
					&& !candidates.get(i).getText().startsWith("?")) {
				String sparql = "SELECT ?uri where { VALUES ?uri {"
						+ candidates.get(i).getText() + "} }";
				double score = candidates.get(i).getScore();
				sparqlCandidates.add(new SparqlCandidate(sparql, score));

				// remember already used candidates
				oldCandidates.add(candidates.get(i).getText());
			} else {
				// delete double candidates
				candidates.remove(i);
				i--;
			}
		}
		return sparqlCandidates;
	}

	/**
	 * 
	 * @param queryTriples
	 * @return a two dimensional List, first dimension represents a QueryTriple,
	 *         second dimension represents a Candidate List for each QueryTriple
	 */
	private ArrayList<SparqlLineCandidate> getSparqlLines(
			QueryTriple queryTriple) {

		// each entry in this list represents a candidate for a line of
		ArrayList<SparqlLineCandidate> lineCandidates = new ArrayList<SparqlLineCandidate>();

		// TODO: test
		if (queryTriple.getEntity1Candidate() != null
				&& queryTriple.getPredicateCandidate() != null
				&& queryTriple.getEntity2Candidate() != null) {

			lineCandidates.add(new SparqlLineCandidate(queryTriple
					.getEntity1Candidate(),
					queryTriple.getPredicateCandidate(), queryTriple
							.getEntity2Candidate()));
			return lineCandidates;
		}

		Iterator<EntityCandidate> entity1Iterator = queryTriple
				.getEntity1Candidates().iterator();
		if (!entity1Iterator.hasNext()) {
			// if no candidate was found, add a wildcard
			List<EntityCandidate> list = new ArrayList<EntityCandidate>();
			list.add(new EntityCandidate("?entity1", 1, "wildcard"));
			entity1Iterator = list.iterator();
		}
		while (entity1Iterator.hasNext()) {
			EntityCandidate entity1Candidate = entity1Iterator.next();

			Iterator<RelationCandidate> relationIterator = queryTriple
					.getRelationCandidates().iterator();
			if (!relationIterator.hasNext()) {
				// if no candidate was found, add a wildcard
				List<RelationCandidate> list = new ArrayList<RelationCandidate>();
				list.add(new RelationCandidate("?relation", 1, "wildcard"));
				relationIterator = list.iterator();
			}
			while (relationIterator.hasNext()) {
				RelationCandidate relationCandidate = relationIterator.next();

				Iterator<EntityCandidate> entity2Iterator = queryTriple
						.getEntity2Candidates().iterator();
				if (!entity2Iterator.hasNext()) {
					// if no candidate was found, add a wildcard
					List<EntityCandidate> list = new ArrayList<EntityCandidate>();
					list.add(new EntityCandidate("?entity2", 1, "wildcard"));
					entity2Iterator = list.iterator();
				}
				while (entity2Iterator.hasNext()) {
					EntityCandidate entity2Candidate = entity2Iterator.next();

					// create the new sparql line
					lineCandidates.add(new SparqlLineCandidate(
							entity1Candidate, relationCandidate,
							entity2Candidate));
				}
			}
		}

		lineCandidates.sort(new CustomComparator());

		return lineCandidates;
	}

	@Override
	/**
	 * do not use, returns the same query many times...
	 */
	public List<SparqlCandidate> getSparqlCanidates(
			List<QueryTriple> queryTriples, int numberOfTriplesPerSparql) {
		List<SparqlCandidate> sparqlCandidates = new ArrayList<SparqlCandidate>();
		List<QueryTriple> tmp_queryTriples = new ArrayList<QueryTriple>(
				queryTriples);

		for (int j = 0; j < topN; j++) {

			List<SparqlLineCandidate> sparqlLineCandidates = new ArrayList<SparqlLineCandidate>();
			for (int i = 0; i < numberOfTriplesPerSparql; i++) {
				if (tmp_queryTriples.size() > i) {
					QueryTriple triple = tmp_queryTriples.get(i);
					sparqlLineCandidates.add(new SparqlLineCandidate(triple
							.getEntity1Candidate(), triple
							.getPredicateCandidate(), triple
							.getEntity2Candidate()));
				}
			}
			sparqlCandidates.add(getSparqlQuery(sparqlLineCandidates));

		}
		return sparqlCandidates;
	}

	@Override
	public List<SparqlCandidate> getSparqlCanidatesForQueryTripleSet(
			List<ArrayList<QueryTriple>> queryTriplesSet)
			throws GenerateSparqlException {
		List<SparqlCandidate> sparqlCandidates = new ArrayList<SparqlCandidate>();

		for (int j = 0; j < topN && j < queryTriplesSet.size(); j++) {
			List<QueryTriple> queryTriples = queryTriplesSet.get(j);

			// check if candidate list are filled
			if (queryTriples.size() > 0) {
				QueryTriple testTriple = queryTriples.get(0);
				if (testTriple.getEntity1Candidates() != null
						&& testTriple.getEntity2Candidates() != null) {
					sparqlCandidates.addAll(getSparqlCanidates(queryTriples));
					continue;
				}
			}

			List<SparqlLineCandidate> sparqlLineCandidates = new ArrayList<SparqlLineCandidate>();
			for (QueryTriple triple : queryTriples) {
				sparqlLineCandidates.add(new SparqlLineCandidate(triple
						.getEntity1Candidate(), triple.getPredicateCandidate(),
						triple.getEntity2Candidate()));
			}
			sparqlCandidates.add(getSparqlQuery(sparqlLineCandidates));

		}
		return sparqlCandidates;
	}

}
