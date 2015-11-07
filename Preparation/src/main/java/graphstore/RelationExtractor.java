package graphstore;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

/**
 * class to find and print the N most used relations.
 * At the moment a single SPARQL expression is used for this task...
 * @author Sebastian Bader (sebastian.bader@student.kit.edu)
 *
 */
public class RelationExtractor {

	private String endpoint;
	private int min_occurance = 50;
	private final static int SPARQL_LIMIT = 10000;
	private final static String file_name = "../Preparation/data/Relations.nq";
	private static Logger logger = Logger.getRootLogger();

	public static void main(String[] args) throws IOException {
		RelationExtractor relationExtractor = new RelationExtractor("http://aifb-ls3-vm8.aifb.kit.edu:8890/sparql");
		relationExtractor.extractRelations();
	}

	public RelationExtractor(String endpoint) {
		this.endpoint = endpoint;
	}

	public void extractRelations() throws IOException {
		SimpleLayout layout = new SimpleLayout();
		ConsoleAppender consoleAppender = new ConsoleAppender( layout );
		logger.addAppender( consoleAppender );
		logger.setLevel(Level.INFO);


		File file = new File(file_name);
		if (file != null) file.delete();

		int ACTUAL_SPARQL_LIMIT = SPARQL_LIMIT;
		double step = 0;
		int error_step = 0;
		while (step == 0) {
			try {
				int offset =   (int) Math.round(SPARQL_LIMIT * step) + error_step;
				String sparqlQueryString = "select distinct ?Relation (SAMPLE(?RelLabel) AS ?RelationLabel) "
						+ "(SAMPLE(?g2) AS ?graph) (count(?Relation) as ?c) \n "
						+ " where { \n"
						+ "graph ?g1 { ?x ?Relation ?y . } \n"
						+ "graph ?g2 { ?Relation <http://www.w3.org/2000/01/rdf-schema#label> ?RelLabel . } \n"
						+ "FILTER(LANG(?RelLabel) = \"\" || LANGMATCHES(LANG(?RelLabel), \"en\")) \n"
						+ "} GROUP BY ( ?Relation )"
						+ " ORDER BY DESC (?c ) \n"
						+ " OFFSET " + offset + " LIMIT " + ACTUAL_SPARQL_LIMIT;
				logger.info(sparqlQueryString);
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
					String entity = qs.get("Relation").toString();

					String relation = "http://www.w3.org/2000/01/rdf-schema#label";

					String graph = qs.get("graph").toString();
					//add a new line
					String[] label_parts = qs.get("RelationLabel").toString().split("@");
					String label = label_parts[0];
					if (label_parts.length > 1) {
						if (!label_parts[1].contains("en")) {continue;}
					}

					try {	
						int counted = qs.getLiteral("c").getInt();
						if (counted < min_occurance ) continue;
					} catch (NumberFormatException e) {}


					String newLine = "<" + entity + "> <" + relation + "> \"" + label + "\" <" + graph +"> . ";

					writer.append(newLine.replace("\n", "")).append("\n");

				}
				qexec.close();
				writer.close();
				step++;
				error_step = 0;
				ACTUAL_SPARQL_LIMIT = SPARQL_LIMIT;
			} catch (QueryParseException e) {
				logger.error("Incorrect Sparql, abort program", e);
				System.exit(1);
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

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

}

