package old;

import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.CosineSimilarity;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import virtuoso.jena.driver.*;

public class CombineDBpdiaAndICD11 {



	private static String[] stopWordsList = {"syndrom", "@en"};

	public static void main(String[] args) {
		String[][] dbpediaEntities = new String[5000][2];
		String[][] icdEntities = new String[30000][2];
		int c1 = 0;
		int c2 = 0;

		String sparqlQueryString1 = "SELECT distinct ?dbEntity ?dbEntityLabel WHERE { "
				+ "GRAPH ?g { "
				+ "?dbEntity ?rel ?o . "
				+ "?dbEntity <http://www.w3.org/2000/01/rdf-schema#label> ?dbEntityLabel. "
				+ "FILTER (langMatches(lang(?dbEntityLabel),\"en\")) "
				+ "FILTER regex(?dbEntity , \"http://dbpedia\") ."
				+ "FILTER regex(?rel, \"icd\") . "
				+ "}}";
		Query query = QueryFactory.create(sparqlQueryString1);
		QueryExecution qexec = QueryExecutionFactory.sparqlService("http://localhost:8890/sparql", query);

		ResultSet results1 = qexec.execSelect();

		while(results1.hasNext()) {
			QuerySolution qs = results1.next();
			dbpediaEntities[c1][0] = qs.get("dbEntity").toString(); 
			dbpediaEntities[c1][1] = qs.get("dbEntityLabel").toString();
			c1++;
		}

		qexec.close();



		String sparqlQueryString2 = "SELECT distinct ?dbEntity ?dbEntityName WHERE { "
				+ "GRAPH ?g { "
				+ "?dbEntity ?rel ?o . "
				+ "	?dbEntity <http://xmlns.com/foaf/0.1/name> ?dbEntityName . "
				+ "	FILTER (langMatches(lang(?dbEntityName ),\"en\")) "
				+ "FILTER regex(?dbEntity , \"http://dbpedia\") ."
				+ "FILTER regex(?rel, \"icd\") . "
				+ "}}";
		query = QueryFactory.create(sparqlQueryString2);
		qexec = QueryExecutionFactory.sparqlService("http://localhost:8890/sparql", query);

		ResultSet results2 = qexec.execSelect();

		while(results2.hasNext()) {
			QuerySolution qs = results2.next();
			dbpediaEntities[c1][0] = qs.get("dbEntity").toString(); 
			dbpediaEntities[c1][1] = qs.get("dbEntityName").toString();
			c1++;
		}

		qexec.close();


		String sparqlQueryString3 = "select ?icdEntity ?icdAltLabel  where { "
				+ "graph ?g { "
				+ "?icdEntity <http://www.w3.org/2004/02/skos/core#altLabel> ?icdAltLabel ."
				+ "?icdEntity <http://www.w3.org/2004/02/skos/core#definition> ?object . "
				+ "FILTER regex(?icdEntity, \"http://id.who.int/icd\") ."
				+ "}}";

		query = QueryFactory.create(sparqlQueryString3);
		qexec = QueryExecutionFactory.sparqlService("http://localhost:8890/sparql", query);

		ResultSet results3 = qexec.execSelect();

		while(results3.hasNext()) {
			QuerySolution qs = results3.next();
			icdEntities[c2][0] = qs.get("icdEntity").toString(); 
			icdEntities[c2][1] = qs.get("icdAltLabel").toString();
			c2++;
		}

		qexec.close() ;



		String sparqlQueryString4 = "select ?icdEntity ?icdPrefLabel  where { "
				+ "graph ?g { "
				+ "?icdEntity <http://www.w3.org/2004/02/skos/core#prefLabel> ?icdPrefLabel ."
				+ "?icdEntity <http://www.w3.org/2004/02/skos/core#definition> ?object ."
				+ "FILTER regex(?icdEntity, \"http://id.who.int/icd\") ."
				+ "}}";

		query = QueryFactory.create(sparqlQueryString4);
		qexec = QueryExecutionFactory.sparqlService("http://localhost:8890/sparql", query);

		ResultSet results4 = qexec.execSelect();

		while(results4.hasNext()) {
			QuerySolution qs = results4.next();
			icdEntities[c2][0] = qs.get("icdEntity").toString(); 
			icdEntities[c2][1] = qs.get("icdPrefLabel").toString();
			c2++;
		}

		qexec.close() ;



		AbstractStringMetric metric = new CosineSimilarity();
		for (int i = 0; i < 5000; i++) {
			//			System.out.println(dbpediaEntities[i][0]);
			for (int j = 0; j < 18000; j++) {
				//				System.out.println(j + icdEntities[j][1]);

				if (j == 14857) {
					System.out.print("");
				} else {
					if (dbpediaEntities[i][1] != null && icdEntities[j][1] != null) {
						String dbpediaEntityName = removeStopwords(dbpediaEntities[i][1]);
						String icdEntityName = removeStopwords(icdEntities[j][1]);
						double sim = metric.getSimilarity(dbpediaEntityName, icdEntityName );
						if (sim>0.75) {

							writeRelationToGraphStore(dbpediaEntities[i], icdEntities[j], sim);

						}
					}

				}
			}
		}

	}

	private static void writeRelationToGraphStore(String[] dbpediaEntity, String[] icdEntity, Double sim) {

		/*			STEP 1			*/
		VirtGraph set = new VirtGraph ("jdbc:virtuoso://localhost:1111", "dba", "dba");

		/*			STEP 2			*/
//		System.out.println("\nexecute: CLEAR GRAPH <http://dbpedia.to.icd11.connections>");
		String str;
//		str = "CLEAR GRAPH <http://dbpedia.to.icd11.connections>";
		VirtuosoUpdateRequest vur;
//		vur = VirtuosoUpdateFactory.create(str, set);
//		vur.exec();                  

		str = "INSERT INTO GRAPH <http://dbpedia.to.icd11.connections> { <" + dbpediaEntity[0] + "> <http://dbpedia.to.icd11.connections/sameAs> <" + icdEntity[0] + "> . }";
		System.out.println(str);	
		vur = VirtuosoUpdateFactory.create(str, set);
		vur.exec();      







		System.out.print("<");
		System.out.print(dbpediaEntity[0]);
		System.out.print(">");
		System.out.print("<");
		System.out.print(dbpediaEntity[1]);
		System.out.print(">");
		System.out.print(" ");
		System.out.print("<");
		System.out.print(icdEntity[0]);
		System.out.print(">");
		System.out.print("<");
		System.out.print(icdEntity[1]);
		System.out.print(">");
		System.out.println(" sim: " + sim);

	}

	private static String removeStopwords(String string) {
		string = string.replaceAll("@en", "");
		string = string.replaceAll("syndrome", "");
		return string;
	}
}
