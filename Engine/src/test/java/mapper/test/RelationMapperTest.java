package mapper.test;

import inputmanagement.candidates.impl.EntityCandidate;
import inputmanagement.candidates.impl.RelationCandidate;
import inputmanagement.impl.GenerateSparqlException;
import inputmanagement.impl.InputManagerImpl;

import java.io.IOException;
import java.util.List;

import mapper.EntityMapper;
import mapper.RelationMapper;
import mapper.impl.EntityMapperImpl;
import mapper.impl.RelationMapperImpl;

import org.junit.Before;
import org.junit.Test;

public class RelationMapperTest {
	
	private List<EntityCandidate> candidates1;
	private List<EntityCandidate> candidates2;

	@Before
	public void prepare() throws IOException {
		InputManagerImpl manager = new InputManagerImpl("http://localhost:8890/sparql", "", null);

		EntityMapper mapper = new EntityMapperImpl(manager);
		candidates1 = mapper.getEntityCandidates("O._Carl_Simonton"); // TODO
																		// add
																		// topNvalue
																		// to
																		// method
		candidates2 = mapper.getEntityCandidates("medicine");

	}

	@Test
	public void test() throws GenerateSparqlException, IOException {
		InputManagerImpl manager = new InputManagerImpl("http://localhost:8890/sparql", "", null);

		RelationMapper mapper = new RelationMapperImpl(manager);
		List<RelationCandidate> candidates = mapper.findRelationCandidates(
				"has preferred label", candidates1, candidates2);

		for (RelationCandidate can : candidates) {
			System.out.print(can.getUri());
			System.out.print(" | ");
			System.out.println(can.getScore());
		}
	}
}
