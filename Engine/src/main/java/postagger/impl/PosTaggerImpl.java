package postagger.impl;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import postagger.PosTagger;
import configuration.ConfigManager;
import configuration.impl.ConfigManagerImpl;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class PosTaggerImpl implements PosTagger {

	private Logger logger;
	private ConfigManager configManager;

	public PosTaggerImpl() {
		this.configManager = new ConfigManagerImpl();
		this.logger = configManager.getLogger();
	}

	@Override
	public ArrayList<String> getEntities(String query) {
		ArrayList<String> enitities = new ArrayList<String>();

		logger.debug("Find entities in '" + query + "'");

		List<List<HasWord>> sentences = MaxentTagger
				.tokenizeText(new StringReader(query));
		logger.debug("POS Tagged query: " + sentences);

		// Build pattern for mention search.
		String namedEntityRegex1 = "([^\\s]*(?:/(?:NN|NNS))\\s)+([0-9\\-]+(?:/CD)\\s)*";
		String namedEntityRegex2 = "([^\\s]*(?:/(?:JJ|JJS))\\s)+"
				+ namedEntityRegex1;
		String namedEntityRegex3 = namedEntityRegex1
				+ "of/IN\\s([^\\s]+/(?:[A-Z])\\s)*('s/POS\\s)*"
				+ namedEntityRegex1;
		String namedEntityRegex4 = "([^\\s]*(?:/(?:NNP|NNPS))\\s)+([0-9\\-]+(?:/CD)\\s)*";
		String namedEntityRegex5 = "([A-Z][a-z]*[^\\s]+(?:/(?:JJ|JJS))\\s)+"
				+ namedEntityRegex4;
		String namedEntityRegex6 = namedEntityRegex4
				+ "of/IN\\s([^\\s]+/(?:[A-Z])\\s)*('s/POS\\s)*"
				+ namedEntityRegex4;

		String namedEntityRegex7 = namedEntityRegex1 + "('s|')(?:/POS)\\s"
				+ namedEntityRegex2;

		String namedEntityRegex8 = namedEntityRegex4 + "('s|')(?:/POS)\\s"
				+ namedEntityRegex4;

		String namedEntityRegex = "(" + namedEntityRegex8 + "|"
				+ namedEntityRegex7 + "|" + namedEntityRegex3 + "|"
				+ namedEntityRegex6 + "|" + namedEntityRegex2 + "|"
				+ namedEntityRegex5 + "|" + namedEntityRegex1 + "|"
				+ namedEntityRegex4 + ")";

		Pattern namedEntityPattern = Pattern.compile(namedEntityRegex);
		// Pattern posTagPattern = Pattern.compile("/([A-Z]{2,4})");

		// Additional patterns.
		String jjNNRegex = "([^\\s]+(?:/(?:JJ|JJS))\\s)+((?:[^\\s]+(?:/(?:NN|NNS))\\s{0,1})+)[^/]";
		// String jjNNPOSRegex =
		// "(?:[^\\s]+(?:/(?:JJ|JJS))\\s)+((?:[^\\s]+(?:/(?:NN|NNS))\\s)+)(?:/(?:POS))";
		Pattern jjNNPattern = Pattern.compile(jjNNRegex);
		// Pattern jjNNPOSPattern = Pattern.compile(jjNNPOSRegex);

		MaxentTagger tagger = new MaxentTagger(configManager.getHome()
				+ "models/english-bidirectional-distsim.tagger");

		// Process each sentence.
		for (int i = 0; i < sentences.size(); i++) {

			List<HasWord> sentence = sentences.get(i);

			// Tag each sentence.
			List<TaggedWord> tSentence = tagger.tagSentence(sentence);

			String taggedSentence = Sentence.listToString(tSentence, false);

			Matcher namedEntityMatcher = namedEntityPattern
					.matcher(taggedSentence);

			while (namedEntityMatcher.find()) {

				String entity = namedEntityMatcher.group(0);

				// Matcher posTagMatcher = posTagPattern.matcher(entity);

				// String posTag = "";
				//
				// if(posTagMatcher.find()){
				// posTag = posTagMatcher.group(1);
				// }

				entity = entity.replaceAll("/[A-Z]{2,4}", "").trim();

				logger.debug("Found entity: '" + entity + "'");
				enitities.add(entity);

				// Look for JJ NN|NNS and add JJ as a mention if it has first
				// capital letter and add NN|NNS as a mention.
				Matcher additionalMatcher = jjNNPattern.matcher(entity);

				while (additionalMatcher.find()) {

					for (int z = 1; z <= additionalMatcher.groupCount(); z++) {
						// Group 1: JJ
						// Group 2: NN

						String entity2 = additionalMatcher.group(z);

						if (z == 1) {
							// Check the first letter must be a capital letter
							// if pos tag is JJ.
							if (!entity2.substring(0, 1).equals(
									entity2.substring(0, 1).toUpperCase())) {
								continue;
							}
						}

						// posTagMatcher = posTagPattern.matcher(entity2);

						// posTag = "";
						//
						// if(posTagMatcher.find()){
						// posTag = posTagMatcher.group(1);
						// }

						entity2 = entity2.replaceAll("/[A-Z]{2,4}", "").trim();
						logger.debug("Found entity by AdditionalMatcher: '"
								+ entity2 + "'");
						enitities.add(entity2);
					}
				}
			}

		}

		return enitities;
	}
}
