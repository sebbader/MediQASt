package naivemapper.impl;

import inputmanagement.InputManager;
import inputmanagement.candidates.Candidate;
import inputmanagement.candidates.RdfCandidate;
import inputmanagement.candidates.RdfCandidateTypes;
import inputmanagement.candidates.impl.EntityCandidate;
import inputmanagement.candidates.impl.RelationCandidate;
import inputmanagement.impl.CustomComparator;
import inputmanagement.impl.GenerateSparqlException;
import inputmanagement.impl.InputManagerImpl;
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
import naiveanalyzer.impl.GraphAnalyzerImpl;
import naiveanalyzer.impl.GreedyAnalyzerImpl;
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

	@SuppressWarnings("unchecked")
	@Override
	public List<ArrayList<QueryTriple>> getQueryTriples(String question)
			throws GenerateSparqlException {

		String[] words = getWords(question);

		int window;
		try {
			window = Integer.parseInt(manager
					.getOption("NaiveMapper:windows"));
		} catch (GenerateSparqlException e) {
			window = 2;
			logger.error(
					"Could not find 'NaiveMapper:windows' parameter. Use default value 2.",
					e);
		}

		double threshold;
		try {
			threshold = Double.parseDouble(manager.getOption(
					"NaiveMapper:threshold").toString());
		} catch (GenerateSparqlException e) {
			threshold = 2.;
			logger.error(
					"Could not find 'NaiveMapper:threshold' parameter. Use default value 2.0 .",
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

		
		
		// Analyzer Part:
		candidates = (List<RdfCandidate>) CommonMethods.removeDuplicates(candidates);
		candidates.sort(new CustomComparator());
		
//		GreedyAnalyzerImpl analyzer = new GreedyAnalyzerImpl(manager);
//		List<ArrayList<QueryTriple>> queryTripleSet = analyzer.getQueryTriples(candidates, question);
		
		GraphAnalyzerImpl analyzer = new GraphAnalyzerImpl((InputManagerImpl) manager);
		List<ArrayList<QueryTriple>> queryTripleSet = analyzer.getQueryTriples(candidates, question);
		
		return queryTripleSet;
	}


	private List<RdfCandidate> findCandidate(String term,
			List<Integer> position, double threshold) {
		List<RdfCandidate> candidates = new ArrayList<RdfCandidate>();

		if (CommonMethods.isVariable(term)) {
			EntityCandidate candidate = new EntityCandidate("?variable", 100);
			candidate.setType(RdfCandidateTypes.VARIABLE);
			candidate.setPosition(position);
			candidates.add(candidate);
			return candidates;
		}

		
		List<EntityCandidate> classes = null;
		if (!manager.isActiveOption("NaiveMapper:noClass", true)) {
			
			ClassMapper classMapper = new ClassMapperImpl(manager);
			classes = classMapper.getClassCandidates(term);
			
			if (classes == null) classes = new ArrayList<EntityCandidate>();
			for (RdfCandidate candidate : classes) {
				if (candidate.getScore() >= threshold)
					candidates.add(candidate);
			}
			
		}

		
		List<EntityCandidate> entities = null;
		if (!manager.isActiveOption("NaiveMapper:noEntity", true)) {
			
			EntityMapper entityMapper = new EntityMapperImpl(manager);
			entities = entityMapper.getEntityCandidates(term);
			
			if (entities == null) entities = new ArrayList<EntityCandidate>();
			for (RdfCandidate candidate : entities) {
				if (candidate.getScore() >= threshold)
					candidates.add(candidate);
			}
			
		}

		
		List<RelationCandidate> relations = null;
		if (!manager.isActiveOption("NaiveMapper:noRelation", true)) {
			
			RelationMapper relationMapper = new RelationMapperImpl(manager);
			relations = relationMapper.getRelationCandidates(term);
			
			if (relations == null) relations = new ArrayList<RelationCandidate>();
			for (RdfCandidate candidate : relations) {
				if (candidate.getScore() >= threshold)
					candidates.add(candidate);
			}
			
		}

		candidates.forEach((candidate) -> candidate.setPosition(position));
		candidates.sort(new CustomComparator());

		
		
		if (candidates.size() > 0) {
			if (candidates.size() > 10) {
				return candidates.subList(0, 10);
			} else {
				return candidates;
			}
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
