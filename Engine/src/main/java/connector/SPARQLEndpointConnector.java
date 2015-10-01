package connector;

import com.hp.hpl.jena.query.ResultSet;

/**
 * 
 * @author Sebastian Bader
 *
 */
public interface SPARQLEndpointConnector {

	public ResultSet executeQuery(String sparqlQuery);

	public ResultSet getResultSet();

	public void closeConnection();

	public boolean isValidSparql(String sparqlQuery);

	public boolean uriExistsInGraph(String uri);

	public boolean executeAsk(String pattern);

}
