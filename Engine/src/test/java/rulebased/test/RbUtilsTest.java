package rulebased.test;

import java.io.IOException;

import inputmanagement.InputManager;
import inputmanagement.impl.InputManagerImpl;

import org.junit.Before;
import org.junit.Test;

import rulebased.RbQuestionAnalyzer;
import rulebased.RbUtils;
import static org.junit.Assert.*;

public class RbUtilsTest {

	@Before
	public void prepare() {
		// initializes the logger, nothing more
		try {
			@SuppressWarnings("unused")
			// TODO delete?
			InputManager manager = new InputManagerImpl("", "", null);
		} catch (IOException e) {
			e.printStackTrace();
			fail("Exception thrown.");
		}
	}

	@Test
	public void test1() {
		RbUtils parser = new RbUtils();
		String out = parser
				.namedEntityRecognition("Good afternoon Rajat Raina, how are you today?");
		System.out.println(out);

	}

	@Test
	public void test2() {
		RbQuestionAnalyzer analyzer = new RbQuestionAnalyzer();
		String out = analyzer.parseDependencyTree(
				"Who are the parents of the wife of Juan Carlos I ?")
				.toString();
		System.out.println(out);
	}

	@Test
	public void test3() {
		RbQuestionAnalyzer analyzer = new RbQuestionAnalyzer();
		String out = analyzer.parseDependencyTree(
				"What is the ICD11 definition of cancer?").toString();
		System.out.println(out);
	}
}
