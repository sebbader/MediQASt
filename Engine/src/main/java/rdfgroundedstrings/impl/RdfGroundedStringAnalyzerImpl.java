package rdfgroundedstrings.impl;

import inputmanagement.impl.GenerateSparqlException;
import inputmanagement.impl.QueryTriple;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import postagger.PosTagger;
import postagger.impl.PosTaggerImpl;
import configuration.ConfigManager;
import configuration.impl.CommonMethods;
import configuration.impl.ConfigManagerImpl;
import rdfgroundedstrings.RdfGroundedStringAnalyzer;

public class RdfGroundedStringAnalyzerImpl implements RdfGroundedStringAnalyzer {

	private Logger logger;

	private String[] blankNodes = {"?a", "?b", "?c", "?d", "?e"};
	private int blankNodeIndex;

	public RdfGroundedStringAnalyzerImpl() {
		ConfigManager configManager = new ConfigManagerImpl();
		this.logger = configManager.getLogger();
		blankNodeIndex = -1;
	}

	@Override
	public List<ArrayList<QueryTriple>> getQueryTriples(String query)
			throws GenerateSparqlException {
		List<ArrayList<QueryTriple>> queryTripleSets = new ArrayList<ArrayList<QueryTriple>>();

		PosTagger posTagger = new PosTaggerImpl();
		ArrayList<String> entities = posTagger.getEntities(query);

		logger.info("POS-Tagger found entities: " + entities.toString());

		List<ArrayList<String>> entitiesPowerSet = getPowerSet(entities);

		for (List<String> entityList : entitiesPowerSet) {
			blankNodeIndex = -1;
			ArrayList<QueryTriple> queryTriples = new ArrayList<QueryTriple>();

			int positionOfVariable = getPostionOfVariable(query, entityList);
			String queryWithVariable = replaceVariable(query);

			List<String> entitiesBeforeVar = new ArrayList<String>();
			for (int i = 0; i < positionOfVariable; i++) {
				entitiesBeforeVar.add(entityList.get(i));
			}
			if (!entitiesBeforeVar.isEmpty()) {
				entitiesBeforeVar.add("VARIABLE");
				queryTriples.addAll(findQueryTriples(queryWithVariable,
						entitiesBeforeVar));
			}

			List<String> entitiesAfterVar = new ArrayList<String>();
			for (int i = positionOfVariable; i < entityList.size(); i++) {
				entitiesAfterVar.add(entityList.get(i));
			}
			if (!entitiesAfterVar.isEmpty()) {
				entitiesAfterVar.add(0, "VARIABLE");
				List<QueryTriple> triples = findQueryTriples(queryWithVariable,
						entitiesAfterVar);
				if (triples != null)
					queryTriples.addAll(triples);
			}

			if (!queryTriples.isEmpty()) {
				if (queryTriples != null)
					queryTripleSets.add(queryTriples);
			}
		}

		return queryTripleSets;
	}

	private String replaceVariable(String query) {
		String[] words = query.split(" ");
		String newquery = "";

		for (String word : words) {
			if (CommonMethods.isVariable(word)) {
				newquery += "VARIABLE ";
			} else {
				newquery += word + " ";
			}
		}
		return newquery.trim();
	}

	private List<QueryTriple> findQueryTriples(String query,
			List<String> entities) {
		List<QueryTriple> queryTriples = new ArrayList<QueryTriple>();
		QueryTriple newQueryTriple = null;
		QueryTriple oldQueryTriple = null;

		String entity = entities.get(0);
		int index = entities.indexOf(entity) + 1;
		while (index < entities.size()) {

			String next_entity = entities.get(index);
			String pattern = getPatternBetweenEntities(query, entity,
					next_entity);

			if (oldQueryTriple == null && !entity.isEmpty()
					&& !next_entity.isEmpty()) {

				if (pattern.isEmpty())
					pattern = "?relation";
				newQueryTriple = new QueryTriple(entity, pattern, next_entity);

			} else if (oldQueryTriple != null && !entity.isEmpty()
					&& !next_entity.isEmpty()) {

				if (pattern.isEmpty())
					pattern = "?relation";

				queryTriples.remove(oldQueryTriple);
				String blankNode = getBlankNode();
				oldQueryTriple = new QueryTriple(oldQueryTriple.getEntity1(),
						oldQueryTriple.getPredicate(), blankNode);
				queryTriples.add(oldQueryTriple);
				newQueryTriple = new QueryTriple(blankNode, entity + " "
						+ pattern, next_entity);

			}

			if (newQueryTriple != null)
				queryTriples.add(newQueryTriple);
			index = entities.indexOf(next_entity) + 1;
			entity = next_entity;
			oldQueryTriple = newQueryTriple;
			newQueryTriple = null;
		}

		if (queryTriples != null && !queryTriples.isEmpty()) {
			queryTriples.forEach(triple -> logger.info("Found QueryTriples: "
					+ triple.toString()));
			return queryTriples;
		} else {
			logger.warn("Did not find QueryTriples.");
			return null;
		}
	}

	private String getBlankNode() {
		return blankNodes[++blankNodeIndex];
	}

	private List<ArrayList<String>> getPowerSet(ArrayList<String> entities) {
		List<ArrayList<String>> list = new ArrayList<ArrayList<String>>();
		list.add(entities);

		for (String entity : entities) {
			ArrayList<String> shorterList = new ArrayList<String>(entities);

			if (shorterList.size() > 1) {
				shorterList.remove(entity);
				list.addAll(getPowerSet(shorterList));
			}
		}
		return list;
	}

	private int getPostionOfVariable(String query, List<String> entities)
			throws GenerateSparqlException {
		if (!CommonMethods.isVariable(query)) {
			throw new GenerateSparqlException(
					"Could not identify variable in query '" + query
					+ "'. No Variable available.");
		}

		// check if variable is before an entity
		for (int i = 0; i < entities.size(); i++) {
			String text = query.split(entities.get(i))[0];
			if (CommonMethods.isVariable(text)) {
				// entities.add(i, "VARIABLE");
				return i;
			}
		}

		// check if variable is after the last entity
		String text = query.split(entities.get(entities.size() - 1))[1];
		if (CommonMethods.isVariable(text)) {
			// entities.add("VARIABLE");
			return entities.size();
		}

		throw new GenerateSparqlException(
				"Could not identify variable in query '" + query
				+ "'. No Variable found.");
	}

	private String getPatternBetweenEntities(String text, String entity,
			String next_entity) {

		int pattern_start = 0;
		if (text.indexOf(entity) >= 0) {
			pattern_start = text.indexOf(entity) + entity.length();
		}
		int pattern_end = 0;
		String text_rest = text;
		int tokens = 0;
		while(pattern_start > pattern_end) {
			if (text.indexOf(next_entity) >= 0) {
				pattern_end = text_rest.indexOf(next_entity);
			}	
			tokens += pattern_end + 1;
			text_rest = text_rest.substring(pattern_end + 1);
			pattern_end = tokens - 1;
		}

		String pattern = text.substring(pattern_start, pattern_end).trim();
		return pattern;
	}

}
