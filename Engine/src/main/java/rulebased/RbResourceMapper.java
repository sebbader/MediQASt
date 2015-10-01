package rulebased;

import inputmanagement.InputManager;
import inputmanagement.candidates.Candidate;
import inputmanagement.candidates.impl.EntityCandidate;
import inputmanagement.candidates.impl.RelationCandidate;
import inputmanagement.impl.QueryTriple;
import inputmanagement.impl.InputManagerImpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.queryparser.classic.ParseException;
import org.netlib.util.booleanW;

import configuration.ConfigManager;
import configuration.impl.CommonMethods;
import configuration.impl.ConfigManagerImpl;
import lucene.LuceneSearcher;

public class RbResourceMapper {
	private enum SEARCH_CASES {
		entity1, predicate, entity2
	};

	private Logger logger;
	private InputManager inputManager;

	public RbResourceMapper(InputManager inputManager) {
		ConfigManager configManager = new ConfigManagerImpl();
		this.logger = configManager.getLogger();

		this.inputManager = inputManager;
	}

	public List<Candidate> findEntity1(QueryTriple triple) {
		List<Candidate> uris = findResources(triple.getEntity1(),
				SEARCH_CASES.entity1);
		return uris;
	}

	public List<Candidate> findRelation(QueryTriple triple) {
		List<Candidate> uris = findResources(triple.getPredicate(),
				SEARCH_CASES.predicate);
		return uris;
	}

	public List<Candidate> findEntity2(QueryTriple triple) {
		List<Candidate> uris = findResources(triple.getEntity2(),
				SEARCH_CASES.entity1);
		return uris;
	}

	private List<Candidate> findResources(String text, SEARCH_CASES status) {
		List<Candidate> uris = new ArrayList<Candidate>();
		List<EntityCandidate> entities = null;
		List<RelationCandidate> predicates = null;
		LuceneSearcher searcher = new LuceneSearcher();

		text = text.replaceAll("-[0-9]*", "");
		text = text.replaceAll("_", " ");

		try {
			switch (status) {
			case entity1:
				// check if the phrase implies a variable
				// if so, just return ?x
				if (CommonMethods.isVariable(text)) { // TODO better connecting
					EntityCandidate candidate = new EntityCandidate("?x", 10);
					uris.add(candidate);
					return uris;
				}

				// use the lucene index to find the resource
				entities = searcher.searchEntity(text, inputManager);
				break;
			case entity2:
				// check if the phrase implies a variable
				// if so, just return ?x
				if (CommonMethods.isVariable(text)) {
					EntityCandidate candidate = new EntityCandidate("?x", 10);
					uris.add(candidate);
					return uris;
				}

				// use the lucene index to find the resource
				entities = searcher.searchEntity(text, inputManager);
				break;
			case predicate:
				// check if the phrase implies a variable
				// if so, just return ?rel
				if (CommonMethods.isVariable(text)) {
					RelationCandidate candidate = new RelationCandidate("?rel",
							10);
					uris.add(candidate);
					return uris;
				}

				// use the lucene index to find the resource
				predicates = searcher.searchRelation(text, inputManager);
				break;
			default:
				break;
			}

			if (entities != null) {
				for (EntityCandidate candidate : entities) {
					uris.add(candidate);
				}
			} else if (predicates != null) {
				uris = new ArrayList<Candidate>();
				for (RelationCandidate candidate : predicates) {
					uris.add(candidate);
				}
			}

		} catch (IOException | ParseException e) {
			logger.error("Exception during entity detection. "
					+ "Could not identify entity/predicate " + text + ".");
		}

		return uris;
	}

}
