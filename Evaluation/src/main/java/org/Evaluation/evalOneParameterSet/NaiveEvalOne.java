package org.Evaluation.evalOneParameterSet;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import org.Evaluation.Evaluation;
import org.Evaluation.EvaluationResult;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import configuration.ConfigManager;
import configuration.impl.ConfigManagerImpl;

/**
 * 
 * @author Sebastian Bader (sebastian.bader@student.kit.edu)
 *
 */
public class NaiveEvalOne extends Evaluation
{

	public static void main( String[] args ) throws NumberFormatException, IOException
	{
		NaiveEvalOne app = new NaiveEvalOne(Level.INFO);
		HashMap<String, String> param = new HashMap<String, String>();

		// standard parameter
		param = new HashMap<String, String>();
		param.put("directSparqlPossible", "true");

		// Analyzer
		param.put("questionAnalyzer", "naive");
		param.put("findEntityAndClass", "true");

		param.put("RelationManagerSimilarity", "Levenshtein");
		//param.put("RelationManagerSimilarity", "WordNet");


		// Mapper
		param.put("resourceMapper", "naive");

		//param.put("NaiveMapper:windows", "2");
//		param.put("NaiveMapper:windows", "3");
		param.put("NaiveMapper:windows", "4");

		param.put("NaiveMapper:threshold", "0.0");
		//param.put("NaiveMapper:threshold", "0.5");

		param.put("NaiveAnalyzer:ValidateSteps", "1");
//		param.put("NaiveAnalyzer:ValidateSteps", "2");

		param.put("LuceneStandardMapper:Formula", "own");
//		param.put("LuceneStandardMapper:Formula", "lucene");

		param.put("LuceneStandardMapper:AdjustFieldNorm", "true");
		//param.put("LuceneStandardMapper:AdjustFieldNorm", "false");

		param.put("LuceneStandardMapper:BoostPerfectMatch", "true");
//		param.put("LuceneStandardMapper:BoostPerfectMatch", "false");

		//param.put("LuceneStandardMapper:Lemmatize", "true");
		param.put("LuceneStandardMapper:Lemmatize", "false");

		param.put("LuceneStandardMapper:StopwordRemoval", "true");
		//param.put("LuceneStandardMapper:StopwordRemoval", "false");

		//param.put("LuceneStandardMapper:SearchPerfect", "only");
		param.put("LuceneStandardMapper:SearchPerfect", "no");

		param.put("LuceneStandardMapper:DivideByOccurrence", "true");
		//param.put("LuceneStandardMapper:DivideByOccurrence", "false");

		param.put("LuceneStandardMapper:FuzzySearch", "true");
		//param.put("LuceneStandardMapper:FuzzySearch", "false");

//		param.put("LuceneStandardMapper:FuzzyParam", "1");
		param.put("LuceneStandardMapper:FuzzyParam", "2");

		param.put("LuceneStandardMapper:NumberOfHits", "20");


		// SPARQL Generator

		param.put("NumberOfSparqlCandidates", "20");
		param.put("SparqlLimit", "50");
		param.put("numberOfTriplesPerSparql", "2");
		param.put("sparqlOption", "greedy");
		param.put("KeyWordQuestionThreshold", "0.4");


		// start evaluation
		if (args.length == 1) {
			int testquestionnumber = Integer.parseInt( args[0] );
			app.startEvaluationOfQuestion(testquestionnumber, param);
		} else if (args.length == 4) {
			int testquestionnumber = Integer.parseInt( args[0] );
			String analyzer = args[1];
			param.put("questionAnalyzer", analyzer);
			String mapper = args[2];
			param.put("resourceMapper", mapper);
			String sparqlGenerator = args[3];
			param.put("sparqlGenerator", sparqlGenerator);
			app.startEvaluationOfQuestion(testquestionnumber, param);
			return;
		} else {
			app.startEvaluationOfAllQuestions(param);
		}

		writer.close();
	}


	public NaiveEvalOne(Level logLevel) throws IOException {
		ConfigManager configManager = new ConfigManagerImpl();
		ConfigManagerImpl.setLogLevel(logLevel);
		configManager.loadProperties();
		logger = configManager.getLogger();

		FileOutputStream fos = new FileOutputStream("eval_results_naive.csv");
		OutputStreamWriter w = new OutputStreamWriter(fos, "UTF-8");
		writer = new BufferedWriter(w);

		evaluationResults = new ArrayList<EvaluationResult>();
		try {
			testQuestions = loadTestQuestions(path_to_testset);
		} catch (Exception e) {
			logger.error("ERROR: ",e);
		}


	}

}
