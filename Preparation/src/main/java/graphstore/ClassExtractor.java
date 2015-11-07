package graphstore;

import java.beans.Customizer;
import java.io.File;
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


public class ClassExtractor {

//	private static String endpoint = "http://localhost:8890/sparql";
	private static String endpoint = "http://aifb-ls3-vm8.aifb.kit.edu:8890/sparql";
	private final static int SPARQL_LIMIT = 100000;
	private final static String file_name = "data/Classes.nq";
	private static Logger logger = Logger.getRootLogger();
	private static boolean customInput = false;

	public static void main(String[] args) throws IOException {
		SimpleLayout layout = new SimpleLayout();
		ConsoleAppender consoleAppender = new ConsoleAppender( layout );
		logger.addAppender( consoleAppender );
		logger.setLevel(Level.INFO);
		
		double step = 0;
		if (args != null && args.toString().equalsIgnoreCase("[]") ) {
			step = Double.parseDouble(args[0]);
			customInput = true;
		}

		File file = new File(file_name);
		if (file != null && !customInput) file.delete();

		int ACTUAL_SPARQL_LIMIT = SPARQL_LIMIT;
		int error_step = 0;
		while (true) {
			try {
				int offset =   (int) Math.round(SPARQL_LIMIT * step) + error_step;
				String sparqlQueryString = "select distinct ?Class ('http://www.w3.org/2000/01/rdf-schema#label' as ?rel) (?ClassLabel AS ?label) (?g AS ?graph) where { \n"
						+ "graph ?g { ?Class <http://www.w3.org/2000/01/rdf-schema#label> ?ClassLabel . } .\n"
						+ "graph ?g2 { ?x a ?Class . } . \n"
						+ "FILTER ( LANG(?ClassLabel) = \"\" || LANGMATCHES(LANG(?ClassLabel), \"en\") ) .\n"
						+ "FILTER ( !contains( str(?g), \"virtrdf\") ) . \n"
						+ "FILTER ( !contains( str(?g), \"localhost\") ) . \n"
						+ "} "
						+ " OFFSET " + offset
						+ " LIMIT " + ACTUAL_SPARQL_LIMIT;
				logger.info("Sparql query: " + sparqlQueryString);
				Query query = QueryFactory.create(sparqlQueryString);
				QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, query);

				ResultSet results = qexec.execSelect();
				logger.info(step);

				if (!results.hasNext()) {
					break;
				}

				FileWriter writer = new FileWriter(file_name, true);		

				while(results.hasNext()) {
					QuerySolution qs = results.next();
					String entity = qs.get("Class").toString();

					String relation = qs.get("rel").toString();

					String graph = qs.get("graph").toString();
					//add a new line
					String[] label_parts = qs.get("label").toString().split("@");
					String label = label_parts[0];
					if (label_parts.length > 1) {
						if (!label_parts[1].contains("en")) {continue;}
					}

					String newLine = "<" + entity + "> <" + relation + "> \"" + label + "\" <" + graph +"> . ";

					writer.append(newLine).append("\n");

				}
				qexec.close();
				writer.close();
				step++;
				error_step = 0;
				ACTUAL_SPARQL_LIMIT = SPARQL_LIMIT;
			} catch (Exception e) {
				logger.error("Error:", e);
				if (ACTUAL_SPARQL_LIMIT > 10000) {
					ACTUAL_SPARQL_LIMIT = ACTUAL_SPARQL_LIMIT / 2;
					logger.info("New ACTUAL_SPARQL_LIMIT: " + ACTUAL_SPARQL_LIMIT);
				} else {
					error_step += 10000;
					logger.info("New error_step: " + error_step);
					logger.info("ACTUAL_SPARQL_LIMIT: " + ACTUAL_SPARQL_LIMIT);
				}
			}
		}
	}

}

