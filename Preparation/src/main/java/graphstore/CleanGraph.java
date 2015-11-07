package graphstore;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.update.UpdateExecutionFactory;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.update.UpdateRequest;

public class CleanGraph {



	public static void main(String[] args) {
		CleanGraph cleaner = new CleanGraph();
		cleaner.cleanSameInOtherLanguageDBPediaLinks();
	}
	
	
	public void cleanSameInOtherLanguageDBPediaLinks() {


		String sparqlQueryString1 = "SELECT distinct ?g  WHERE { "
				+ "GRAPH ?g { "
				+ " ?x <http://www.w3.org/2002/07/owl#sameAs> ?y . "
				+ "FILTER regex(?x, \"http://[a-z]{2}.dbpedia.org\") . "
				+ "}}";
		Query query = QueryFactory.create(sparqlQueryString1);
		QueryExecution qexec = QueryExecutionFactory.sparqlService("http://localhost:8890/sparql", query);

		ResultSet results1 = qexec.execSelect();

		while(results1.hasNext()) {

			String sparqlQueryString2 = " DELETE WHERE { GRAPH "
					+ "<" + results1.next().get("?g").toString() + "> {"
					+ " ?x <http://www.w3.org/2002/07/owl#sameAs> ?y . "
					+ " FILTER regex(?x, \"http://[a-z]{2}.dbpedia.org\") . "
					+ "}}";
			System.out.println(sparqlQueryString2);
			UpdateRequest update = UpdateFactory.create(sparqlQueryString2);
			UpdateProcessor upd_exec = UpdateExecutionFactory.createRemote(update, "http://localhost:8890/sparql");
			upd_exec.execute();
			
		}

		qexec.close();


	}
}
