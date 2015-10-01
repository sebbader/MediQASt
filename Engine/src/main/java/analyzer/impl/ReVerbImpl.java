package analyzer.impl;

import java.io.IOException;

import org.apache.log4j.Logger;

import analyzer.ReVerb;
import edu.washington.cs.knowitall.extractor.ReVerbExtractor;
import edu.washington.cs.knowitall.extractor.conf.ConfidenceFunction;
import edu.washington.cs.knowitall.extractor.conf.ReVerbOpenNlpConfFunction;
import edu.washington.cs.knowitall.nlp.ChunkedSentence;
import edu.washington.cs.knowitall.nlp.OpenNlpSentenceChunker;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedBinaryExtraction;

public class ReVerbImpl implements ReVerb {

	private String arg1;
	private String relation;
	private String arg2;

	private Logger logger;

	public ReVerbImpl(Logger logger) {
		this.logger = logger;
	}

	public void run(String inputQuestion) throws IOException {
		// TODO Auto-generated method stub
		OpenNlpSentenceChunker chunker = new OpenNlpSentenceChunker();
		ChunkedSentence sentence = chunker.chunkSentence(inputQuestion);

		// (token, tag, chunk-tag) for each word
		for (int i = 0; i < sentence.getLength(); i++) {
			String token = sentence.getToken(i);
			String posTag = sentence.getPosTag(i);
			String chunkTag = sentence.getChunkTag(i);
			logger.info("Word " + i + ": " + token + " " + posTag + " "
					+ chunkTag);
		}

		ReVerbExtractor reverb = new ReVerbExtractor();
		ConfidenceFunction confFunc;

		confFunc = new ReVerbOpenNlpConfFunction();
		double maxConfidence = 0.0;
		arg1 = "";
		relation = "";
		arg2 = "";
		for (ChunkedBinaryExtraction extr : reverb.extract(sentence)) {
			double conf = confFunc.getConf(extr);
			if (conf > maxConfidence) {
				maxConfidence = conf;
				arg1 = extr.getArgument1().toString();
				relation = extr.getRelation().toString();
				arg2 = extr.getArgument2().toString();
			}
		}
		logger.info("Arg1=" + arg1);
		logger.info("Rel=" + relation);
		logger.info("Arg2=" + arg2);
		logger.info("Conf=" + maxConfidence);
	}

	public String getArg1() {
		return arg1;
	}

	public String getArg2() {
		return arg2;
	}

	public String getRelation() {
		return relation;
	}

}
