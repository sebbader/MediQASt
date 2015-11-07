import static org.junit.Assert.*;

import org.junit.Test;

import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtuosoUpdateFactory;
import virtuoso.jena.driver.VirtuosoUpdateRequest;


public class InsertPredicateInfo {

	@Test
	public void test() {
		VirtGraph set = new VirtGraph ("jdbc:virtuoso://localhost:1111", "dba", "dba");

		String str = "insert { graph <http://dbpedia.org/ontology> {"
				+ "<http://dbpedia.org/ontology/icd11> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> ."
				+ "<http://dbpedia.org/ontology/icd11> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#DatatypeProperty> ."
				+ " "
				+ "<http://dbpedia.org/ontology/icd11> <http://www.w3.org/2000/01/rdf-schema#domain> <http://dbpedia.org/ontology/Disease> ."
				+ "<http://dbpedia.org/ontology/icd11> <http://www.w3.org/2000/01/rdf-schema#label> \"ICD11\"@en ."
				+ "}}";
		System.out.println(str);	
		VirtuosoUpdateRequest vur = VirtuosoUpdateFactory.create(str, set);
		vur.exec();
	}

}
