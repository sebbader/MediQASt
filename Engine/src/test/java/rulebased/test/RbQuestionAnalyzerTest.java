package rulebased.test;

import java.io.IOException;

import inputmanagement.InputManager;
import inputmanagement.impl.InputManagerImpl;

import org.junit.Before;
import org.junit.Test;

import rulebased.RbQuestionAnalyzer;

public class RbQuestionAnalyzerTest {

	@Before
	public void prepare() throws IOException {
		// initializes the logger, nothing more
		@SuppressWarnings("unused")
		InputManager manager = new InputManagerImpl("", "", null);
	}

	@Test
	public void test1() {
		RbQuestionAnalyzer analyser = new RbQuestionAnalyzer();
		System.out.println(analyser
				.getQueryTriples("What is the ICD definition of cancer?"));
		System.out.println(analyser
				.getQueryTriples("What are types of cancer?"));
		System.out
				.println(analyser
						.getQueryTriples("Who are the parents of the wife of Juan Carlos I?"));
		System.out
				.println(analyser
						.getQueryTriples(
								"Which drug has the side effect hemorrhage and also the side effect with the id C0002792?")
						.toString());
	}

	@Test
	public void test2() {

	}
}
