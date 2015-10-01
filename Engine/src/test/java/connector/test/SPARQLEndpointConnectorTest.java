package connector.test;

import org.junit.Test;

import com.hp.hpl.jena.query.*;

import connector.impl.SPARQLEndpointConnectorImpl;

public class SPARQLEndpointConnectorTest {

	// final String endpointURI = "http://lod.openlinksw.com/sparql";
	// final String endpointURI =
	// "http://aifb-ls3-vm8.aifb.kit.edu:8890/sparql";
	final String endpointURI = "http://localhost:8890/sparql";
	SPARQLEndpointConnectorImpl connector;

	@Test
	public void testConnection() {
		try {
			// String query = "SELECT ?uri WHERE {?uri a ?o} limit 10";
			String query = "SELECT ?uri WHERE { values ?uri {<http://dbpedia.org/resource/Cancer>} } limit 10";

			connector = new SPARQLEndpointConnectorImpl(endpointURI);
			connector.executeQuery(query);
			ResultSet rs = connector.getResultSet();

			// assertNotNull(rs);
			// assertTrue(rs.hasNext());

			while (rs.hasNext()) {
				QuerySolution querySolution = rs.next();
				System.out.println(querySolution.get("uri"));
			}

			connector.closeConnection();

			// assertNull(rs);

		} catch (Exception e) {
			System.out.println("Exception thrown: " + e.getMessage());
		}
	}

}
