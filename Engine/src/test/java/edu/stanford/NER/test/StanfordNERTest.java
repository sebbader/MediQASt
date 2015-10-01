package edu.stanford.NER.test;

import inputmanagement.InputManager;
import inputmanagement.impl.InputManagerImpl;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import edu.stanford.Ner.NerClassifier;

public class StanfordNERTest {

	@Before
	public void prepare() throws IOException {
		@SuppressWarnings("unused")
		InputManager inputManager = new InputManagerImpl("", "", null);
	}

	@Test
	public void test() throws Exception {
		// String text = "What is athlete's foot?";
		String text = "Good afternoon Rajat Raina, how are you today?";
		// String text = "Who was Mr. Hirschfeld?";
		NerClassifier classifier = new NerClassifier();
		List<String> terms = classifier.recognizeEntities(text);

		Iterator<String> iter = terms.iterator();
		while (iter.hasNext()) {
			String entry = iter.next();
			System.out.println(entry);
		}
	}

}
