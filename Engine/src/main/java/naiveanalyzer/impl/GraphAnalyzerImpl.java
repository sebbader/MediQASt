package naiveanalyzer.impl;

import java.util.ArrayList;
import java.util.List;

import inputmanagement.candidates.RdfCandidate;
import inputmanagement.candidates.RdfCandidateTypes;
import inputmanagement.candidates.impl.EntityCandidate;
import inputmanagement.candidates.impl.RelationCandidate;
import inputmanagement.impl.CustomComparator;
import inputmanagement.impl.GenerateSparqlException;
import inputmanagement.impl.InputManagerImpl;
import inputmanagement.impl.QueryTriple;
import naivemapper.impl.InsertResult;
import naivemapper.impl.MappedQuery;
import naivemapper.impl.QueryTripleSetComparator;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.serializer.QuerySerializer;

import configuration.ConfigManager;
import configuration.impl.ConfigManagerImpl;
import configuration.impl.RdfList;
import connector.SPARQLEndpointConnector;
import connector.impl.SPARQLEndpointConnectorImpl;

public class GraphAnalyzerImpl {

	private InputManagerImpl manager;
	private Logger logger;


	public GraphAnalyzerImpl(InputManagerImpl manager) {
		this.manager = manager;

		ConfigManager configManager = new ConfigManagerImpl();
		this.logger = configManager.getLogger();
	}


	public List<ArrayList<QueryTriple>> getQueryTriples(List<RdfCandidate> input_candidates, String question) throws NumberFormatException, GenerateSparqlException {
		List<RdfCandidate> candidates = new ArrayList<RdfCandidate>(input_candidates);
		MQueryList<MappedQuery> mappedQueries = new MQueryList<MappedQuery>();
		List<ArrayList<QueryTriple>> queryTripleSet = new ArrayList<ArrayList<QueryTriple>>();
		int max = Integer.parseInt(manager.getOption("NumberOfSparqlCandidates"));

		for (int i = 0; i < max; i++) {
			if (candidates.isEmpty()) return queryTripleSet;
			int counter = 0;
			RdfCandidate topCandidate = candidates.get(counter);

			try {
				while (!topCandidate.getType().equals(RdfCandidateTypes.ENTITY) && !topCandidate.getType().equals(RdfCandidateTypes.CLASS)) {
					topCandidate = candidates.get(++counter);
				}
			} catch (IndexOutOfBoundsException e) {
				break;
			}
			candidates.remove(topCandidate);

			List<RdfCandidate> tmpCandidates = new ArrayList<RdfCandidate>();
			tmpCandidates.add(topCandidate);
			for (RdfCandidate can : candidates) {
				if (can.getText().equals("?variable")) {
					tmpCandidates.add(can);
					continue;
				}
				if (isAvailable(topCandidate, can)) {
					tmpCandidates.add(can);
				} else {
					logger.debug("Ignoring " + can.getText() + " for one round. ");
				}
			}

			// find all possible candidate combinations
			List<MappedQuery> tmpMappedQueries = greedyCandidateInserting(
					new MappedQuery(question), tmpCandidates);
			tmpMappedQueries.sort(new MappedQueryComperator());
			if (tmpMappedQueries.size() >= 5) {
				tmpMappedQueries = tmpMappedQueries.subList(0, 5);
			}
			for (MappedQuery query : tmpMappedQueries) {
				logger.info("NaivAnalyzer found Candidates in text: "
						+ query.toString());
			}
			mappedQueries.addAll(tmpMappedQueries);
		}
		mappedQueries.sort(new MappedQueryComperator());

		// Convert mapped queries to querytriples
		for (MappedQuery query : mappedQueries) {
			ArrayList<QueryTriple> queryTriples = findQueryTriples(query);
			if (queryTriples != null) {
				queryTripleSet.add(queryTriples);
			}
		}


		for (ArrayList<QueryTriple> queryTriples : queryTripleSet) {
			logger.info("NaiveMapper found Query Triples Set:");
			for (QueryTriple triple : queryTriples) {
				logger.info("BruteForceMapper found Query Triples: "
						+ triple.toString() + " Score: " + triple.getScore());
			}
		}

		//		queryTripleSet.sort(new QueryTripleSetComparator());
		return queryTripleSet;
	}


	private boolean isAvailable(RdfCandidate topCandidate, RdfCandidate can) {
		boolean isAvailable = false;
		boolean check2steps = manager.isActiveOption("NaiveAnalyzer:ValidateSteps", "2");

		if (can.getType().equals(RdfCandidateTypes.ENTITY) || can.getType().equals(RdfCandidateTypes.CLASS)) {
			String sparql = "";
			if (check2steps) {
				sparql = "{ "
						+ "  { " + topCandidate.getText() + " ?r " + can.getText() + " . } "
						+ "  union { " + can.getText() + " ?r " + topCandidate.getText() + " . } "
						+ "}"
						+ "union "
						+ "{ "
						+ "  { " + topCandidate.getText() + " ?r1 ?x . } union { ?x ?r1 " + topCandidate.getText() + " . } . "
						+ "  { ?x ?r2 " + can.getText() + " . } union { " + can.getText() + " ?r2 ?x . } . "
						+ " MINUS { " + topCandidate.getText() + " <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?x . "
						+ " " + can.getText() + " <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?x . }"
						+ "}";
			} else {
				sparql = "{ "
						+ "  { " + topCandidate.getText() + " ?r " + can.getText() + " . } "
						+ "  union { " + can.getText() + " ?r " + topCandidate.getText() + " . } "
						+ "}";
			}
			SPARQLEndpointConnector connector = InputManagerImpl.endpointConnector;
			isAvailable = connector.executeAsk(sparql);
		} else if (can.getType().equals(RdfCandidateTypes.RELATION)) {
			String sparql = "";
			if (check2steps) {
				sparql = 
					" {"
					+ " { " + topCandidate.getText() + " " + can.getText() + " ?x . } "
							+ " union { ?x " + can.getText() + " " + topCandidate.getText() + " . } "
							+ "union "
							+ "{ "
							+ "  {{ " + topCandidate.getText() + " ?r ?x . } union { ?x ?r " + topCandidate.getText() + " . }}"
							+ " union "
							+ "  {{ ?x " + can.getText() + " ?y . } union { ?y " + can.getText() + " ?x . }}"
							+ "}"
							+ "}";
			} else {
				sparql = "{ "
						+ " { " + topCandidate.getText() + " " + can.getText() + " ?x . } "
						+ " union { ?x " + can.getText() + " " + topCandidate.getText() + " . } "
						+ "}";
			}
			SPARQLEndpointConnector connector = InputManagerImpl.endpointConnector;
			isAvailable = connector.executeAsk(sparql);
		}
		return isAvailable;
	}


	private List<RdfCandidate> getSurrounding(RdfCandidate candidate) {
		RdfList availableResources = new RdfList();
		SPARQLEndpointConnector connector = manager.endpointConnector;
		String sparql = "";

		if (candidate.getType().equals(RdfCandidateTypes.ENTITY) || candidate.getType().equals(RdfCandidateTypes.CLASS)) {
			sparql = "SELECT distinct ?entity ?relation ?entity2 ?relation2 WHERE { "
					+ "{ " + candidate.getText() + " ?relation ?entity . "
					+ "{ ?entity ?relation2 ?entity2 . } union { ?entity2 ?relation2 ?entity  . }"
					+ "} "
					+ "union  "
					+ "{ ?entity ?relation " + candidate.getText() + " . "
					+ "{ ?entity ?relation2 ?entity2 . } union { ?entity2 ?relation2 ?entity . }"
					+ "} "
					+ "} ";

			ResultSet rs = connector.executeQuery(sparql);

			while (rs.hasNext()) {
				QuerySolution qs = rs.next();
				String entity = qs.get("entity").toString();
				availableResources.addDistinct( new EntityCandidate(entity, 0));
				String relation = qs.getResource("relation").getURI();
				availableResources.addDistinct( new RelationCandidate(relation, 0));
				String entity2 = qs.get("entity2").toString();
				availableResources.addDistinct( new EntityCandidate(entity2, 0));
				String relation2 = qs.getResource("relation2").getURI();
				availableResources.addDistinct( new RelationCandidate(relation2, 0));
			}

		} else if (candidate.getType().equals(RdfCandidateTypes.RELATION)) {
			sparql = "SELECT distinct ?subj ?subj2 ?relation ?obj ?obj2 WHERE { "
					+ "{ ?subj <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?obj . "
					+ "{ ?obj ?relation ?obj2 . } union { ?subj2 ?relation ?obj . }"
					+ "union"
					+ "{ ?subj ?relation ?obj2 . } union { ?subj2 ?relation ?subj . }"
					+ "} "
					+ "} LIMIT 100000";

			ResultSet rs = connector.executeQuery(sparql);

			while (rs.hasNext()) {
				QuerySolution qs = rs.next();
				String entity = qs.get("subj").toString();
				availableResources.addDistinct( new EntityCandidate(entity, 0));
				String subj2 = qs.get("subj2").toString();
				availableResources.addDistinct( new EntityCandidate(subj2, 0));
				String obj = qs.get("obj").toString();
				availableResources.addDistinct( new EntityCandidate(obj, 0));
				String obj2 = qs.get("obj2").toString();
				availableResources.addDistinct( new EntityCandidate(obj2, 0));
				String relation = qs.getResource("relation").getURI();
				availableResources.addDistinct( new RelationCandidate(relation, 0));
			}
		}

		return availableResources;
	}


	private List<MappedQuery> greedyCandidateInserting(MappedQuery query,
			List<RdfCandidate> candidates) {
		MQueryList<MappedQuery> mappedQueries = new MQueryList<MappedQuery>();

		List<RdfCandidate> candidates_copy = new ArrayList<RdfCandidate>(
				candidates);


		MappedQuery mappedQuery = query.clone();

		List<RdfCandidate> tmp_candidates = new ArrayList<RdfCandidate>(
				candidates_copy);
		tmp_candidates.sort(new CustomComparator());
		List<RdfCandidate> notInsertedCandidates = new ArrayList<RdfCandidate>();
		while (!tmp_candidates.isEmpty()) {
			RdfCandidate candidate = tmp_candidates.remove(0);
			InsertResult result = mappedQuery.insertCandidate(candidate);
			if (!result.didInsert_worked()) {
				candidate.setBlockingCandidate(result.getBlockingCandidate());
				notInsertedCandidates.add(candidate);
			}
		}

		mappedQueries.addDistinct(mappedQuery.clone());

		//
		while (!notInsertedCandidates.isEmpty()) {
			mappedQuery = query.clone();
			RdfCandidate insertCandidate = notInsertedCandidates.remove(0);
			tmp_candidates = new ArrayList<RdfCandidate>(
					candidates_copy);
			tmp_candidates.remove(insertCandidate.getBlockingCandidate());
			mappedQuery.insertCandidate(insertCandidate);
			while (!tmp_candidates.isEmpty()) {
				RdfCandidate candidate = tmp_candidates.remove(0);
				mappedQuery.insertCandidate(candidate);
			}
			mappedQueries.addDistinct(mappedQuery.clone());
		}

		return mappedQueries;
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

					List<Integer> positions = candidate.getPosition();
					relation = new RelationCandidate(candidate.getText(), candidate.getScore()) ;
					relation.setPosition(positions);
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


}
