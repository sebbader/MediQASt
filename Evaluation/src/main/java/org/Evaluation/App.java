package org.Evaluation;

import inputmanagement.candidates.impl.SparqlCandidate;
import inputmanagement.impl.GenerateSparqlException;
import inputmanagement.impl.InputManagerImpl;

import java.io.File;
import java.io.IOException;
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
public class App 
{
	private Logger logger;
	private InputManagerImpl manager;
	private HashMap<String, EvaluationResult> evaluationResults;
	private List<TestQuestion> testQuestions;
	private HashMap<String, String> param;

	String endpoint = "http://aifb-ls3-vm8.aifb.kit.edu:8890/sparql";
	//	String endpoint = "http://localhost:8890/sparql";

	//	private String path_to_testset = "TestQuestions/testquestions.xml";
	private String path_to_testset = "TestQuestions/testquestions2.xml";

	public static void main( String[] args ) throws NumberFormatException, IOException
	{
		App app = new App();
		if (args.length > 0) {
			app.startEvaluationOfQuestion(Integer.parseInt( args[0] ) );
		} else {
			app.startEvaluationOfAllQuestions();
		}
	}

	public App() throws IOException {
		ConfigManager configManager = new ConfigManagerImpl();
		ConfigManagerImpl.setLogLevel(Level.DEBUG);
		configManager.loadProperties();
		logger = configManager.getLogger();

		evaluationResults = new HashMap<String, EvaluationResult>();
		try {
			testQuestions = loadTestQuestions(path_to_testset);
		} catch (Exception e) {
			logger.error("ERROR: ",e);
		}

		param = new HashMap<String, String>();
		param.put("directSparqlPossible", "true");

		param.put("questionAnalyser", "rulebased");
		//		
		param.put("resourceMapper", "luceneStandard");
		param.put("LuceneStandardMapper:BoostPerfectMatch", "true");
		//		param.put("LuceneStandardMapper:Lemmatize", "true");
		param.put("LuceneStandardMapper:StopwordRemoval", "true");
		param.put("LuceneStandardMapper:DivideByOccurrence", "true");
		//		param.put("considerRelationEnvironment", false);
		param.put("findEntityAndClass", "true");
		param.put("RelationManagerSimilarity", "Levenshtein");

		//		param.put("resourceMapper", "naive");
		//		param.put("NaiveMapper:windows", "3");
		//		param.put("NaiveMapper:threshold", "0.5");
		//		param.put("NaiveMapper:filter", "true");


		//		param.put("questionAnalyser", "RdfGroundedString");
		//		param.put("resourceMapper", "RdfGroundedString");
		//		param.put("findEntityAndClass", "true");
		//		param.put("LuceneStandardMapper:BoostPerfectMatch", "true");
		//		param.put("LuceneStandardMapper:DivideByOccurrence", "false");

		param.put("sparqlGenerator", "standard");
		param.put("NumberOfSparqlCandidates", "30");
		param.put("SparqlLimit", "100");
		param.put("numberOfTriplesPerSparql", "2");
		param.put("sparqlOption", "greedy");

		param.put("KeyWordQuestionThreshold", "0.5");
	}

	public void startEvaluationOfAllQuestions() throws IOException {

		printParameter(param);

		int counter = 0;
		for (TestQuestion testQuestion : testQuestions) {
			evaluateTestQuestion(testQuestion, counter);
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
		}
		logger.info("-------------------------------------------------------------");
		logger.info("");
		logger.info("");
	}

	public void startEvaluationOfQuestion(int number) throws IOException {
		evaluateTestQuestion(testQuestions.get(number - 1), number);
		printEvaluation(evaluationResults);
	}

	public void evaluateTestQuestion(TestQuestion testQuestion, int number) throws IOException {
		manager = new InputManagerImpl(endpoint, testQuestion.getNaturalQuestion(), param);
		logger.info("");
		logger.info("");
		logger.info("-------------------------------------------------------------");
		logger.info("Evaluate query #" + number + ": " + testQuestion.getNaturalQuestion());
		logger.info("-------------------------------------------------------------");

		try {
			List<SparqlCandidate> sparqlList = manager.generateSparql();
			List<String> results = new ArrayList<String>();
//			for (SparqlCandidate candidate : sparqlList) {
//				List<String> result = manager.executeSparql(candidate.getSparqlQuery());
//				candidate.addSolution( result );
//				results.addAll(result);
//			}
			
			results = manager.executeSparqlSet(sparqlList);
			logger.info("----------------------------------");
			logger.info("received results:");
			results.forEach(result -> logger.info(result));
			logger.info("----------------------------------");
			
			List<String> true_positives = getTruePositives(results, testQuestion.getCorrectAnswers());

			EvaluationResult evaluationResult = new EvaluationResult(true_positives, results, testQuestion.getCorrectAnswers());
			evaluationResults.put(number + ": " + testQuestion.getNaturalQuestion(), evaluationResult);
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
				if (results.get(j).equalsIgnoreCase(corAnswer)) {
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
		int[] correctResultsDistrinution = new int[12];

		logger.warn("");
		logger.warn("-------------------------------------------------");
		logger.warn("Evaluation:");
		logger.warn("-------------------------------------------------");

		Iterator<Entry<String, EvaluationResult>> iterator = evaluationResults.entrySet().iterator();
		List<Double> precisions = new ArrayList<Double>();
		List<Double> recalls = new ArrayList<Double>();
		List<Double> fMeasures = new ArrayList<Double>();
		while (iterator.hasNext()) {
			Entry<String, EvaluationResult> entry = iterator.next();

			double precision = entry.getValue().getPrecision();
			double recall = entry.getValue().getRecall();
			double fMeasure = entry.getValue().getFMeasure();

			// results for each test question
			precisions.add(precision);
			recalls.add(recall);
			fMeasures.add(fMeasure);
			logger.warn(entry.getKey() + " has result:");
			logger.warn("Precision " + precision + " | Recall " + recall + " | F-Measure " + fMeasure);
		}

		// return the average results
		double avg_precision = getAverage(precisions);
		double avg_recall = getAverage(recalls);
		double avg_fMeasure = getAverage(fMeasures);

		logger.warn("-------------------------------------------------");
		logger.warn("Average results:");
		logger.warn("Precision " + avg_precision + " | Recall " + avg_recall + " | F-Measure " + avg_fMeasure);
		logger.warn("-------------------------------------------------");
		logger.warn("");
	}

	private double getAverage(List<Double> list) {
		double sum = 0;
		for (Double value : list) {
			sum += value;
		}
		return sum / ((double) list.size());
	}

}
