package graphstore;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

import virtuoso.jena.driver.*;

/**
 * connects dbpedia instances and icd instances with owl:sameAs and 
 * the new dbpedia:icd11 relation
 * @author Sebastian Bader (sebastian.bader@student.kit.edu)
 *
 */
public class MapDBpdiaToICD11 {

	public static void main(String[] args) {
		//		String[][] dbpediaEntities = new String[10000][2];
		String[][] icdEntities = new String[10000][2];
		int c1 = 0;

		int counter = 0;


		String sparqlQueryString1 = "SELECT distinct ?icdEntity ?icdLabel WHERE { "
				+ "GRAPH ?g { "
				+ "?icdEntity <http://id.who.int/icd/relation#hasICD10code> ?icdLabel . "
				+ "}}";
		Query query = QueryFactory.create(sparqlQueryString1);
		QueryExecution qexec = QueryExecutionFactory.sparqlService("http://localhost:8890/sparql", query);

		ResultSet results1 = qexec.execSelect();

		while(results1.hasNext()) {
			QuerySolution qs = results1.next();
			icdEntities[c1][0] = qs.get("icdEntity").toString(); 
			icdEntities[c1][1] = qs.get("icdLabel").toString();
			c1++;
		}

		qexec.close();


		VirtGraph set = new VirtGraph ("jdbc:virtuoso://localhost:1111", "dba", "dba");

		//insert the new ICD11 Relation
		String str = "INSERT { GRAPH <http://dbpedia.org/> { "
				+ "<http://dbpedia.org/ontology/icd11> a <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ."
				+ "<http://dbpedia.org/ontology/icd11> a <http://www.w3.org/2002/07/owl#DatatypeProperty> ."
				+ "<http://dbpedia.org/ontology/icd11> <http://www.w3.org/2000/01/rdf-schema#label> \"ICD11\" . "
				+ "}}";
		System.out.println(str);	
		VirtuosoUpdateRequest vur = VirtuosoUpdateFactory.create(str, set);
		vur.exec();

		for (int c2 = 0; c2 < c1; c2++) {

			String sparqlQueryString2 = "SELECT distinct ?dbEntity ?dbLabel ?g WHERE { "
					+ "GRAPH ?g { "
					+ "?dbEntity <http://dbpedia.org/ontology/icd10> ?dbLabel . "
					+ "FILTER regex( ?dbLabel, \"" + icdEntities[c2][1] + "(?!([0-9])|\\\\.)\") ."
					+ "}}";
			query = QueryFactory.create(sparqlQueryString2);
			qexec = QueryExecutionFactory.sparqlService("http://localhost:8890/sparql", query);

			ResultSet results2 = qexec.execSelect();

			while(results2.hasNext()) {
				QuerySolution qs = results2.next();
//				System.out.print(icdEntities[c2][0]);
//				System.out.print(" ");
//				System.out.print(icdEntities[c2][1]);
//				System.out.print(" ");
//				System.out.print(qs.get("dbEntity").toString());
//				System.out.print(" ");
//				System.out.println(qs.get("dbLabel").toString());

				str = "INSERT {"
						+ " GRAPH <" + qs.get("g").toString() + "> { "
						+ "<" + qs.get("dbEntity").toString() + "> <http://dbpedia.org/ontology/icd11> <" + icdEntities[c2][0] + "> . "
						+ "<" + qs.get("dbEntity").toString() + "> <http://www.w3.org/2002/07/owl#sameAs> <" + icdEntities[c2][0] + "> . "
						+ "}}";
//				System.out.println(str);
				System.out.println("<" + qs.get("dbEntity").toString() + "> <http://www.w3.org/2002/07/owl#sameAs> <" + icdEntities[c2][0] + "> . ");
//				vur = VirtuosoUpdateFactory.create(str, set);
//				vur.exec();
				counter++;

			}



		}

		System.out.println("Inserted " + counter + " triples.");
	}
}
