package inputmanagement.test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import inputmanagement.candidates.impl.SparqlCandidate;
import inputmanagement.impl.GenerateSparqlException;
import inputmanagement.impl.InputManagerImpl;

import org.junit.Test;

public class InputManagerTest {

	String endpoint = "http://aifb-ls3-vm8.aifb.kit.edu:8890/sparql";

	@Test
	public void test() throws GenerateSparqlException, IOException {
		// String test_question = "Asthma is the death cause of whom?";
		String test_question = "Which injury has the MEsH ID D002056?";

		HashMap<String, String> param = new HashMap<String, String>();
		param = new HashMap<String, String>();
		param.put("directSparqlPossible", "true");

		// param.put("questionAnalyser", "CASIA");
		//
		// param.put("resourceMapper", "luceneStandard");
		// param.put("considerRelationEnvironment", true);
		// param.put("findEntityAndClass", true);
		// param.put("RelationManagerSimilarity", "Levenshtein");

		// param.put("resourceMapper", "bruteForce");
		// param.put("BruteForceMapper:windows", 2);
		// param.put("BruteForceMapper:noRelation", true);

		param.put("sparqlGenerator", "standard");
		param.put("NumberOfSparqlCandidates", "30");
		param.put("sparqlOption", "greedy");

		param.put("KeyWordQuestionThreshold", "0.5");

		InputManagerImpl manager = new InputManagerImpl(endpoint, test_question, param);
		List<SparqlCandidate> candidates = manager.generateSparql();
		for (SparqlCandidate candidate : candidates) {
			System.out.println("---------------");
			System.out.println(candidate.getSparqlQuery());
			System.out.println("Score: " + candidate.getScore());
			System.out.println("---------------");
		}
	}

}
