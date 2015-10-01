package naivemapper.impl;

import inputmanagement.InputManager;
import inputmanagement.candidates.Candidate;
import inputmanagement.candidates.RdfCandidate;
import inputmanagement.candidates.RdfCandidateTypes;
import inputmanagement.candidates.impl.EntityCandidate;
import inputmanagement.candidates.impl.RelationCandidate;
import inputmanagement.impl.CustomComparator;
import inputmanagement.impl.GenerateSparqlException;
import inputmanagement.impl.QueryTriple;
import inputmanagement.impl.QueryTripleComparator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import lucene.LuceneSearcher;
import mapper.ClassMapper;
import mapper.EntityMapper;
import mapper.RelationMapper;
import mapper.impl.ClassMapperImpl;
import mapper.impl.EntityMapperImpl;
import mapper.impl.RelationMapperImpl;
import naivemapper.NaiveMapper;

import org.apache.log4j.Logger;

import configuration.ConfigManager;
import configuration.impl.CommonMethods;
import configuration.impl.ConfigManagerImpl;
import edu.stanford.Ner.NerClassifier;

public class NaiveMapperImpl implements NaiveMapper {

	private InputManager manager;
	private Logger logger;

	public NaiveMapperImpl(InputManager manager) {
		this.manager = manager;

		ConfigManager configManager = new ConfigManagerImpl();
		this.logger = configManager.getLogger();
	}

	@Override
	public List<ArrayList<QueryTriple>> getQueryTriples(String question)
			throws GenerateSparqlException {
		List<ArrayList<QueryTriple>> queryTripleSet = new ArrayList<ArrayList<QueryTriple>>();

		String[] words = getWords(question);

		int window;
		try {
			window = Integer.parseInt(manager
					.getOption("NaiveMapper:windows"));
		} catch (GenerateSparqlException e) {
			window = 2;
			logger.error(
					"Could not find 'BruteForceSearch:windows' parameter. Use default value 2.",
					e);
		}

		double threshold;
		try {
			threshold = Double.parseDouble(manager.getOption(
					"NaiveMapper:threshold").toString());
		} catch (GenerateSparqlException e) {
			threshold = 2.;
			logger.error(
					"Could not find 'BruteForceMapper:threshold' parameter. Use default value 2.0 .",
					e);
		}

		// find patterns with length "window" in the query string that match an
		// entity/relation
		List<RdfCandidate> candidates = new ArrayList<RdfCandidate>();
		for (int k = window; k > 0; k--) {
			// start with the longest possible window as entities matching a
			// long pattern
			// tend to be better

			for (int i = 0; i <= words.length - k; i++) {
				List<Integer> position = new ArrayList<Integer>();
				String term = "";
				for (int j = i; j < i + k; j++) {
					term += words[j] + " ";
					position.add(j);
				}
				term = term.trim();

				List<RdfCandidate> new_candidates = findCandidate(term,
						position, threshold);

				if (new_candidates != null) {
					candidates.addAll(new_candidates);
					for (RdfCandidate candidate : new_candidates) {
						logger.info("Found " + candidate.getText()
								+ " for text '" + term + "' Score: "
								+ candidate.getScore() + " Type: "
								+ candidate.getType());
					}
				}
			}
		}

		// remove useless candidates
		if (manager.isActiveOption("BruteForceMapper:filter", true)) {
			candidates = CommonMethods.filterCandidates(candidates);
		}

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
			queryTripleSet.add(queryTriples);
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
					relation = new RelationCandidate("?relation" + relationCounter++, 1);

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
					// do nothing
				}
				break;

			case 3:
				if (candidate.getType().equals(RdfCandidateTypes.ENTITY)
						|| candidate.getType().equals(RdfCandidateTypes.CLASS)) {

					entity = (EntityCandidate) candidate;
					variable = new EntityCandidate("?uri", 1);
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
					entity = new EntityCandidate("?entity", 1);

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
					status = 1;

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
					status = 2;

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
		return queryTriples;
	}

	private List<MappedQuery> greedyCandidateInserting(MappedQuery query,
			List<RdfCandidate> candidates) {
		List<MappedQuery> mappedQueries = new ArrayList<MappedQuery>();
		candidates.sort(new CustomComparator());
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

	private List<RdfCandidate> findCandidate(String term,
			List<Integer> position, double threshold) {
		List<RdfCandidate> candidates = new ArrayList<RdfCandidate>();

		if (CommonMethods.isVariable(term)) {
			EntityCandidate candidate = new EntityCandidate("?uri", 10);
			candidate.setType(RdfCandidateTypes.VARIABLE);
			candidate.setPosition(position);
			candidates.add(candidate);
			return candidates;
		}

		List<EntityCandidate> classes = null;
		if (!manager.isActiveOption("BruteForceMapper:noClass", true)) {
			ClassMapper classMapper = new ClassMapperImpl(manager);
			classes = classMapper.getClassCandidates(term);
			for (RdfCandidate candidate : classes) {
				if (candidate.getScore() >= threshold)
					candidates.add(candidate);
			}
		}

		List<EntityCandidate> entities = null;
		if (!manager.isActiveOption("BruteForceMapper:noEntity", true)) {
			EntityMapper entityMapper = new EntityMapperImpl(manager);
			entities = entityMapper.getEntityCandidates(term);
			for (RdfCandidate candidate : entities) {
				if (candidate.getScore() >= threshold)
					candidates.add(candidate);
			}
		}

		List<RelationCandidate> relations = null;
		if (!manager.isActiveOption("BruteForceMapper:noRelation", true)) {
			RelationMapper relationMapper = new RelationMapperImpl(manager);
			relations = relationMapper.getRelationCandidates(term);
			for (RdfCandidate candidate : relations) {
				if (candidate.getScore() >= threshold)
					candidates.add(candidate);
			}
		}

		candidates.forEach((candidate) -> candidate.setPosition(position));
		candidates.sort(new CustomComparator());

		if (candidates.size() > 0) {
			return candidates;
		} else {
			return null;
		}

		// old code
		/*
		 * if ( !manager.isActiveOption("BruteForceMapper:noClass", true) ) {
		 * ClassMapper classMapper = new ClassMapperImpl(logger);
		 * List<EntityCandidate> classes = classMapper.getClassCandidates(term);
		 * classes.forEach( (classCandidate) ->
		 * classCandidate.setPosition(position));
		 * queryTriple.addEntity1Candidates(classes); }
		 * 
		 * if ( !manager.isActiveOption("BruteForceMapper:noEntity", true) ) {
		 * EntityMapper entityMapper = new EntityMapperImpl(logger);
		 * List<EntityCandidate> entities =
		 * entityMapper.getEntityCandidates(term); entities.forEach(
		 * (entityCandidate) -> entityCandidate.setPosition(position));
		 * queryTriple.addEntity1Candidates(entities); } else {
		 * List<EntityCandidate> entity1Candidates = new
		 * ArrayList<EntityCandidate>(); entity1Candidates.add(new
		 * EntityCandidate("?entity1", 1, "variable") );
		 * queryTriple.setEntity2Candidates(entity1Candidates); }
		 * 
		 * if ( !manager.isActiveOption("BruteForceMapper:noRelation", true) ) {
		 * RelationMapper relationMapper = new RelationMapperImpl(logger,
		 * manager); List<RelationCandidate> relations =
		 * relationMapper.getRelationCandidates(term); relations.forEach(
		 * (relationCandidate) -> relationCandidate.setPosition(position));
		 * queryTriple.addRelationCandidates(relations); } else {
		 * List<RelationCandidate> relationCandidates = new
		 * ArrayList<RelationCandidate>(); relationCandidates.add(new
		 * RelationCandidate("?relation", 1, "variable") );
		 * queryTriple.setRelationCandidates(relationCandidates); }
		 */
	}

	private String[] getWords(String question) {
		question = question.replaceAll("[\\?\\,\\.]", "");
		return question.split(" ");
	}

}
