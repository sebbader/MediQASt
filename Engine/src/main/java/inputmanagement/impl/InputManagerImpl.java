package inputmanagement.impl;

import inputmanagement.candidates.impl.RelationCandidate;
import inputmanagement.candidates.impl.SparqlCandidate;
import inputmanagement.InputManager;
import inputmanagement.QuestionTypes;
import inputmanagement.SparqlGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import lucene.LuceneMapper;
import naivemapper.NaiveMapper;
import naivemapper.impl.NaiveMapperImpl;

import org.apache.log4j.Logger;

import rdfgroundedstrings.RdfGroundedStringAnalyzer;
import rdfgroundedstrings.RdfGroundedStringMapper;
import rdfgroundedstrings.impl.RdfGroundedStringAnalyzerImpl;
import rdfgroundedstrings.impl.RdfGroundedStringMapperImpl;
import rulebased.RbQuestionAnalyzer;
import analyzer.ReVerb;
import analyzer.impl.ReVerbImpl;

import com.hp.hpl.jena.query.QueryException;
import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

import configuration.ConfigManager;
import configuration.impl.ConfigManagerImpl;
import connector.SPARQLEndpointConnector;
import connector.impl.SPARQLEndpointConnectorImpl;
import edu.stanford.lemmatizer.StanfordLemmatizer;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.washington.cs.knowitall.extractor.conf.ConfidenceFunctionException;

public class InputManagerImpl implements InputManager {

	private String inputQuestion;
	private String taggedInputQuestion;
	private String question_with_variable;
	private String tagged_question_with_variable;
	private Logger logger;
	private ConfigManager configManager;
	public MaxentTagger posTagger;
	public static SPARQLEndpointConnector endpointConnector;
	private StanfordLemmatizer lemmatizer;

	private HashMap<String, String> parameter;

	private QuestionTypes questionType;
	private List<SparqlCandidate> sparqlQueries = new ArrayList<SparqlCandidate>();
	private List<ArrayList<QueryTriple>> queryTripleSet = new ArrayList<ArrayList<QueryTriple>>();

	/**
	 * creates a new InputManager Object.
	 * 
	 * @param endpoint
	 *            uri where a SPARQL endpoint listens
	 * @param home
	 *            the home directory including all relevant files/indices/models
	 * @param inputQuestion
	 *            the user question in natural language
	 * @param parameter
	 *            Map of input parameters, controlling used methods/algorithms
	 *            etc.
	 * @throws IOException if reading of 'config.properties' causes issues
	 */
	public InputManagerImpl(String endpoint, String inputQuestion, HashMap<String, String> parameter) throws IOException {
		// load properties and initiate the logger
		configManager = new ConfigManagerImpl();
		configManager.loadProperties();
		logger = configManager.initLogger();

		logger.info("Start converting NL to SPARQL");

		this.inputQuestion = inputQuestion;
		endpointConnector = new SPARQLEndpointConnectorImpl(endpoint);
		logger.debug("Input query: " + inputQuestion + "\tSPARQL endpoint: "
				+ endpoint);

		// load the parameter list
		setParameter(parameter);
	}

	@Override
	public HashMap<String, String> getParameter() {
		return this.parameter;
	}

	public void setParameter(HashMap<String, String> param) {
		this.parameter = param;
	}

	@Override
	public List<SparqlCandidate> generateSparql()
			throws GenerateSparqlException {

		// ------------ Step A --------------------//
		// Preparation A.1: Check if the query is already a valid SPARQL query
		if (isActiveOption("directSparqlPossible", "true")) {
			// User inserted a valid SPARQL query, no further processing
			// necessary
			if (endpointConnector.isValidSparql(inputQuestion)) {
				logger.info("valid SPARQL query found - no preprocessing");
				sparqlQueries.add(new SparqlCandidate(inputQuestion, 10.0));
				logger.info("New SparqlQuery: '" + inputQuestion + "'");
				return sparqlQueries;
			}
		}

		// Preparation A.2: POS tagging
		if (posTagger == null)
			posTagger = new MaxentTagger(configManager.getHome()
					+ "models/english-left3words-distsim.tagger");
		taggedInputQuestion = posTagger.tagString(inputQuestion);
		logger.info("Stanford POS Tagger returns: " + taggedInputQuestion);

		// ------------ Step B --------------------//
		// Rule-based Question Analysis (like in CASIA)
		questionType = getQuestionType(taggedInputQuestion);
		logger.info("Query Type: " + questionType);
		if ((questionType != QuestionTypes.select_person)
				&& (questionType != QuestionTypes.select_thing)) {
			// throw new GenerateSparqlException("Categorized question as " +
			// questionType + ". Untill now only qeustions "
			// + "asking for people and things are possible.");
		}

		// ------------ Step C --------------------//
		// Question Analyzing
		List<QueryTriple> queryTriples = new ArrayList<QueryTriple>();

		try {

			if (isActiveOption("questionAnalyser", "ReVerb")) {
				// -- using ReVerb to split query into "arg1 rel arg2" format

				// replace question word with variable TODO: delete line?
				tagged_question_with_variable = replaceWHxxWithVariable(taggedInputQuestion);

				question_with_variable = replacePOSTags(tagged_question_with_variable);
				logger.info("Query before question analysis by ReVerb: '"
						+ inputQuestion + "'");

				// Looks on the classpath for the default model files.
				ReVerb r = new ReVerbImpl(logger);
				r.run(question_with_variable);
				String arg1 = r.getArg1();
				String relation = r.getRelation();
				String arg2 = r.getArg2();
				if (arg1 != null || relation != null || arg2 != null) {
					queryTriples.add(new QueryTriple(arg1, relation, arg2));
					logger.info("Found triple by ReVerb Question Analyser: "
							+ queryTriples);
				} else {
					logger.info("ReVerb Question Analyzer found no triples.");
				}
			}

			// --- C.2 Rule-based approach inspired by CASIA
			if (isActiveOption("questionAnalyser", "rulebased")) {
				RbQuestionAnalyzer analyzer = new RbQuestionAnalyzer();
				queryTriples.addAll(analyzer.getQueryTriples(inputQuestion));
				logger.info("Found triples by Rule-based Question Analyzer: "
						+ queryTriples);

			}

			// --- C.3 Using English Chain Rules to identify entities.
			// Everything between entities
			// which is not a variable (question term) is regarded as a
			// predicate
			if (isActiveOption("questionAnalyser", "RdfGroundedString")) {
				RdfGroundedStringAnalyzer analyzer = new RdfGroundedStringAnalyzerImpl();
				queryTripleSet.addAll(analyzer.getQueryTriples(inputQuestion));
				for (ArrayList<QueryTriple> triples : queryTripleSet) {
					logger.info("Found triple sets by RdfGroundedString Question Analyzer: ");
					triples.forEach(triple -> logger.info(triple));
				}
			}

			// //--- C.3: find known entities/relations directly by trying to
			// compare Strings to the
			// // stored labels using Lucene Searcher
			// if (isActiveOption("questionAnalyser", "StatistcMapper")) {
			// StatisticMapperImpl statistcMapper = new
			// StatisticMapperImpl(parameter);
			// queryTriples.addAll(statistcMapper.findCandidates(inputQuestion));
			// logger.info("Found triples by Statistic Mapper: " +
			// queryTriples);
			// }

			// replace "VARIABLE" with "?uri"
			queryTriples.forEach((queryTriple) -> queryTriple.clean());
			queryTripleSet.forEach(triples -> triples.forEach(triple -> triple
					.clean()));

			// -----Step D:
			// --- Entity and Relation Mapping to URIs

			// --- D.1: Using Naive Approach
			if (isActiveOption("resourceMapper", "naive")) {
				NaiveMapper mapper = new NaiveMapperImpl(this);
				queryTripleSet = mapper.getQueryTriples(inputQuestion);

				logger.info("Found URIs by NaiveMapper for '"
						+ inputQuestion + "':");
				for (List<QueryTriple> triples : queryTripleSet) {
					logger.info("Triple Set:");
					for (QueryTriple triple : triples) {
						logger.info(triple.getTripleWithCandidates());
					}
				}

			}

			// --- D.2: Using standard lucene
			if (isActiveOption("resourceMapper", "luceneStandard")) {
				LuceneMapper luceneMapper = new LuceneMapper(this);

				if (!queryTriples.isEmpty() && queryTripleSet.isEmpty()) {
					// map elements of the queryTriples list
					for (QueryTriple triple : queryTriples) {
						luceneMapper.mapQueryTriple(triple);
					}

				} else if (queryTriples.isEmpty() && !queryTripleSet.isEmpty()) {

					for (List<QueryTriple> triplesList : queryTripleSet) {
						for (QueryTriple triple : triplesList) {
							luceneMapper.mapQueryTriple(triple);
						}
					}

				} else if (queryTriples.isEmpty() && queryTripleSet.isEmpty()) {
					logger.error("No available QueryTriples for Lucene Standard Mapper.");
				} else if (!queryTriples.isEmpty() && !queryTripleSet.isEmpty()) {
					logger.error("Can not decide which list of QueryTriples should be mapped by Lucene Standard Mapper.");
				}
			}

			// --- D.3: Using RdfGroundedString Mapper for relatiosn and
			// EntityMaper for entities
			if (isActiveOption("resourceMapper", "RdfGroundedString")) {

				if (queryTripleSet.isEmpty()) {
					logger.error("No available QueryTriples for RdfGroundedString Mapper.");
				} else {

					logger.info("Mapping predicates using RdfGroundedString Mapper:");

					RdfGroundedStringMapper mapper = new RdfGroundedStringMapperImpl();
					for (int i = 0; i < queryTripleSet.size(); i++) {
						ArrayList<QueryTriple> triples = queryTripleSet.get(i);
						logger.info("");
						logger.info("Search relations for QueryTriple Set:");
						for (QueryTriple triple : triples) {
							List<RelationCandidate> relationCandidates = mapper
									.findRelationCandidates(triple
											.getPredicate());

							if (relationCandidates != null) {
								triple.addRelationCandidates(relationCandidates);
								logger.info("Found relations for triple '"
										+ triple.toString() + "':");
								for (RelationCandidate relationCandidate : relationCandidates) {
									logger.info(relationCandidate.toString());
								}
							} else {
								logger.warn("Could not map triple '"
										+ triple.toString()
										+ "'. Remove query triple set and continue with next set.");
								queryTripleSet.remove(i);
								i--;
								break;
							}
						}
					}

					logger.info("Mapping entities using Lucene Mapper:");

					LuceneMapper luceneMapper = new LuceneMapper(this);
					for (ArrayList<QueryTriple> triples : queryTripleSet) {
						logger.info("");
						logger.info("Search entities for QueryTriple Set:");
						for (QueryTriple triple : triples) {
							luceneMapper.mapOnlyEntitiesClasses(triple);
						}
					}

					logger.info("Found URIs by RdfGroundedStrings Mapper for '"
							+ inputQuestion + "':");
					for (List<QueryTriple> triples : queryTripleSet) {
						logger.info("Triple Set:");
						for (QueryTriple triple : triples) {
							logger.info(triple.getTripleWithCandidates());
						}
					}

				}

			}

			// ------ Step E:
			// --- Combine entities and relations to possible queries
			if (isActiveOption("sparqlGenerator", "standard")) {
				SparqlGenerator sparlqGenerator = new SparqlGeneratorImpl(this);
				sparlqGenerator.setNumberOfCandidates(Integer
						.parseInt(getOption("NumberOfSparqlCandidates")));

				if (!queryTriples.isEmpty() && queryTripleSet.isEmpty()) {
					// create SPARQL using queryTriples
					sparqlQueries = sparlqGenerator
							.getSparqlCanidates(queryTriples);

					for (SparqlCandidate sparqlCandidate : sparqlQueries) {
						logger.info("Found SPARQL queries by SPARQL Generator: "
								+ sparqlCandidate);
					}
				} else if (queryTriples.isEmpty() && !queryTripleSet.isEmpty()) {
					// create SPARQL using queryTripleSet
					sparqlQueries = sparlqGenerator
							.getSparqlCanidatesForQueryTripleSet(queryTripleSet);

					for (SparqlCandidate sparqlCandidate : sparqlQueries) {
						logger.info("Found SPARQL queries by SPARQL Generator: "
								+ sparqlCandidate);
					}
				} else if (queryTriples.isEmpty() && queryTripleSet.isEmpty()) {
					logger.error("No available QueryTriples for SparqlGenerator.");
				} else if (!queryTriples.isEmpty() && !queryTripleSet.isEmpty()) {
					logger.error("Can not decide which list of QueryTriples should be used by SparqlGenerator.");
				}

			}

			if (isActiveOption("sparqlGenerator", "selectQueries")) {
				logger.warn("Option 'sparqlGenerator:selectQueries' contains bugs and is not recommended.");
				int numberOfTriplesPerSparql = 2;
				try {
					numberOfTriplesPerSparql = Integer
							.parseInt(getOption("numberOfTriplesPerSparql"));
				} catch (GenerateSparqlException | NumberFormatException e) {
					logger.warn("Failed to read parameter 'numberOfTriplesPerSparql', using default value 2 .");
				}

				SparqlGenerator sparlqGenerator = new SparqlGeneratorImpl(this);
				sparlqGenerator.setNumberOfCandidates(Integer
						.parseInt(getOption("NumberOfSparqlCandidates")));
				sparqlQueries = sparlqGenerator.getSparqlCanidates(
						queryTriples, numberOfTriplesPerSparql);

				logger.info("Found SPARQL queries by SPARQL Generator: ");
				for (SparqlCandidate sparql : sparqlQueries) {
					logger.info(sparql);
				}
			}

			// Step E.3 the BruteForeMapper needs a special kind of treatment
			if (isActiveOption("resourceMapper", "naive")) {
				SparqlGenerator sparlqGenerator = new SparqlGeneratorImpl(this);
				sparlqGenerator.setNumberOfCandidates(Integer
						.parseInt(getOption("NumberOfSparqlCandidates")));
				sparqlQueries = sparlqGenerator
						.getSparqlCanidatesForQueryTripleSet(queryTripleSet);

				logger.info("Found SPARQL queries by SPARQL Generator: ");
				for (SparqlCandidate sparql : sparqlQueries) {
					logger.info(sparql);
				}
			}

			return sparqlQueries;

		} catch (ConfidenceFunctionException e) {
			// TODO Auto-generated catch block
			logger.error("", e);
		} catch (IOException e) {
			logger.error("", e);
		}

		return null;
	}

	private String replacePOSTags(String tagged_question) {
		String output;
		output = tagged_question.replaceAll("\\_[A-Z\\.]+", "")
				.replace("?", "");
		return output;
	}

	public String replaceWHxxWithVariable(String taggedQuestion) {
		String[] words = taggedQuestion.split(" ");
		String output = taggedQuestion;
		for (String word : words) {
			if (word.contains("_WP") || word.contains("_WDT")) {
				output = taggedQuestion.replace(word.replace("_WP", ""),
						"VARIABLE");
				word = "x";
			}
		}
		return output;
	}

	private QuestionTypes getQuestionType(String tagged_question) {
		tagged_question = tagged_question.toLowerCase();

		if (tagged_question.matches(".*(who).*"))
			return QuestionTypes.select_person;
		if (tagged_question.matches(".*how.*(many|tall|long).*"))
			return QuestionTypes.sum;
		if (tagged_question
				.matches(".*(when|how often|since when|how long|for how long).*"))
			return QuestionTypes.date;
		if (tagged_question.matches(".*(what|which|where).*"))
			return QuestionTypes.select_thing;
		if (tagged_question.matches(".*(show me|give me|list).*"))
			return QuestionTypes.select_thing;
		if (tagged_question.matches(".*(are|did|is|was|does|were|do).*"))
			return QuestionTypes.existantial;

		// default question type
		return QuestionTypes.select_thing;
	}

	@Override
	public List<String> executeSparql(String query) throws QueryParseException,
	QueryException {

		ResultSet rs = endpointConnector.executeQuery(query);
		List<String> results = new ArrayList<String>();

		while (rs.hasNext()) {
			QuerySolution qs = rs.next();
			if (qs.get("uri") != null)
				results.add(qs.get("uri").toString());
		}

		return results;
	}

	@Override
	public List<String> executeSparqlSet(List<SparqlCandidate> sparqlCandidates) throws QueryParseException,
	QueryException {

		List<String> results = new ArrayList<String>();
		
		for (SparqlCandidate candidate : sparqlCandidates) {
			ResultSet rs = endpointConnector.executeQuery(candidate.getSparqlQuery());

			logger.debug(candidate + " returns results:");
			
			while (rs.hasNext()) {
				QuerySolution qs = rs.next();
				if (qs.get("uri") != null) {
					String new_uri = qs.get("uri").toString();
					logger.debug(new_uri);
					if (!results.contains(new_uri)) results.add(new_uri);
				}
			}
		}

		return results;
	}

	@Override
	public boolean isActiveOption(String key, Object expectedObject) {
		String expectedValue = expectedObject.toString();

		// parameter map was not initialized as expected therefore throw an
		// exception
		if (parameter == null) {
			logger.error("Parameter Map not initialized! Provide a parameter map to the InputManager.");
			return false;
		}
		if (parameter.isEmpty()) {
			logger.error("Parameter Map is empty. Insert valid parameters!");
			return false;
		}
		// no entry implies not using the option
		if (!parameter.containsKey(key))
			return false;

		// return true only if the stored parameter is equal to the expected one
		if (parameter.get(key).toString()
				.equalsIgnoreCase(expectedValue.toLowerCase()))
			return true;

		return false;
	}

	@Override
	public String getOption(String key) throws GenerateSparqlException {
		// parameter map was not initialized as expected therefore throw an
		// exception
		if (parameter == null) {
			logger.error("Parameter Map not initialized! Provide a parameter map to the InputManager.");
			throw new GenerateSparqlException(
					"Parameter Map not initialized! Provide a parameter map to the InputManager.");
		}
		if (parameter.isEmpty()) {
			logger.error("Parameter Map is empty. Insert valid parameters!");
			throw new GenerateSparqlException(
					"Parameter Map is empty. Insert valid parameters!");
		}
		// no entry implies not using the option
		if (!parameter.containsKey(key)) {
			logger.error("Missing parameter " + key + "!");
			throw new GenerateSparqlException("Missing parameter " + key + "!");
		}

		return parameter.get(key);

	}

	@Override
	public StanfordLemmatizer getLemmatizer() {
		if (lemmatizer == null) {
			lemmatizer = new StanfordLemmatizer();
			logger.info("Load StanfordLemmatizer");
		}
		return lemmatizer;
	}
}
