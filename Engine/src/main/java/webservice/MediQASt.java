package webservice;

import inputmanagement.InputManager;
import inputmanagement.candidates.impl.SparqlCandidate;
import inputmanagement.impl.InputManagerImpl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import configuration.ConfigManager;
import configuration.impl.ConfigManagerImpl;

/**
 * Servlet implementation class MediQASt
 */
@WebServlet("/answer")
public class MediQASt extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Logger logger;

	/**
	 * Default constructor. 
	 * @throws IOException 
	 * @throws ServletException 
	 */
	public MediQASt() throws IOException {
		ConfigManager configManager = new ConfigManagerImpl();
		this.logger = configManager.getLogger();
		//this.logger = Logger.getRootLogger();

	}



	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		//Load received values
		String query = request.getParameter("query");
		String apporach = request.getParameter("approach");
		String endpoint = "http://aifb-ls3-vm8.aifb.kit.edu:8890/sparql";


		//Load parameter set
		HashMap<String, String> parameter = new HashMap<String, String>();
		if (apporach.equalsIgnoreCase("rule based")) {

			// standard parameter
			parameter.put("directSparqlPossible", "true");

			// Analyzer
			parameter.put("questionAnalyzer", "rulebased");
			parameter.put("findEntityAndClass", "true");

			parameter.put("RelationManagerSimilarity", "Levenshtein");
			//parameter.put("RelationManagerSimilarity", "WordNet");


			// Mapper
			parameter.put("resourceMapper", "luceneStandard");

			parameter.put("LuceneStandardMapper:Formula", "own");
			//parameter.put("LuceneStandardMapper:Formula", "lucene");

			parameter.put("LuceneStandardMapper:AdjustFieldNorm", "true");
			//parameter.put("LuceneStandardMapper:AdjustFieldNorm", "false");

			parameter.put("LuceneStandardMapper:BoostPerfectMatch", "true");
//			parameter.put("LuceneStandardMapper:BoostPerfectMatch", "false");

			//parameter.put("LuceneStandardMapper:Lemmatize", "true");
			parameter.put("LuceneStandardMapper:Lemmatize", "false");

			parameter.put("LuceneStandardMapper:StopwordRemoval", "true");
			//parameter.put("LuceneStandardMapper:StopwordRemoval", "false");

			//parameter.put("LuceneStandardMapper:SearchPerfect", "only");
			parameter.put("LuceneStandardMapper:SearchPerfect", "no");

			parameter.put("LuceneStandardMapper:DivideByOccurrence", "true");
			//parameter.put("LuceneStandardMapper:DivideByOccurrence", "false");

			parameter.put("LuceneStandardMapper:FuzzySearch", "true");
			//parameter.put("LuceneStandardMapper:FuzzySearch", "false");

			parameter.put("LuceneStandardMapper:FuzzyParam", "1");
			//parameter.put("LuceneStandardMapper:FuzzyParam", "2");

			parameter.put("LuceneStandardMapper:NumberOfHits", "20");

			
			// SPARQL Generator
			parameter.put("sparqlGenerator", "standard");

			parameter.put("NumberOfSparqlCandidates", "40");
			parameter.put("SparqlLimit", "1000");
			parameter.put("numberOfTriplesPerSparql", "2");
			parameter.put("sparqlOption", "greedy");
			parameter.put("KeyWordQuestionThreshold", "0.4");


		} else if (apporach.equalsIgnoreCase("naive")) {

			parameter.put("directSparqlPossible", "true");

			// Analyzer
			parameter.put("questionAnalyzer", "naive");
			parameter.put("findEntityAndClass", "true");

			parameter.put("RelationManagerSimilarity", "Levenshtein");
			//parameter.put("RelationManagerSimilarity", "WordNet");


			// Mapper
			parameter.put("resourceMapper", "naive");

			//parameter.put("NaiveMapper:windows", "2");
//			parameter.put("NaiveMapper:windows", "3");
			parameter.put("NaiveMapper:windows", "4");

			parameter.put("NaiveMapper:threshold", "0.0");
			//parameter.put("NaiveMapper:threshold", "0.5");

			parameter.put("NaiveAnalyzer:ValidateSteps", "1");
//			parameter.put("NaiveAnalyzer:ValidateSteps", "2");

			parameter.put("LuceneStandardMapper:Formula", "own");
//			parameter.put("LuceneStandardMapper:Formula", "lucene");

			parameter.put("LuceneStandardMapper:AdjustFieldNorm", "true");
			//parameter.put("LuceneStandardMapper:AdjustFieldNorm", "false");

			parameter.put("LuceneStandardMapper:BoostPerfectMatch", "true");
//			parameter.put("LuceneStandardMapper:BoostPerfectMatch", "false");

			//parameter.put("LuceneStandardMapper:Lemmatize", "true");
			parameter.put("LuceneStandardMapper:Lemmatize", "false");

			parameter.put("LuceneStandardMapper:StopwordRemoval", "true");
			//parameter.put("LuceneStandardMapper:StopwordRemoval", "false");

			//parameter.put("LuceneStandardMapper:SearchPerfect", "only");
			parameter.put("LuceneStandardMapper:SearchPerfect", "no");

			parameter.put("LuceneStandardMapper:DivideByOccurrence", "true");
			//parameter.put("LuceneStandardMapper:DivideByOccurrence", "false");

			parameter.put("LuceneStandardMapper:FuzzySearch", "true");
			//parameter.put("LuceneStandardMapper:FuzzySearch", "false");

			parameter.put("LuceneStandardMapper:FuzzyParam", "1");
//			parameter.put("LuceneStandardMapper:FuzzyParam", "2");

			parameter.put("LuceneStandardMapper:NumberOfHits", "20");


			// SPARQL Generator

			parameter.put("NumberOfSparqlCandidates", "20");
			parameter.put("SparqlLimit", "50");
			parameter.put("numberOfTriplesPerSparql", "2");
			parameter.put("sparqlOption", "greedy");
			parameter.put("KeyWordQuestionThreshold", "0.4");

		} else if (apporach.equalsIgnoreCase("binary relation")) {

			parameter.put("directSparqlPossible", "true");

			// Analyzer
			parameter.put("questionAnalyzer", "reverb");
			parameter.put("findEntityAndClass", "true");

			parameter.put("RelationManagerSimilarity", "Levenshtein");
			//parameter.put("RelationManagerSimilarity", "WordNet");


			// Mapper
			parameter.put("resourceMapper", "luceneStandard");

			parameter.put("LuceneStandardMapper:Formula", "own");
			//parameter.put("LuceneStandardMapper:Formula", "lucene");
			
			parameter.put("LuceneStandardMapper:AdjustFieldNorm", "true");
			//parameter.put("LuceneStandardMapper:AdjustFieldNorm", "false");

			parameter.put("LuceneStandardMapper:BoostPerfectMatch", "true");
			//parameter.put("LuceneStandardMapper:BoostPerfectMatch", "false");

			//parameter.put("LuceneStandardMapper:Lemmatize", "true");
			parameter.put("LuceneStandardMapper:Lemmatize", "false");

			parameter.put("LuceneStandardMapper:StopwordRemoval", "true");
			//parameter.put("LuceneStandardMapper:StopwordRemoval", "false");

			//parameter.put("LuceneStandardMapper:SearchPerfect", "only");
			parameter.put("LuceneStandardMapper:SearchPerfect", "no");

			parameter.put("LuceneStandardMapper:DivideByOccurrence", "true");
			//parameter.put("LuceneStandardMapper:DivideByOccurrence", "false");

			parameter.put("LuceneStandardMapper:FuzzySearch", "true");
			//parameter.put("LuceneStandardMapper:FuzzySearch", "false");

			parameter.put("LuceneStandardMapper:FuzzyParam", "1");
			//parameter.put("LuceneStandardMapper:FuzzyParam", "2");

			parameter.put("LuceneStandardMapper:NumberOfHits", "10");

			// SPARQL Generator
			parameter.put("sparqlGenerator", "standard");

			parameter.put("NumberOfSparqlCandidates", "2");
			parameter.put("SparqlLimit", "3");
			parameter.put("numberOfTriplesPerSparql", "2");
			parameter.put("sparqlOption", "greedy");
			parameter.put("KeyWordQuestionThreshold", "0.4");

		} else if (apporach.equalsIgnoreCase("pattern based")) {

			parameter.put("directSparqlPossible", "true");

			// Analyzer
			parameter.put("questionAnalyzer", "rdfgroundedstring");
			parameter.put("findEntityAndClass", "true");

			parameter.put("RelationManagerSimilarity", "Levenshtein");
			//parameter.put("RelationManagerSimilarity", "WordNet");


			// Mapper
			parameter.put("resourceMapper", "rdfgroundedstring");

			parameter.put("LuceneStandardMapper:Formula", "own");
			//parameter.put("LuceneStandardMapper:Formula", "lucene");
			
			parameter.put("LuceneStandardMapper:AdjustFieldNorm", "true");
			//parameter.put("LuceneStandardMapper:AdjustFieldNorm", "false");

			parameter.put("LuceneStandardMapper:BoostPerfectMatch", "true");
			//parameter.put("LuceneStandardMapper:BoostPerfectMatch", "false");

			//parameter.put("LuceneStandardMapper:Lemmatize", "true");
			parameter.put("LuceneStandardMapper:Lemmatize", "false");

			parameter.put("LuceneStandardMapper:StopwordRemoval", "true");
			//parameter.put("LuceneStandardMapper:StopwordRemoval", "false");

			//parameter.put("LuceneStandardMapper:SearchPerfect", "only");
			parameter.put("LuceneStandardMapper:SearchPerfect", "no");

			parameter.put("LuceneStandardMapper:DivideByOccurrence", "true");
			//parameter.put("LuceneStandardMapper:DivideByOccurrence", "false");

			parameter.put("LuceneStandardMapper:FuzzySearch", "true");
			//parameter.put("LuceneStandardMapper:FuzzySearch", "false");

			parameter.put("LuceneStandardMapper:FuzzyParam", "1");
			//parameter.put("LuceneStandardMapper:FuzzyParam", "2");

			parameter.put("LuceneStandardMapper:NumberOfHits", "10");


			// SPARQL Generator
			parameter.put("sparqlGenerator", "standard");

			parameter.put("NumberOfSparqlCandidates", "5");
			parameter.put("SparqlLimit", "20");
			parameter.put("numberOfTriplesPerSparql", "2");
			parameter.put("sparqlOption", "greedy");
			parameter.put("KeyWordQuestionThreshold", "0.4");
		}


		try {


			//Start the Engine
			InputManager inputManager = new InputManagerImpl(endpoint, query, parameter);
			List<SparqlCandidate> sparqlList = inputManager.generateSparql();


			//Get the results
			List<String> uris = new ArrayList<String>();
			List<String> result = inputManager.executeSparqlSet(sparqlList);

			if (result != null) {
				uris.addAll( result );
			}



			//send the results
			PrintWriter out = response.getWriter();
			out.println("<!DOCTYPE html>");
			out.println("<html>");
			out.println("<head>");
			out.println("<meta charset=\"ISO-8859-1\">");
			out.println("<title>MediQASt Demo</title>");
			out.println("</head>");
			out.println("<body>");
			out.println("<h1>Proposed Entities for:<br>'" + query + "'</h1><br>");
			for (String uri : uris) {
				out.println("<a href=\"" + uri + "\">" + uri + "</a><br>");
			}
			out.println("</body>");
			out.println("</html>");


		} catch (Exception e) { 
			response.sendError(500, "internal error. Exception was thrown: " + e);
			logger.error("Exception during excecution.", e);
		}

		response.setStatus(200);

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response) 
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		ServletContext context = request.getServletContext();
		String path = context.getRealPath("/");

		logger.info("Received POST request");
		HashMap<String, String> parameter = new HashMap<String, String>();
		String endpoint = null;
		String query = null;


		//Load received values
		StringBuffer jb = new StringBuffer();
		String line = null;
		try {
			BufferedReader reader = request.getReader();
			while ((line = reader.readLine()) != null) {
				jb.append(line);
			}

			String body = jb.toString();
			logger.debug("POST body: " + body);
			JSONObject json = new JSONObject(body);


			JSONArray param = json.getJSONArray("parameter");
			for (int i = 0; i < param.length(); i++) {
				JSONObject obj = param.getJSONObject(i);
				for( String key : obj.keySet()) {
					String value = obj.getString(key);
					parameter.put(key, value);
				}
			}


			query = json.getString("query");

			endpoint = json.getString("endpoint");

		} catch (Exception e) { 
			response.sendError(406, "invalid input. Exception was thrown: " + e);
			logger.error("Exception during parsing of body data.", e);
		}

		try {
			//Start the Engine
			InputManager inputManager = new InputManagerImpl(endpoint, query, parameter);
			List<SparqlCandidate> sparqlList = inputManager.generateSparql();

			//Get the results
			List<String> uris = new ArrayList<String>();
			for (int i = 0; i < 10 && i < sparqlList.size(); i++) {
				SparqlCandidate sparql = sparqlList.get(i);
				String sparqlQuery = sparql.getSparqlQuery();
				List<String> result = inputManager.executeSparql(sparqlQuery);

				if (result != null) {
					uris.addAll( result );
				}
			}

			//send the results
			PrintWriter out = response.getWriter();

			String responseJson = new Gson().toJson( uris );
			JSONObject answer = new JSONObject("{\"answers\":" + responseJson + "}");

			out.println(answer.toString());
			logger.debug("Sent to client: " + responseJson);
		} catch (Exception e) { 
			response.sendError(500, "internal error. Exception was thrown: " + e);
			logger.error("Exception during excecution.", e);
		}

		response.setStatus(200);
	}


}