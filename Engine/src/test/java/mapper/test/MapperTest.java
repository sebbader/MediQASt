package mapper.test;

import inputmanagement.candidates.impl.RelationCandidate;
import inputmanagement.impl.InputManagerImpl;

import java.io.IOException;
import java.util.List;

import mapper.RelationMapper;
import mapper.impl.RelationMapperImpl;

import org.junit.Test;

public class MapperTest {

	/**
	 * Test the class src/mapper/impl/ClassMapperImpl.java
	 * @throws IOException 
	 */
	/*
	 * @Test public void classMapperTest() { String class_term = "drug";
	 * 
	 * Logger logger = Logger.getRootLogger(); ClassMapper mapper = new
	 * ClassMapperImpl(logger); List<EntityCandidate> result =
	 * mapper.getClassCandidates(class_term);
	 * 
	 * System.out.println("Classes similar to " + class_term); while
	 * (!result.isEmpty()) { EntityCandidate classCandidate = (EntityCandidate)
	 * result.remove(0); System.out.println("Class: " +
	 * classCandidate.toString()); } System.out.println("------"); }
	 */
	@Test
	public void relationMapperTest() throws IOException {
		String relation = " side effect ";

		RelationMapper relMapper = new RelationMapperImpl(new InputManagerImpl(
				"", "", null));
		List<RelationCandidate> result = relMapper
				.findRelationCandidatesWithLucene(relation);

		System.out.println("Relations similar to " + relation);
		while (!result.isEmpty()) {
			RelationCandidate relationCandidate = (RelationCandidate) result
					.remove(0);
			System.out.println("Class: " + relationCandidate.toString());
		}
		System.out.println("------");
	}

	/*
	 * @Test public void relationMapperTestWithEnvironment() { String relation =
	 * "type"; String entity = "acetylsalicylic acid";
	 * 
	 * HashMap<String, Object> parameter = new HashMap<String, Object>();
	 * parameter.put("RelationManagerSimilarity", "Levenshtein");
	 * 
	 * InputManager manager = new
	 * InputManagerImpl("http://aifb-ls3-vm8.aifb.kit.edu:8890/sparql", "",
	 * parameter); ConfigManager configManager = new ConfigManagerImpl(); Logger
	 * logger = configManager.getLogger();
	 * 
	 * EntityMapper entityMapper = new EntityMapperImpl(logger);
	 * List<EntityCandidate> entityCandidates =
	 * entityMapper.getEntityCandidates(entity);
	 * 
	 * RelationMapper relMapper = new RelationMapperImpl(logger, manager);
	 * List<RelationCandidate> result =
	 * relMapper.findRelationCandidatesFromEntityEnvironment(entityCandidates,
	 * relation);
	 * 
	 * System.out.println("Relations similar to " + relation); while
	 * (!result.isEmpty()) { RelationCandidate relationCandidate =
	 * (RelationCandidate) result.remove(0); System.out.println("Class: " +
	 * relationCandidate.toString()); } System.out.println("------"); }
	 */
}
