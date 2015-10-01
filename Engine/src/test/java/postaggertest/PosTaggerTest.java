package postaggertest;

import java.util.List;

import org.junit.Test;

import postagger.PosTagger;
import postagger.impl.PosTaggerImpl;

public class PosTaggerTest {

	@Test
	public void test() {
		String test = "This is a test sentence with Barack Obama as the main enitity.";

		PosTagger posTagger = new PosTaggerImpl();
		List<String> entities = posTagger.getEntities(test);

		System.out.println("Found entities by POS-Tagger:");
		for (String entity : entities) {
			System.out.println(entity);
		}
	}

}
