package inputmanagement.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import inputmanagement.SparqlGenerator;
import inputmanagement.candidates.impl.EntityCandidate;
import inputmanagement.candidates.impl.RelationCandidate;
import inputmanagement.candidates.impl.SparqlCandidate;
import inputmanagement.impl.InputManagerImpl;
import inputmanagement.impl.SparqlGeneratorImpl;

import org.junit.Test;

public class SparqlGeneratorTest {

	@Test
	public void test() throws IOException {

		List<EntityCandidate> entity1Candidates = new ArrayList<EntityCandidate>();
		List<EntityCandidate> entity2Candidates = new ArrayList<EntityCandidate>();
		List<RelationCandidate> relationCandidates = new ArrayList<RelationCandidate>();

		entity1Candidates.add(new EntityCandidate(
				"http://dbpedia.org/resource/Category:Cancer", 10));
		entity2Candidates.add(new EntityCandidate("?x", 10));
		relationCandidates.add(new RelationCandidate(
				"http://www.w3.org/2004/02/skos/core#broader", 10));

		SparqlGenerator sparqlGenerator = new SparqlGeneratorImpl(
				new InputManagerImpl("", "", null));
		List<SparqlCandidate> sparql = sparqlGenerator.getSparqlCanidates(
				entity1Candidates, entity2Candidates, relationCandidates);

		for (SparqlCandidate candidate : sparql) {
			System.out.println(candidate.getSparqlQuery());
			System.out.println("score: " + candidate.getScore());
			System.out.println();
		}

		assertFalse(sparql.isEmpty());
	}

}
