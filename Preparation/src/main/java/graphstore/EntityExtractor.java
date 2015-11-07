package graphstore;

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


public class EntityExtractor {

	private final static int SPARQL_LIMIT = 100000;
	private final static String file_name = "../Preparation/data/Instances_allLiterals.nq";
	private static Logger logger = Logger.getRootLogger();
	private static boolean customInput = false;
	
	private final static String endpoint = "http://localhost:8890/sparql";
//	private final static String endpoint = "http://aifb-ls3-vm8.aifb.kit.edu:8890/sparql";

	public static void main(String[] args) throws IOException {
		SimpleLayout layout = new SimpleLayout();
		ConsoleAppender consoleAppender = new ConsoleAppender( layout );
		logger.addAppender( consoleAppender );
		logger.setLevel(Level.INFO);

		double step = 0;
		if (args != null) if (args.length > 0)
			if(!args.toString().equalsIgnoreCase("[]") ) {
				step = Double.parseDouble(args[0]);
				customInput = true;
				logger.info("Set STEP to " + step);
			}

		File file = new File(file_name);
		if (file != null && !customInput) {
			file.delete();
			logger.info("Deleted old File.");
		}

		int ACTUAL_SPARQL_LIMIT = SPARQL_LIMIT;
		int error_step = 0;
		int counter = 0;
		String newLine = null;
		while (true) {
			try {
				counter = 0;

				int offset =   (int) Math.round(SPARQL_LIMIT * step) + error_step;
				String sparqlQueryString = "select distinct ?Entity ?rel ?EntityLabel (<http://dummy.edu/graph> AS ?graph) where { \n"
						+ "graph ?g {"
						+ "?Entity ?rel ?EntityLabel . \n"
						+ "FILTER ( !contains( str(?g), \"virtrdf\") ) . \n"
						+ "FILTER ( !contains( str(?g), \"localhost\") ) . \n"
						+ "FILTER ( LANG(?EntityLabel) = \"\" || LANGMATCHES(LANG(?EntityLabel), \"en\") ) .\n"
						+ "} \n"
						+ "} \n"
						+ " OFFSET " + offset
						+ " LIMIT " + ACTUAL_SPARQL_LIMIT ;
				
				Query query = QueryFactory.create(sparqlQueryString);
				QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, query);

				ResultSet results = qexec.execSelect();
				logger.info(step);

				if (!results.hasNext()) {
					break;
				}

				FileWriter writer = new FileWriter(file_name, true);		
				int line_number = 0;
				while(results.hasNext()) {
					try {
						line_number++;
						
						QuerySolution qs = results.next();
						String entity = qs.get("Entity").toString();

						String relation = qs.get("rel").toString();

						String graph = qs.get("graph").toString();
						//add a new line
						String[] label_parts = qs.get("EntityLabel").toString().split("@");
						String label = label_parts[0];
						if (label_parts.length > 1) {
							if (!label_parts[1].contains("en")) {continue;}
						}
						label = label.replace("\"", "''");

						newLine = "<" + entity + "> <" + relation + "> \"" + label + "\" <" + graph +"> . ";
						newLine = newLine.replace("\n", "");
						newLine = newLine.replace("\r", "");
						writer.append(newLine).append("\n");
						counter++;
					} catch (Exception e) {
						logger.error("Exception in inner loop: ", e);
						logger.error("Step: " + step);
						logger.error("Inner Step: " + counter);
						logger.error("Line: " + newLine);
					}

				}
				qexec.close();
				writer.close();
				step++;
				error_step = 0;
				ACTUAL_SPARQL_LIMIT = SPARQL_LIMIT;
			} catch (Exception e) {
				logger.error("Exception in outer loop: ", e);
				logger.error("Step: " + step);
				logger.error("Inner Step: " + counter);
				logger.error("Line: " + newLine);

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

