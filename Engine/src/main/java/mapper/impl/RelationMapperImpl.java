package mapper.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

import configuration.ConfigManager;
import configuration.impl.ConfigManagerImpl;
import connector.SPARQLEndpointConnector;
import rdf.Triple;
import rdf.impl.RDFPath;
import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;
import wordnetsimilarity.WordNetSimilarityMetric;
import lucene.LuceneSearcher;
import mapper.RelationMapper;
import inputmanagement.InputManager;
import inputmanagement.candidates.impl.EntityCandidate;
import inputmanagement.candidates.impl.RelationCandidate;
import inputmanagement.impl.CustomComparator;
import inputmanagement.impl.GenerateSparqlException;
import inputmanagement.impl.InputManagerImpl;
import inputmanagement.impl.RelationCandidatesArrayList;

public class RelationMapperImpl implements RelationMapper {

	private Logger logger;
	private InputManager inputManager;

	private List<RelationCandidate> relations = new RelationCandidatesArrayList();

	public RelationMapperImpl(InputManager inputManager) {
		ConfigManager configManager = new ConfigManagerImpl();
		this.logger = configManager.getLogger();

		this.inputManager = inputManager;
	}

	public List<RelationCandidate> getRelationCandidates() {
		relations.sort(new CustomComparator());
		return this.relations;
	}

	public List<RelationCandidate> getRelationCandidates(String relation) {
		// compute relations as in other method
		findRelationCandidatesWithLucene(relation);

		relations.sort(new CustomComparator());
		return this.relations;
	}

	public List<RelationCandidate> findRelationCandidates(String relation,
			List<EntityCandidate> entity1Candidates,
			List<EntityCandidate> entity2Candidates)
			throws GenerateSparqlException {

		// compute relations as in other method
		findRelationCandidatesWithLucene(relation);

		// also search for relations in the near surrounding of the entity
		// candidates
		findRelationCandidatesFromEntityEnvironment(entity1Candidates, relation);
		findRelationCandidatesFromEntityEnvironment(entity2Candidates, relation);

		relations.sort(new CustomComparator());

		return relations;
	}

	public List<RelationCandidate> findRelationCandidatesWithLucene(
			String relation) {

		// 1) relation already is a valid uri
		Pattern p = Pattern.compile("<http://.*>");
		Matcher m = p.matcher(relation);
		if (m.matches()) {
			relations.add(new RelationCandidate(relation, 10));
			logger.info("Identified relation " + relation
					+ " as valid uri. No further processing.");
			return relations;
		}


		// 2) assume that the entity exists as a resource in the Lucene index
		try {
			LuceneSearcher searcher = new LuceneSearcher();
			logger.info("Search for \"" + relation
					+ "\" in Lucene relation index.");

			relations.addAll(searcher.searchRelation(relation, inputManager));
			return relations;
		} catch (Exception e1) {
			logger.error("Could not identify relation: " + relation + ".");
			// e1.printStackTrace();
		}

		logger.debug("Could not identify relation: " + relation + ".");

		return null;
	}

	public List<RelationCandidate> findRelationCandidatesFromEntityEnvironment(
			List<EntityCandidate> entityCandidates, String relation) {

		for (EntityCandidate entity : entityCandidates) {

			if (entity.getEnvironment() == null)
				break;
			for (RDFPath rdfPath : entity.getEnvironment()) {
				for (Triple triple : rdfPath.getPath()) {
					// for each relation in the environment of each
					// entityCandidate
					// compare if the searched relation is similar to its label

					String pred = triple.getPredicate();
					String label = getLabelByUri(pred);

					if (inputManager.isActiveOption(
							"RelationManagerSimilarity", "WordNet")) {
						// compare with WordNetSimilarity
						WordNetSimilarityMetric wordNetMetric = new WordNetSimilarityMetric();
						float similarity = (float) wordNetMetric
								.getPathSimilarity(label, relation);
						if (similarity >= 0.4) {
							RelationCandidate candidate = new RelationCandidate(
									pred, 10 * similarity * entity.getScore(),
									label);
							((RelationCandidatesArrayList) relations)
									.addDistinctCandidate(candidate);
						}
					} else if (inputManager.isActiveOption(
							"RelationManagerSimilarity", "Levenshtein")) {
						// compare the Strings with String Similarity Algorithms
						Levenshtein stringMetric = new Levenshtein();
						float similarity = stringMetric.getSimilarity(label,
								relation);
						if (similarity >= 0.4) {
							RelationCandidate candidate = new RelationCandidate(
									pred, 10 * similarity * entity.getScore(),
									label);
							((RelationCandidatesArrayList) relations)
									.addDistinctCandidate(candidate);
						}
					} else {
						logger.error("No valid RelationManagerSimilarity found, can not find RelationCandidates!");
					}
				}
			}
		}
		return relations;
	}

	// TODO delete method
	@SuppressWarnings("unused")
	private List<RelationCandidate> getRelationUriOld(String relation)
			throws GenerateSparqlException {
		//
		HashMap<String, String> properties = new HashMap<String, String>();
		properties.put("head", "");
		properties.put("type",
				"<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>");
		properties.put("label", "<http://www.w3.org/2000/01/rdf-schema#label>");
		properties.put("subject", "<http://purl.org/dc/terms/subject>");
		properties.put("broader",
				"<http://www.w3.org/2004/02/skos/core#broader>");
		properties.put("same as", "<http://www.w3.org/2002/07/owl#sameAs>");
		properties.put("icd", "<http://dbpedia.org/property/icd>");
		properties.put("icd11", "<http://icd11>");
		properties.put("name", "<http://dbpedia.org/property/name>");
		properties.put("reason", "<http://dbpedia.org/property/reason>");
		properties.put("caption", "<http://dbpedia.org/property/caption>");
		properties.put("abstract", "<http://dbpedia.org/ontology/abstract>");

		AbstractStringMetric metric = new Levenshtein();
		double similarityThreshold = 0.1;
		// match the relation
		List<RelationCandidate> relations = new ArrayList<RelationCandidate>();
		Iterator<Entry<String, String>> iter = properties.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<String, String> entry = iter.next();
			float similarity = metric.getSimilarity(entry.getKey(), relation);
			if (similarity > similarityThreshold) {
				RelationCandidate foundRelation = new RelationCandidate(
						entry.getValue(), similarity);
				relations.add(foundRelation);
			}
		}
		if (relations.isEmpty()) {
			logger.warn("Could not identify relation.");
			throw new GenerateSparqlException("Could not identify relation.");
		}

		Collections.sort(relations, new CustomComparator());

		return relations;
	}

	private String getLabelByUri(String uri) {
		String sparql = "select distinct ?label where {graph ?g{" + uri
				+ " <http://www.w3.org/2000/01/rdf-schema#label> ?label ."
				+ "}} LIMIT 1";

		SPARQLEndpointConnector connector = InputManagerImpl.endpointConnector;
		ResultSet rs = connector.executeQuery(sparql);
		String label = "";
		if (rs.hasNext()) {
			QuerySolution qs = rs.next();
			label = qs.getLiteral("label").getString();
		}

		return label;
	}

}
