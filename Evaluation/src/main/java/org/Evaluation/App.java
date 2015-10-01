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
	private HashMap<String, Integer> evaluationResults;
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
		configManager.loadProperties();
		logger = configManager.getLogger();

		evaluationResults = new HashMap<String, Integer>();
		try {
			testQuestions = loadTestQuestions(path_to_testset);
		} catch (Exception e) {
			logger.error("ERROR: ",e);
		}

		param = new HashMap<String, String>();
		param.put("directSparqlPossible", "true");
		
//		param.put("questionAnalyser", "rulebased");
//		
		param.put("resourceMapper", "luceneStandard");
		param.put("LuceneStandardMapper:BoostPerfectMatch", "true");
//		param.put("LuceneStandardMapper:Lemmatize", "true");
		param.put("LuceneStandardMapper:DivideByOccurrence", "true");
//		param.put("considerRelationEnvironment", false);
		param.put("findEntityAndClass", "true");
		param.put("RelationManagerSimilarity", "Levenshtein");
		
		param.put("resourceMapper", "naive");
		param.put("NaiveMapper:windows", "3");
		param.put("NaiveMapper:threshold", "0.5");
		param.put("NaiveMapper:filter", "true");
		
		
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
			for (SparqlCandidate candidate : sparqlList) {
				List<String> result = manager.executeSparql(candidate.getSparqlQuery());
				candidate.addSolution( result );
			}
			int pos = getPositionOfCorrectSparql(sparqlList, testQuestion.getCorrectAnswers());
			evaluationResults.put(number + ": " + testQuestion.getNaturalQuestion(), pos);
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
	private int getPositionOfCorrectSparql(List<SparqlCandidate> sparqlList, List<String> correctAnswers) {
		int positionOfCorrectSparqlCandidate = Integer.MAX_VALUE;
		String result = "";

		for (int i = 0; i < sparqlList.size(); i++) {
			List<String> solutions = sparqlList.get(i).getSolutions();
			int postitionOfCorrectAnswer = Integer.MAX_VALUE;
			for (int j = 0; j < solutions.size(); j++) {
				for (String corAnswer : correctAnswers ) {
					if (solutions.get(j).equalsIgnoreCase(corAnswer)) {
						logger.info("found correct answer at position " + (j + 1) + " of SPARQL query #" + (i + 1));

						// only return the best found position
						if (i + 1 < positionOfCorrectSparqlCandidate) positionOfCorrectSparqlCandidate = i + 1;
						if (j + 1 < postitionOfCorrectAnswer) postitionOfCorrectAnswer = j + 1;
					}
				}
			}
		}
		if (positionOfCorrectSparqlCandidate == Integer.MAX_VALUE) logger.info("could not find any correct answers");
		return positionOfCorrectSparqlCandidate;
	}


	private void printEvaluation(HashMap<String, Integer> evaluationResults) {
		int[] correctResultsDistrinution = new int[12];

		logger.warn("");
		logger.warn("-------------------------------------------------");
		logger.warn("Evaluation:");
		logger.warn("-------------------------------------------------");

		Iterator<Entry<String, Integer>> iterator = evaluationResults.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, Integer> entry = iterator.next();
			if (entry.getValue() < Integer.MAX_VALUE) {
				logger.warn("Query '" + entry.getKey() + "' has correct SPARQL at position " + entry.getValue());
				if (entry.getValue() < 10) {
					correctResultsDistrinution[entry.getValue()] += 1;
				} else {
					correctResultsDistrinution[11] += 1;
				}
			} else {
				logger.warn("Query '" + entry.getKey() + "' has no answer.");
				correctResultsDistrinution[0] += 1;
			}
		}

		// also show the distribution of top 10 positions
		for (int i = 1; i <= 10; i++) {
			logger.warn("Number of Test Questions with correct SPARQL at position " + i + ": " + correctResultsDistrinution[i] );
		}
		logger.warn("Number of Test Questions with correct SPARQL at position above 10: " + correctResultsDistrinution[11] );
		logger.warn("Number of Test Questions with NO correct SPARQL: " + correctResultsDistrinution[0] );
		logger.warn("-------------------------------------------------");
		logger.warn("");
	}

}
