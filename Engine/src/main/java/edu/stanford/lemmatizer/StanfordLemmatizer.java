package edu.stanford.lemmatizer;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import configuration.ConfigManager;
import configuration.impl.ConfigManagerImpl;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.logging.RedwoodConfiguration;
import edu.stanford.nlp.util.logging.RedwoodConfiguration.Handlers;

/**
 * 
 * @author Sebastian Bader (minor changes)
 * @author Peter Natterer (original version)
 *
 */
public class StanfordLemmatizer {

	protected StanfordCoreNLP pipeline;
	private Logger logger;

	public StanfordLemmatizer() {
		ConfigManager configManager = new ConfigManagerImpl();
		this.logger = configManager.getLogger();
		// Create StanfordCoreNLP object properties, with POS tagging
		// (required for lemmatization), and lemmatization
		Properties props;
		props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, lemma");
		props.put("pos.model", configManager.getHome() + "models/english-bidirectional-distsim.tagger");
		logger.debug("StanfordLemmatizer properties: annotators: tokenize, ssplit, pos, lemma");
		logger.debug("StanfordLemmatizer pos.model: " + configManager.getHome() + "models/english-bidirectional-distsim.tagger");
		
		// StanfordCoreNLP loads a lot of models, so you probably
		// only want to do this once per execution

		// this is your print stream, store the reference
		PrintStream err = System.err;

		// now make all writes to the System.err stream silent
		System.setErr(new PrintStream(new OutputStream() {
			public void write(int b) {
			}
		}));
		this.pipeline = new StanfordCoreNLP(props);
		System.setErr(err);
	}

	public String lemmatize(String documentText) {

		String lemmatizedText = new String();

		// create an empty Annotation just with the given text
		Annotation document = new Annotation(documentText);

		// run all Annotators on this text
		this.pipeline.annotate(document);

		// Iterate over all of the sentences found
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		for (CoreMap sentence : sentences) {
			// Iterate over all tokens in a sentence
			for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
				// Retrieve and add the lemma for each word into the list of
				// lemmas
				lemmatizedText += token.get(LemmaAnnotation.class) 
//						+ "_"
//						+ token.get(PartOfSpeechAnnotation.class) 
						+ " "
						;
			}
		}
		
		logger.info("Converted '" + documentText + "' to '" + lemmatizedText + "' using StanfordLemmatizer.");
		
		return lemmatizedText;
	}
}