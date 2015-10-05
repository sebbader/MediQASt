package rulebased.test;

import inputmanagement.candidates.Candidate;
import inputmanagement.impl.InputManagerImpl;
import inputmanagement.impl.QueryTriple;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import rulebased.RbResourceMapper;

public class RbResourceMapperTest {

	private List<QueryTriple> test_triples;

	@Before
	public void prepare() {
		test_triples = new ArrayList<QueryTriple>();
		test_triples.add(new QueryTriple("What-1", "the-3_ICD-4_definition-5",
				"cancer-7"));
		test_triples.add(new QueryTriple("What-1", "types-3", "cancer-5"));
		test_triples.add(new QueryTriple("Which-1_drug-2", "has-3",
				"the-4_side-5_effect-6_hemorrhage-7"));
		test_triples.add(new QueryTriple("Which-1_drug-2", "has-3",
				"?also-9_the-10_side-11_effect-12"));
		test_triples.add(new QueryTriple("?also-9_the-10_side-11_effect-12",
				"also-9_the-10_side-11_effect-12", "the-14_id-15_C0002792-16"));
	}

	@Test
	public void test() throws IOException {
		for (int i = 0; i < test_triples.size(); i++) {
			System.out.print("CasiaTriple: ");
			System.out.println(test_triples.get(i));
			System.out.println("");

			HashMap<String, String> parameter = new HashMap<String, String>();
			RbResourceMapper mapper = new RbResourceMapper(
					new InputManagerImpl("http://aifb-ls3-vm8.aifb.kit.edu:8890/sparql", "", parameter));

			List<Candidate> entity1 = mapper.findEntity1(test_triples.get(i));
			System.out.print("Entity1 Candidate List: ");
			System.out.println(entity1);

			List<Candidate> relation = mapper.findRelation(test_triples.get(i));
			System.out.print("Relation Candidate List: ");
			System.out.println(relation);

			List<Candidate> entity2 = mapper.findEntity2(test_triples.get(i));
			System.out.print("Entity2 Candidate List: ");
			System.out.println(entity2);
			System.out.println("--------------------------------------------");
		}
	}

}
