package naiveanalyzer.impl;

import inputmanagement.InputManager;
import inputmanagement.candidates.RdfCandidate;
import inputmanagement.candidates.RdfCandidateTypes;
import inputmanagement.candidates.impl.EntityCandidate;
import inputmanagement.candidates.impl.RelationCandidate;
import inputmanagement.impl.QueryTriple;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import configuration.ConfigManager;
import configuration.impl.ConfigManagerImpl;
import naivemapper.impl.InsertResult;
import naivemapper.impl.MappedQuery;

public class GreedyAnalyzerImpl {

	private InputManager manager;
	private Logger logger;

	
	public GreedyAnalyzerImpl(InputManager manager) {
		this.manager = manager;

		ConfigManager configManager = new ConfigManagerImpl();
		this.logger = configManager.getLogger();
	}
	

	public List<ArrayList<QueryTriple>> getQueryTriples(List<RdfCandidate> candidates, String question) {
		List<ArrayList<QueryTriple>> queryTripleSet = new ArrayList<ArrayList<QueryTriple>>();
		
		// find all possible candidate combinations
		List<MappedQuery> mappedQueries = greedyCandidateInserting(
				new MappedQuery(question), candidates);
		for (MappedQuery query : mappedQueries) {
			logger.info("BruteForceMapper found Candidates in text: "
					+ query.toString());
		}

		// Convert mapped queries to querytriples
		for (MappedQuery query : mappedQueries) {
			ArrayList<QueryTriple> queryTriples = findQueryTriples(query);
			if (queryTriples != null) {
				queryTripleSet.add(queryTriples);
			}
		}

		// queryTripleSet.sort(new QueryTripleSetComparator() );

		for (ArrayList<QueryTriple> queryTriples : queryTripleSet) {
			logger.info("BruteForceMapper found Query Triples Set:");
			for (QueryTriple triple : queryTriples) {
				logger.info("BruteForceMapper found Query Triples: "
						+ triple.toString() + " Score: " + triple.getScore());
			}
		}

		return queryTripleSet;
	}


	private ArrayList<QueryTriple> findQueryTriples(MappedQuery query) {
		ArrayList<QueryTriple> queryTriples = new ArrayList<QueryTriple>();
		int status = 0;
		int relationCounter = 0;

		EntityCandidate entity = null;
		RelationCandidate relation = null;
		EntityCandidate variable = null;

		for (RdfCandidate candidate : query.getQuery()) {

			if (candidate.getType().equals(RdfCandidateTypes.UNKNOWN))
				continue;

			switch (status) {
			case 0:
				if (candidate.getType().equals(RdfCandidateTypes.ENTITY)
						|| candidate.getType().equals(RdfCandidateTypes.CLASS)) {

					// accept entities, classes and variables
					entity = (EntityCandidate) candidate;
					status = 1;

				} else if (candidate.getType().equals(
						RdfCandidateTypes.VARIABLE)) {

					variable = (EntityCandidate) candidate;
					status = 2;

				} else if (candidate.getType().equals(
						RdfCandidateTypes.RELATION)) {

					relation = (RelationCandidate) candidate;
					status = 3;
				}
				break;

			case 1:
				if (candidate.getType().equals(RdfCandidateTypes.ENTITY)
						|| candidate.getType().equals(RdfCandidateTypes.CLASS)) {

					entity = (EntityCandidate) candidate;
					status = 1;

				} else if (candidate.getType().equals(
						RdfCandidateTypes.VARIABLE)) {

					relation = new RelationCandidate("?relation" + relationCounter++, 1);
					variable = (EntityCandidate) candidate;

					QueryTriple triple = new QueryTriple(entity, relation,
							variable);
					triple.setScore(entity.getScore() + relation.getScore()
							+ variable.getScore());
					queryTriples.add(triple);

					entity = null;
					relation = null;
					status = 2;

				} else if (candidate.getType().equals(
						RdfCandidateTypes.RELATION)) {

					relation = (RelationCandidate) candidate;
					status = 4;

				}
				break;

			case 2:
				if (candidate.getType().equals(RdfCandidateTypes.ENTITY)
						|| candidate.getType().equals(RdfCandidateTypes.CLASS)) {

					entity = (EntityCandidate) candidate;
					relation = new RelationCandidate("?relation" + relationCounter++, 0);

					QueryTriple triple = new QueryTriple(variable, relation,
							entity);
					triple.setScore(variable.getScore() + relation.getScore()
							+ entity.getScore());
					queryTriples.add(triple);

					entity = null;
					relation = null;
					status = 2;

				} else if (candidate.getType().equals(
						RdfCandidateTypes.RELATION)) {

					relation = (RelationCandidate) candidate;
					status = 5;

				} else if (candidate.getType().equals(
						RdfCandidateTypes.VARIABLE)) {

					relation = (RelationCandidate) candidate;
					status = 2;

				}
				break;

			case 3:
				if (candidate.getType().equals(RdfCandidateTypes.ENTITY)
						|| candidate.getType().equals(RdfCandidateTypes.CLASS)) {

					entity = (EntityCandidate) candidate;
					variable = new EntityCandidate("?variable", 0);
					variable.setType(RdfCandidateTypes.VARIABLE);

					QueryTriple triple = new QueryTriple(variable, relation,
							entity);
					triple.setScore(variable.getScore() + relation.getScore()
							+ entity.getScore());
					queryTriples.add(triple);

					entity = null;
					relation = null;
					status = 2;

				} else if (candidate.getType().equals(
						RdfCandidateTypes.RELATION)) {

					relation = (RelationCandidate) candidate;
					status = 3;

				} else if (candidate.getType().equals(
						RdfCandidateTypes.VARIABLE)) {
					variable = (EntityCandidate) candidate;
					entity = new EntityCandidate("?entity", 0);

					QueryTriple triple = new QueryTriple(variable, relation,
							entity);
					triple.setScore(variable.getScore() + relation.getScore()
							+ entity.getScore());
					queryTriples.add(triple);

					entity = null;
					relation = null;
					status = 2;
				}
				break;

			case 4:
				if (candidate.getType().equals(RdfCandidateTypes.ENTITY)
						|| candidate.getType().equals(RdfCandidateTypes.CLASS)) {

					entity = (EntityCandidate) candidate;
					relation = null;
					variable = null;
					status = 1;

				} else if (candidate.getType().equals(
						RdfCandidateTypes.RELATION)) {

					relation = (RelationCandidate) candidate;
					status = 4;

				} else if (candidate.getType().equals(
						RdfCandidateTypes.VARIABLE)) {

					variable = (EntityCandidate) candidate;

					QueryTriple triple = new QueryTriple(variable, relation,
							entity);
					triple.setScore(variable.getScore() + relation.getScore()
							+ entity.getScore());
					queryTriples.add(triple);

					entity = null;
					relation = null;
					status = 2;
				}
				break;

			case 5:
				if (candidate.getType().equals(RdfCandidateTypes.ENTITY)
						|| candidate.getType().equals(RdfCandidateTypes.CLASS)) {

					entity = (EntityCandidate) candidate;

					QueryTriple triple = new QueryTriple(variable, relation,
							entity);
					triple.setScore(variable.getScore() + relation.getScore()
							+ entity.getScore());
					queryTriples.add(triple);

					entity = null;
					relation = null;
					status = 2;

				} else if (candidate.getType().equals(
						RdfCandidateTypes.RELATION)) {

					relation = (RelationCandidate) candidate;
					status = 5;

				} else if (candidate.getType().equals(
						RdfCandidateTypes.VARIABLE)) {

					entity = null;
					relation = null;
					status = 2;
				}
				break;

			default:
				// should not happen...
				break;
			}
		}

		if (queryTriples.isEmpty()) {
			return null;
		} else {
			return queryTriples;
		}
	}


	private List<MappedQuery> greedyCandidateInserting(MappedQuery query,
			List<RdfCandidate> candidates) {
		List<MappedQuery> mappedQueries = new ArrayList<MappedQuery>();

		List<RdfCandidate> candidates_copy = new ArrayList<RdfCandidate>(
				candidates);

		for (int i = 0; i < candidates.size(); i++) {
			MappedQuery mappedQuery = query.clone();
			RdfCandidate leastImportantCandidate = null;
			double contributionOfleastImportantCandidate = Double.MAX_VALUE;

			List<RdfCandidate> tmp_candidates = new ArrayList<RdfCandidate>(
					candidates_copy);
			while (!tmp_candidates.isEmpty()) {
				RdfCandidate candidate = tmp_candidates.remove(0);

				InsertResult result = mappedQuery.insertCandidate(candidate);
				if (!result.didInsert_worked()) {
					double score_difference = result.getBlockingCandidate()
							.getScore() - candidate.getScore();
					if (score_difference < contributionOfleastImportantCandidate) {
						leastImportantCandidate = result.getBlockingCandidate();
						contributionOfleastImportantCandidate = score_difference;
					}
				} else {
					double score_difference = candidate.getScore() - 0;
					if (score_difference < contributionOfleastImportantCandidate) {
						leastImportantCandidate = candidate;
						contributionOfleastImportantCandidate = score_difference;
					}
				}
			}
			mappedQueries.add(mappedQuery.clone());
			candidates_copy.remove(leastImportantCandidate);
		}

		return mappedQueries;
	}

}
