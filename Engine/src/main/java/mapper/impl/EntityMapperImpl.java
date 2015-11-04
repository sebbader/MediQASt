package mapper.impl;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import rdf.Triple;
import rdf.impl.RDFPath;
import rdf.impl.TripleImpl;
import configuration.ConfigManager;
import configuration.impl.ConfigManagerImpl;
import connector.SPARQLEndpointConnector;
import lucene.LuceneSearcher;
import mapper.EntityMapper;
import inputmanagement.InputManager;
import inputmanagement.candidates.RdfCandidateTypes;
import inputmanagement.candidates.impl.EntityCandidate;
import inputmanagement.impl.InputManagerImpl;

public class EntityMapperImpl implements EntityMapper {

	private Logger logger;
	private int maxPathDepth = 1;
	private InputManager inputManager;

	public EntityMapperImpl(InputManager inputManager) {
		ConfigManager configManager = new ConfigManagerImpl();
		this.logger = configManager.getLogger();

		this.inputManager = inputManager;
	}

	public List<EntityCandidate> getEntityCandidates(String entity) {
		return getEntityCandidatesWithLucene(entity);
	}

	public List<EntityCandidate> getEntityCandidatesWithEnvironment(
			String entity) {
		List<EntityCandidate> entityCandidates = getEntityCandidatesWithLucene(entity);
		for (EntityCandidate candidate : entityCandidates) {
			retrieveEntityEnvironement(candidate);
		}
		return entityCandidates;
	}

	/**
	 * finds entities by comparing their labels with the input term only checks
	 * the entity index
	 * 
	 * @param entity
	 * @return
	 */
	private List<EntityCandidate> getEntityCandidatesWithLucene(String entity) {
		List<EntityCandidate> entities = new ArrayList<EntityCandidate>();

		// 1) entities starting with "?" or "_:" are treated like variables/empty nodes
		Pattern p = Pattern.compile("((\\?[a-zA-Z]+)|(_:[a-zA-Z]+))");
		Matcher m = p.matcher(entity);
		if (m.matches()) {
			p = Pattern.compile("\\?variable");
			m = p.matcher(entity);
			
			EntityCandidate entityCandidate;
			if (m.matches()) {
				entityCandidate = new EntityCandidate(entity, 100,
					"");
			} else {
				entityCandidate = new EntityCandidate(entity, 1,
						"");
			}
			entityCandidate.setType(RdfCandidateTypes.ENTITY);
			entities.add(entityCandidate);
			logger.info("Identified entity " + entity
					+ " as variable. No further processing.");
			return entities;
		}

		// 2) entity already is a valid uri
		p = Pattern.compile("<http://.*>");
		m = p.matcher(entity);
		if (m.matches()) {

			EntityCandidate entityCandidate = new EntityCandidate(entity, 100, "");

			logger.info("Identified entity " + entity
					+ " as valid uri. No further processing.");

			entities.add(entityCandidate);
			return entities;
		}

		// 3) assume that the entity exists as a resource in the Lucene index
		try {

			LuceneSearcher searcher = new LuceneSearcher();
			logger.info("Search for '" + entity + "' in Lucene entity index.");

			List<EntityCandidate> candidates = searcher.searchEntity(entity,
					inputManager);

			return candidates;
		} catch (Exception e1) {
			logger.error("Exception during entity detection. Could not identify entity "
					+ entity + ".");
			e1.printStackTrace();
		}

		logger.debug("Could not identify entity " + entity + ".");

		return null;
	}

	private void retrieveEntityEnvironement(EntityCandidate entityCandidate) {

		SPARQLEndpointConnector connector = InputManagerImpl.endpointConnector;
		if (connector != null) {

			logger.info("Search for environment of "
					+ entityCandidate.getText());
			List<RDFPath> environment = new ArrayList<RDFPath>();
			for (int pathDepth = 1; pathDepth <= maxPathDepth; pathDepth++) {

				String sparql = "SELECT * WHERE { \n" + "GRAPH ?g {  " + "{ "
						+ entityCandidate.getUri() + " ?r1  ?x1  . } \n"
						+ "UNION \n" + "{ ?x1 ?r1 " + entityCandidate.getUri()
						+ " . } \n";
				sparql += " FILTER ( !isLiteral(?x1) ) . \n ";
				for (int i = 1; i < pathDepth; i++) { // TODO in both directions
														// as above...
					sparql += " ?x" + i + " ?r" + (i + 1) + " ?x" + (i + 1)
							+ " . ";
					sparql += " FILTER (!isLiteral(?x" + (i + 1) + ") ) . ";
				}
				sparql += "}}";

				connector.executeQuery(sparql);

				ResultSet rs = connector.getResultSet();

				while (rs.hasNext()) {
					QuerySolution qs = rs.next();
					List<Triple> path = new ArrayList<Triple>();

					String subject = entityCandidate.getUri();
					String predicate = "<" + qs.get("r1").toString() + ">";
					String object;
					if (qs.get("x1").isLiteral()) {
						object = qs.get("x1").toString();
					} else {
						object = "<" + qs.get("x1").toString() + ">";
					}
					path.add(new TripleImpl(subject, predicate, object));

					for (int i = 1; i < pathDepth; i++) {
						subject = "<" + qs.get("x" + i).toString() + ">";
						predicate = "<" + qs.get("r" + (i + 1)).toString()
								+ ">";
						if (qs.get("x" + (i + 1)).isLiteral()) {
							object = qs.get("x" + (i + 1)).toString();
						} else {
							object = "<" + qs.get("x" + (i + 1)).toString()
									+ ">";
						}
						path.add(new TripleImpl(subject, predicate, object));
					}
					RDFPath rdfPath = new RDFPath(path);
					environment.add(rdfPath);
					logger.debug("Found environment for "
							+ entityCandidate.getText() + ": "
							+ rdfPath.toString());
				}

				connector.closeConnection();
			}

			entityCandidate.setEnvironment(environment);
		} else {
			logger.error("Could not search vor environment of "
					+ entityCandidate.getText()
					+ " as no SparqlConnector is available.");
		}

	}

}
