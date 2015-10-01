package edu.stanford.Ner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import configuration.ConfigManager;
import configuration.impl.ConfigManagerImpl;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;

public class NerClassifier {

	private final String serializedClassifier;
	private Logger logger;
	private ConfigManager configManager;

	public NerClassifier() {
		configManager = new ConfigManagerImpl();
		this.logger = configManager.getLogger();
		this.serializedClassifier = configManager.getHome()
				+ "StanfordNerClassifiers/english.all.3class.distsim.crf.ser.gz";
	}

	public List<String> recognizeEntities(String text) {
		List<String> terms = new ArrayList<String>();

		AbstractSequenceClassifier<CoreLabel> classifier;
		try {
			classifier = CRFClassifier.getClassifier(serializedClassifier);

			String words = classifier
					.classifyToString(text, "slashTags", false).replace("\r\n",
							"");

			String[] word_array = words.split(" ");
			String oldTerm = "";
			String oldTag = "O";
			for (String str : word_array) {
				String[] split = str.split("/");
				try {
					String term = split[0].toLowerCase();
					String tag = split[1];
					if (!tag.equalsIgnoreCase("O")) {
						if (tag.equalsIgnoreCase(oldTag)) {
							term = oldTerm + " " + term;
						} else {
							if (!oldTerm.isEmpty()
									&& !oldTerm.equalsIgnoreCase(" ")
									&& !oldTag.equalsIgnoreCase("O"))
								terms.add(oldTerm);
						}
					} else if (!oldTag.equalsIgnoreCase("O")) {
						terms.add(oldTerm);
					}
					oldTag = tag;
					oldTerm = term;
				} catch (ArrayIndexOutOfBoundsException e) {
					logger.info("Exception during named entity classification of '"
							+ text + "'. Try to continue...");
				}
			}
			// terms.add(oldTerm);

			return terms;

		} catch (ClassCastException | ClassNotFoundException | IOException e) {
			logger.error("Error during named entity classification of '" + text
					+ "'.", e);
			return Arrays.asList(text.split(" "));
		}
	}
}
