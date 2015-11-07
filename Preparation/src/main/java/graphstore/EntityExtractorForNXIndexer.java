package graphstore;

import java.io.FileWriter;
import java.io.IOException;
import java.util.NoSuchElementException;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;


public class EntityExtractorForNXIndexer {

	private static Logger logger = Logger.getRootLogger();
	private static String condition;
	private static String marker;
	private final static int DOCUMENT_LINE_LIMIT = 300;
	private final static int SPARQL_LIMIT = 30000;

	public static void main(String[] args) throws IOException {
		//		initLogger();


		marker = "default";
		condition = "";
		for (int i = 0; i <= 35; i++) {
			if (writeSet(i) == 1) break;
		}

		marker = "label";
		condition = "?Entity <http://www.w3.org/2000/01/rdf-schema#label> ?EntityLabel . "
				+ "FILTER (langMatches(lang(?EntityLabel ),\"en\")) . ";
		for (int i = 0; i <= 15; i++) {
			if (writeSet(i) == 1) break;
		}

		marker = "name";
		condition = "?Entity <http://xmlns.com/foaf/0.1/name> ?EntityLabel . "
				+ "FILTER (langMatches(lang(?EntityLabel ),\"en\")) . ";
		for (int i = 0; i <= 10; i++) {
			if (writeSet(i) == 1) break;
		}
	}

	private static int writeSet(int step) throws IOException {	
		int document_counter = 0;
		int line_counter = 0;
		int offset = SPARQL_LIMIT * step;
		String sparqlQueryString = "select distinct ?Entity ?EntityLabel where {Graph ?g {"
				+ "?Entity ?r ?y . "
				+ condition
				+ "}} "
				+ " OFFSET " + offset
				+ " LIMIT " + SPARQL_LIMIT;
		Query query = QueryFactory.create(sparqlQueryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService("http://localhost:8890/sparql", query);

		ResultSet results = qexec.execSelect();
		System.out.println("executed query");

		if (!results.hasNext()) {
			return 1;
		} else {
			FileWriter writer = new FileWriter(
					"/Users/Sebastian/eclipse-luna-64/lucene-5.1.0/src/Demo/Docs/Entity/Entitites" 
							+ marker 
							+ offset 
							+ "-" 
							+ document_counter + ".txt"
							, false);
			
			
			while(results.hasNext()) {
				QuerySolution qs = results.next();
				String entity = qs.get("Entity").toString();

				//add a new line
				if (qs.contains("EntityLabel")) {
					String label = qs.get("EntityLabel").toString();
					label = label.split("@")[0];

					String newLine = "<" + entity + ">" + label;
					newLine = adjustString(newLine, 150);
					writer.append(newLine).append("\n");
				} else {
					String newLine = "<" + entity + ">";
					newLine = adjustString(newLine, 150);
					writer.append(newLine).append("\n");;
				}
				line_counter++;
				
				//close and open a new file if actual one gets to big
				if (line_counter >= DOCUMENT_LINE_LIMIT) {
					writer.close();
					line_counter = 0;
					document_counter++;
					writer = new FileWriter(
							"/Users/Sebastian/eclipse-luna-64/lucene-5.1.0/src/Demo/Docs/Entity/Entitites" 
									+ marker 
									+ offset 
									+ "-" 
									+ document_counter + ".txt"
									, false);
				}
			}

			qexec.close();
			writer.close();
			return 0;
		}
	}


	private static String adjustString(String newLine, int limit) {
		if (newLine.length() < limit) {
			newLine = adjustString(newLine + "+", limit);
		} else if (newLine.length() > limit) {
			return newLine.substring(0, limit);
		}
		return newLine;
	}

	/**
	 * initiate the log4j logger
	 */
	private static void initLogger() {
		try {
			SimpleLayout layout = new SimpleLayout();

			try {
				logger.getAllAppenders().nextElement();
			} catch (NoSuchElementException e) {
				//add appenders for console and log file if and only if no one has been created yet
				ConsoleAppender consoleAppender = new ConsoleAppender( layout );
				logger.addAppender( consoleAppender );

				FileAppender fileAppender = new FileAppender( layout, "logs/naturallang2SPARQL_engine_connector_SPARQLEndpointConnector.log", true );
				logger.addAppender( fileAppender );
			}

			logger.setLevel( Level.ALL );
		} catch( Exception ex ) {
			System.out.println("Error: Could not initialize Logger. Terminate Application.");
			System.out.println( ex );
			System.exit(1);			
		}
	}
}

