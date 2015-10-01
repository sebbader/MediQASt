package naive.test;

import inputmanagement.InputManager;
import inputmanagement.impl.GenerateSparqlException;
import inputmanagement.impl.InputManagerImpl;
import inputmanagement.impl.QueryTriple;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import naivemapper.NaiveMapper;
import naivemapper.impl.NaiveMapperImpl;

import org.junit.Test;

import static org.junit.Assert.*;

public class NaiveSearcherTest {

	@Test
	public void test() throws GenerateSparqlException {
		String question = "Which drug can have the side effect amnesia?";

		HashMap<String, String> parameter = new HashMap<String, String>();
		parameter.put("BruteForceMapper:windows", "3");
		InputManager manager;
		try {
			manager = new InputManagerImpl("http://aifb-ls3-vm8.aifb.kit.edu:8890/sparql", question, parameter);


			NaiveMapper searcher = new NaiveMapperImpl(manager);
			List<ArrayList<QueryTriple>> results = searcher
					.getQueryTriples(question);

			System.out.println();
			System.out.println("Found QueryTriples for '" + question + "':");
			for (ArrayList<QueryTriple> foundTriples : results) {
				System.out.println("Found Query Triple Set:");

				for (QueryTriple foundTriple : foundTriples) {
					System.out.println(foundTriple);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("Exception thrown");
		}
		
	}


}
