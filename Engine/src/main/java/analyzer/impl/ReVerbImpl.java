package analyzer.impl;

import inputmanagement.impl.QueryTriple;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import analyzer.ReVerb;
import edu.washington.cs.knowitall.extractor.ReVerbExtractor;
import edu.washington.cs.knowitall.extractor.conf.ConfidenceFunction;
import edu.washington.cs.knowitall.extractor.conf.ReVerbOpenNlpConfFunction;
import edu.washington.cs.knowitall.nlp.ChunkedSentence;
import edu.washington.cs.knowitall.nlp.OpenNlpSentenceChunker;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedBinaryExtraction;

public class ReVerbImpl implements ReVerb {

	private Logger logger;

	private static ReVerbExtractor reverb;
	private static ConfidenceFunction confFunc;
	private static OpenNlpSentenceChunker chunker;

	public ReVerbImpl(Logger logger) {
		this.logger = logger;

		try {
			if (reverb == null) reverb = new ReVerbExtractor();
			if (confFunc == null) confFunc = new ReVerbOpenNlpConfFunction();
			if (chunker == null) chunker = new OpenNlpSentenceChunker();
		} catch (IOException e) {
			logger.error("Could not open necessary Objects for ReVerbAnalyzer.", e);
		}
	}

	public List<QueryTriple> run(String inputQuestion) {
		List<QueryTriple> queryTriples = new ArrayList<QueryTriple>();

		ChunkedSentence sentence = chunker.chunkSentence(inputQuestion);

		// (token, tag, chunk-tag) for each word
		for (int i = 0; i < sentence.getLength(); i++) {
			String token = sentence.getToken(i);
			String posTag = sentence.getPosTag(i);
			String chunkTag = sentence.getChunkTag(i);
			logger.info("Word " + i + ": " + token + " " + posTag + " "
					+ chunkTag);
		}

		// Prints out extractions from the sentence.
		Iterable<ChunkedBinaryExtraction> extractions = reverb.extract(sentence);
		if (!extractions.iterator().hasNext()) {
			logger.warn("ReVerb did not find any structure.");
		}
		for (ChunkedBinaryExtraction extr : extractions) {
			double conf = confFunc.getConf(extr);
			String arg1 = extr.getArgument1().toString();
			String relation = extr.getRelation().toString();
			String arg2 = extr.getArgument2().toString();
			
			logger.info("arg1: " + arg1);
			logger.info("relation: " + relation);
			logger.info("arg2: " + arg2);
			logger.info("confidence: " + conf);
			
			QueryTriple queryTriple = new QueryTriple(arg1, relation, arg2);
			queryTriple.setScore(conf);
			logger.info("Found QueryTriple: " + queryTriple.toString());
			queryTriples.add(queryTriple);
		}

		for (QueryTriple queryTriple1 : queryTriples) {
			for (QueryTriple queryTriple2 : queryTriples) {
				if (queryTriple1.equals(queryTriple2)) continue;

				if (queryTriple1.getEntity1().equalsIgnoreCase(queryTriple2.getEntity1())) {
					String var = "?" + queryTriple1.getEntity1().replace(" ", "");
					queryTriple1.setEntity1(var);
					queryTriple2.setEntity1(var);
				} else if (queryTriple1.getEntity1().equalsIgnoreCase(queryTriple2.getEntity2())) {
					String var = "?" + queryTriple1.getEntity1().replace(" ", "");
					queryTriple1.setEntity1(var);
					queryTriple2.setEntity2(var);
				} else if (queryTriple1.getEntity2().equalsIgnoreCase(queryTriple2.getEntity1())) {
					String var = "?" + queryTriple2.getEntity2().replace(" ", "");
					queryTriple1.setEntity2(var);
					queryTriple2.setEntity1(var);
				} else if (queryTriple1.getEntity2().equalsIgnoreCase(queryTriple2.getEntity2())) {
					String var = "?" + queryTriple2.getEntity2().replace(" ", "");
					queryTriple1.setEntity2(var);
					queryTriple2.setEntity2(var);
				}
			}
		}

		return queryTriples;
	}

}
