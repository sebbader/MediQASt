package org.Evaluation;

import inputmanagement.candidates.impl.SparqlCandidate;
import inputmanagement.impl.GenerateSparqlException;
import inputmanagement.impl.InputManagerImpl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.netlib.util.doubleW;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import configuration.ConfigManager;
import configuration.impl.ConfigManagerImpl;

/**
 * 
 * @author Sebastian Bader (sebastian.bader@student.kit.edu)
 *
 */
public class DebugArea 
{
	private static Writer writer;

	private Logger logger;
	private InputManagerImpl manager;
	private HashMap<String, EvaluationResult> evaluationResults;
	private List<TestQuestion> testQuestions;

	private static String[] approaches = {"naive"};//, "reverb", "rdfgroundedstring", "rulebased"};
	static String[] bol = {"true", "false"};

	String endpoint = "http://aifb-ls3-vm8.aifb.kit.edu:8890/sparql";
	//	String endpoint = "http://localhost:8890/sparql";

	//	private String path_to_testset = "TestQuestions/testquestions.xml";
	private String path_to_testset = "TestQuestions/testquestions3.xml";

	public static void main( String[] args ) throws NumberFormatException, IOException
	{
		DebugArea app = new DebugArea(Level.INFO);
		HashMap<String, String> param = new HashMap<String, String>();

		for (String approach : approaches) {

			// standard parameter
			param = new HashMap<String, String>();
			param.put("directSparqlPossible", "true");

			// Analyzer
			param.put("questionAnalyzer", approach);
			param.put("findEntityAndClass", "true");

			param.put("RelationManagerSimilarity", "Levenshtein");
			//param.put("RelationManagerSimilarity", "WordNet");


			// Mapper
			if (approach.equals("rulebased") || approach.equals("reverb")) {
				param.put("resourceMapper", "luceneStandard");
			} else if ( approach.equals("naive")) {
				param.put("resourceMapper", "naive");
			} else if (approach.equals("rdfgroundedstring")) {
				param.put("resourceMapper", "rdfgroundedstring");
			}

			for (int i = 1; i <= 4; i++) {
//				if (i > 1 && !approach.equals("naive")) continue;
//				param.put("NaiveMapper:windows", Integer.toString(i));
				param.put("NaiveMapper:windows", "1");
				//param.put("NaiveMapper:windows", "3");
				//param.put("NaiveMapper:windows", "4");

				String[] doub = {"0.0", "0.2", "0.5"};
//				for (String s1 : doub) {
//					if (!s1.equals("0.0") && !approach.equals("naive")) continue;
//					param.put("NaiveMapper:threshold", s1);
					param.put("NaiveMapper:threshold", "0.0");
					//param.put("NaiveMapper:threshold", "0.5");


//					for (String s2 : bol) {
//						param.put("LuceneStandardMapper:AdjustFieldNorm", s2);
						param.put("LuceneStandardMapper:AdjustFieldNorm", "true");
						//param.put("LuceneStandardMapper:AdjustFieldNorm", "false");

//						for (String s3 : bol) {
//							param.put("LuceneStandardMapper:BoostPerfectMatch", s3);
							param.put("LuceneStandardMapper:BoostPerfectMatch", "true");
							//param.put("LuceneStandardMapper:BoostPerfectMatch", "false");

//							for (String s4 : bol) {
//								param.put("LuceneStandardMapper:Lemmatize", s4);
								param.put("LuceneStandardMapper:Lemmatize", "true");
								//param.put("LuceneStandardMapper:Lemmatize", "false");

//								for (String s5 : bol) {
//									param.put("LuceneStandardMapper:StopwordRemoval", s5);
									param.put("LuceneStandardMapper:StopwordRemoval", "true");
									//param.put("LuceneStandardMapper:StopwordRemoval", "false");

									String[] perfect = {"only", "no"};
//									for (String s6 : perfect) {
//										param.put("LuceneStandardMapper:SearchPerfect", s6);
										param.put("LuceneStandardMapper:SearchPerfect", "only");
										//param.put("LuceneStandardMapper:SearchPerfect", "no");

//										for (String s7 : bol) {
//											param.put("LuceneStandardMapper:DivideByOccurrence", s7);
											param.put("LuceneStandardMapper:DivideByOccurrence", "true");
											//param.put("LuceneStandardMapper:DivideByOccurrence", "false");

//											for (String s8 : bol) {
//												param.put("LuceneStandardMapper:FuzzySearch", s8);
												param.put("LuceneStandardMapper:FuzzySearch", "true");
												//param.put("LuceneStandardMapper:FuzzySearch", "false");

//												for (int j = 1; j <= 2; j++) {
//													if (s8.equalsIgnoreCase("false")) continue;	
//													param.put("LuceneStandardMapper:FuzzyParam", Integer.toString(j));
													param.put("LuceneStandardMapper:FuzzyParam", "1");
//													param.put("LuceneStandardMapper:FuzzyParam", "2");



													// SPARQL Generator
													param.put("sparqlGenerator", "standard");
													String[] can = {"10", "50", "1000"};
//													for (String s9 : can) {
//														param.put("NumberOfSparqlCandidates", s9);
														param.put("NumberOfSparqlCandidates", "10");
//														param.put("NumberOfSparqlCandidates", "1000");
													
//														for (String s10 : can) {
//															param.put("SparqlLimit", s10);
															param.put("SparqlLimit", "10");
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
														}
													}
//												}
//											}
//										}
//									}
//								}
//							}
//						}
//					}
//				}
//			}
//		}

		writer.close();
	}


	public DebugArea(Level logLevel) throws IOException {
		ConfigManager configManager = new ConfigManagerImpl();
		ConfigManagerImpl.setLogLevel(logLevel);
		configManager.loadProperties();
		logger = configManager.getLogger();

		FileOutputStream fos = new FileOutputStream("eval_results.csv");
		OutputStreamWriter w = new OutputStreamWriter(fos, "UTF-8");
		writer = new BufferedWriter(w);

		evaluationResults = new HashMap<String, EvaluationResult>();
		try {
			testQuestions = loadTestQuestions(path_to_testset);
		} catch (Exception e) {
			logger.error("ERROR: ",e);
		}


	}


	public void startEvaluationOfAllQuestions(HashMap<String, String> param) throws IOException {
		printParameter(param);

		int counter = 0;
		for (TestQuestion testQuestion : testQuestions) {
			evaluateTestQuestion(testQuestion, counter, param);
			counter++;
		}

		printEvaluation(evaluationResults);
	}


	private void printParameter(HashMap<String, String> parameter) {
		logger.info("");
		logger.info("");
		logger.info("-------------------------------------------------------------");
		logger.info("Parameter Set: ");
		logger.info("-------------------------------------------------------------");

		Iterator<Entry<String, String>> iter = parameter.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<String, String> entry = iter.next();
			logger.info(entry.getKey() + " has value: " + entry.getValue());
			write(entry.getKey() + ": " + entry.getValue());
		}
		logger.info("-------------------------------------------------------------");
		logger.info("");
		logger.info("");
	}

	public void startEvaluationOfQuestion(int number, HashMap<String, String> param) throws IOException {
		evaluateTestQuestion(testQuestions.get(number - 1), number, param);
		printEvaluation(evaluationResults);
	}

	public void evaluateTestQuestion(TestQuestion testQuestion, int number, HashMap<String, String> param) throws IOException {
		long startTime = System.nanoTime(); 
		
		manager = new InputManagerImpl(endpoint, testQuestion.getNaturalQuestion(), param);
		logger.info("");
		logger.info("");
		logger.info("-------------------------------------------------------------");
		logger.info("Evaluate query #" + number + ": " + testQuestion.getNaturalQuestion());
		logger.info("-------------------------------------------------------------");

		try {
			List<SparqlCandidate> sparqlList = manager.generateSparql();
			List<String> results = new ArrayList<String>();
			//for (SparqlCandidate candidate : sparqlList) {
			//List<String> result = manager.executeSparql(candidate.getSparqlQuery());
			//candidate.addSolution( result );
			//results.addAll(result);
			//}
			long elapsedTime = System.nanoTime() - startTime;
			results = manager.executeSparqlSet(sparqlList);
			logger.info("----------------------------------");
			logger.info("received results:");
			results.forEach(result -> logger.info(result));
			logger.info("----------------------------------");

			List<String> true_positives = getTruePositives(results, testQuestion.getCorrectAnswers());

			EvaluationResult evaluationResult = new EvaluationResult(true_positives, results, elapsedTime, testQuestion.getCorrectAnswers());
			evaluationResults.put(number + ": " + testQuestion.getNaturalQuestion(), evaluationResult);



			//print to eval csv file
			write(number);
			write(evaluationResult.getFMeasure());
			write(evaluationResult.getPrecision());
			write(evaluationResult.getRecall());
		} catch (GenerateSparqlException e) {
			logger.error("ERROR: ", e);
		}

	}



	/**
	 * 
	 * @param path
	 * @return
	 * @throws XPathExpressionException
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	private static List<TestQuestion> loadTestQuestions(String path) throws XPathExpressionException, SAXException, IOException, ParserConfigurationException {
		List<TestQuestion> testQuestions = new ArrayList<TestQuestion>();

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(new File(path));
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();

		// get all test queries
		XPathExpression expr = xpath.compile("//question");
		NodeList nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
		for (int i = 0; i < nl.getLength(); i++) {
			Node testQuery = nl.item(i);

			// get the query
			expr = xpath.compile("query");
			String query = (String) expr.evaluate(testQuery, XPathConstants.STRING);

			// get a possible SPARQL solution
			expr = xpath.compile("sparql");
			String sparql = (String) expr.evaluate(testQuery, XPathConstants.STRING);

			// get all correct answers
			expr = xpath.compile("answer");
			NodeList answersNL = (NodeList) expr.evaluate(testQuery, XPathConstants.NODESET);
			List<String> answers = new ArrayList<String>();
			for (int j = 0; j < answersNL.getLength(); j++) {
				String answer = answersNL.item(j).getTextContent();
				answers.add(answer);
			}

			testQuestions.add(new TestQuestion(query, answers, sparql));
		}

		return testQuestions;
	}


	/**
	 * find the position of the correct Sparql query.
	 * The 'correct' query is defined as the one which returns the expected URI
	 * @param sparqlList
	 * @return
	 */
	private List<String> getTruePositives(List<String> results, List<String> correctAnswers) {
		List<String> truePositives = new ArrayList<String>();
		int counter = 0;


		for (int j = 0; j < results.size(); j++) {
			for (String corAnswer : correctAnswers ) {
				counter++;
				if (results.get(j).equals(corAnswer)) {
					logger.info("found correct answer at position #" + counter);
					logger.info("position " + (j + 1) + ":" + corAnswer);

					truePositives.add(results.get(j));
				}
			}
		}
		if (truePositives.isEmpty()) logger.warn("Could not find any correct answer!");
		return truePositives;
	}


	private void printEvaluation(HashMap<String, EvaluationResult> evaluationResults) {

		logger.warn("");
		logger.warn("-------------------------------------------------");
		logger.warn("Evaluation:");
		logger.warn("-------------------------------------------------");

		Iterator<Entry<String, EvaluationResult>> iterator = evaluationResults.entrySet().iterator();
		List<Double> precisions = new ArrayList<Double>();
		List<Double> recalls = new ArrayList<Double>();
		List<Double> fMeasures = new ArrayList<Double>();
		List<Long> durations = new ArrayList<Long>();
		while (iterator.hasNext()) {
			Entry<String, EvaluationResult> entry = iterator.next();

			double precision = entry.getValue().getPrecision();
			double recall = entry.getValue().getRecall();
			double fMeasure = entry.getValue().getFMeasure();

			// results for each test question
			precisions.add( precision);
			recalls.add(recall);
			fMeasures.add(fMeasure);
			logger.warn(entry.getKey() + " has result:");
			logger.warn("Precision " + precision + " | Recall " + recall + " | F-Measure " + fMeasure);
			
			long duration = entry.getValue().getElapsedTime();
			durations.add(duration);
			logger.warn("Elapsed Time " + duration);
		}

		// return the average results
		double avg_precision = getDoubleAverage(precisions);
		double avg_recall = getDoubleAverage(recalls);
		double avg_fMeasure = getDoubleAverage(fMeasures);
		long avg_duration = getLongAverage(durations);
		Double avg_duration_in_seconds = avg_duration /((double) 1000000000.0);

		logger.warn("-------------------------------------------------");
		logger.warn("Average results:");
		logger.warn("Precision " + avg_precision + " | Recall " + avg_recall + " | F-Measure " + avg_fMeasure + " | Elapsed Time " + avg_duration_in_seconds);
		logger.warn("-------------------------------------------------");
		logger.warn("");
		
		write("total results:");
		write(avg_fMeasure);
		write(avg_precision);
		write(avg_recall);
		writeln(avg_duration_in_seconds);
	}



	private long getLongAverage(List<Long> list) {
		long sum = 0;
		for (Long value : list) {
			sum += value;
		}
		return sum / ((long) list.size());
	}


	private double getDoubleAverage(List<Double> list) {
		double sum = 0;
		for (Double value : list) {
			if (!value.isNaN())
				sum += value.doubleValue();
		}
		return sum / ((double) list.size());
	}

	private void write(String text) {
		try {
			writer.append(text);
			writer.append(",");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void write(int number) {
		try {
			writer.append(Integer.toString(number));
			writer.append(",");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void write(double number) {
		try {
			writer.append(Double.toString(number));
			writer.append(",");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void writeln(String line) {
		try {
			writer.append(line);
			writer.append("\n");
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void writeln(double line) {
		try {
			writer.append(Double.toString(line));
			writer.append("\n");
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
