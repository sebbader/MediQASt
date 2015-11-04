package connector.impl;

import org.apache.jena.atlas.web.HttpException;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.QueryException;

import connector.SPARQLEndpointConnector;

public class SPARQLEndpointConnectorImpl implements SPARQLEndpointConnector {

	private static Logger logger = Logger.getRootLogger();

	private String endpointURI;
	private Query query;
	private QueryExecution qexec;
	private ResultSet resultSet = null;

	public SPARQLEndpointConnectorImpl(String endpointURI) {
		this.endpointURI = endpointURI;
	}

	/**
	 * @throws QueryException
	 */
	public ResultSet executeQuery(String sparqlQuery) throws QueryException {
		logger.trace("Started query: " + sparqlQuery);

		try {
			query = QueryFactory.create(sparqlQuery);
			qexec = QueryExecutionFactory.sparqlService(endpointURI, query);

			resultSet = qexec.execSelect();

			logger.trace("Executed query.");
			return resultSet;
		} catch (QueryException e) {
			logger.error("Error during query execution :", e);
			throw e;
		} catch (HttpException e) {
			logger.error("Http Error, retry...", e);
			return executeQuery(sparqlQuery);
		}

	}

	public boolean executeAsk(String pattern) {
		boolean result = false;

		String query_text = "ASK {" + pattern + "}";
		logger.debug("Started ask: " + query);
		try {
			query = QueryFactory.create(query_text);
			qexec = QueryExecutionFactory.sparqlService(endpointURI, query);

			result = qexec.execAsk();

		} catch (QueryException e) {
			logger.debug("Error during query execution :", e);
			throw e;
		} catch (HttpException e) {
			logger.error("Http Error, retry...", e);
			return executeAsk(pattern);
		}

		return result;
	}

	/**
	 * Tests whether a query is valid sparql by creating a new Jena Query
	 * object. An exception is interpreted as a parsing error and therefore
	 * FALSE is returned otherwise TRUE
	 * 
	 * @param sparqlQuery
	 * @return true if the query is accepted by
	 *         com.hp.hpl.jena.query.QueryFactory
	 */
	public boolean isValidSparql(String sparqlQuery) {
		try {
			@SuppressWarnings("unused")
			Query query = QueryFactory.create(sparqlQuery);
		} catch (Exception e) {
			return false;
		}

		return true;
	}

	public ResultSet getResultSet() {
		return resultSet;
	}

	public void closeConnection() {
		query = null;
		qexec.close();
		logger.trace("Connection to " + endpointURI + " closed succesfully");
	}

	public boolean uriExistsInGraph(String uri) {
		String sparqlQuery = "SELECT * WHERE {GRAPH ?g { " + uri
				+ "?relation1 ?object . " + " ?subject  ?relation2  " + uri
				+ " ." + "}} Limit 2";

		logger.trace("Started query to identify entity: " + sparqlQuery);

		try {
			query = QueryFactory.create(sparqlQuery);
			qexec = QueryExecutionFactory.sparqlService(endpointURI, query);

			ResultSet resultSet = qexec.execSelect();

			boolean found = resultSet.hasNext();
			logger.trace("Executed query to identify entity. Found: " + found);
			return found;
		} catch (QueryException e) {
			return false;
		}
	}
}
