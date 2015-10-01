package lucene.test;

import static org.junit.Assert.*;

import java.io.IOException;

import inputmanagement.impl.InputManagerImpl;
import lucene.MyStemmer;

import org.junit.Before;
import org.junit.Test;

public class MyStemmerTest {

	@Before
	public void prepare() throws IOException {
		@SuppressWarnings("unused")
		InputManagerImpl manager = new InputManagerImpl("http://localhost:8890/sparql", "", null);
	}

	@Test
	public void test() {
		MyStemmer stemmer = new MyStemmer();
		String result = stemmer
				.doRemoveStopwordsStemming("This is a stupid test phrase. With plurals!");
		String expectedResult = "stupid test phrase plural";
		assertTrue(result.equals(expectedResult));
	}

}
