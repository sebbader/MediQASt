package inputmanagement.test;

import java.io.IOException;
import java.util.List;

import inputmanagement.candidates.impl.EntityCandidate;
import inputmanagement.impl.InputManagerImpl;
import mapper.EntityMapper;
import mapper.impl.EntityMapperImpl;

import org.junit.Test;

public class EntityMapperTest {

	@Test
	public void test() throws IOException {
		InputManagerImpl manager = new InputManagerImpl("http://localhost:8890/sparql", "", null);

		EntityMapper mapper = new EntityMapperImpl(manager);
		List<EntityCandidate> candidates = mapper
				.getEntityCandidates("<http://dbpedia.org/resource/Category:Cancer>");

		System.out.println(candidates.get(0));
	}

}
