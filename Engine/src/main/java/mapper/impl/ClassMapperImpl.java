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
import mapper.ClassMapper;
import inputmanagement.InputManager;
import inputmanagement.candidates.RdfCandidateTypes;
import inputmanagement.candidates.impl.EntityCandidate;
import inputmanagement.impl.InputManagerImpl;

public class ClassMapperImpl implements ClassMapper {

	private Logger logger;
	private int maxPathDepth = 2;
	private InputManager inputManager;

	public ClassMapperImpl(InputManager inputManager) {
		ConfigManager configManager = new ConfigManagerImpl();
		this.logger = configManager.getLogger();

		this.inputManager = inputManager;
	}

	public List<EntityCandidate> getClassCandidates(String class_term) {
		return getClassCandidatesWithLucene(class_term);
	}

	public List<EntityCandidate> getClassCandidatesWithEnvironment(
			String class_term) {
		List<EntityCandidate> classCandidates = getClassCandidatesWithLucene(class_term);
		for (EntityCandidate candidate : classCandidates) {
			retrieveClassEnvironement(candidate);
		}
		return classCandidates;
	}

	public List<EntityCandidate> getClassCandidatesWithLucene(String class_term) {
		List<EntityCandidate> classes = new ArrayList<EntityCandidate>();

		// 1) entities starting with "?" or "_:" are treated like variables/empty nodes
		Pattern p = Pattern.compile("((\\?[a-zA-Z]+)|(_:[a-zA-Z]+))");
		Matcher m = p.matcher(class_term);
		if (m.matches()) {
			p = Pattern.compile("\\?variable");
			m = p.matcher(class_term);
			
			EntityCandidate classCandidate;
			if (m.matches()) {
				classCandidate = new EntityCandidate(class_term, 100,
					"");
			} else {
				classCandidate = new EntityCandidate(class_term, 1,
						"");
			}
			classCandidate.setType(RdfCandidateTypes.CLASS);
			classes.add(classCandidate);
			logger.info("Identified class " + class_term
					+ " as variable. No further processing.");
			return classes;
		}

		// 2) entity already is a valid uri
		p = Pattern.compile("<http://.*>");
		m = p.matcher(class_term);
		if (m.matches()) {

			EntityCandidate classCandidate = new EntityCandidate(class_term, 100,
					"");
			classCandidate.setType(RdfCandidateTypes.CLASS);

			logger.info("Identified class " + class_term
					+ " as valid uri. No further processing.");

			classes.add(classCandidate);
			return classes;
		}

		// 3) assume that the entity exists as a resource in the Lucene index
		try {
			LuceneSearcher searcher = new LuceneSearcher();
			logger.info("Search for '" + class_term
					+ "' in Lucene class index.");

			List<EntityCandidate> candidates = searcher.searchClass(class_term,
					inputManager);

			return candidates;
		} catch (Exception e1) {
			logger.error("Exception during entity detection. Could not identify entity '"
					+ class_term + "' :", e1);
		}

		logger.debug("Could not identify entity " + class_term + ".");

		return null;
	}

	private void retrieveClassEnvironement(EntityCandidate classCandidate) {

		SPARQLEndpointConnector connector = InputManagerImpl.endpointConnector;
		if (connector != null) {

			logger.info("Search for environment of " + classCandidate.getText());
			List<RDFPath> environment = new ArrayList<RDFPath>();
			for (int pathDepth = 1; pathDepth <= maxPathDepth; pathDepth++) {

				String sparql = "SELECT * WHERE { GRAPH ?g {  "
						+ classCandidate.getUri() + " ?r1  ?x1  . ";
				sparql += " FILTER (!isLiteral(?x1) ) . ";
				for (int i = 1; i < pathDepth; i++) {
					sparql += " ?x" + i + " ?r" + (i + 1) + " ?x" + (i + 1)
							+ " . ";
					sparql += " FILTER (!isLiteral(?x" + (i + 1)
							+ ") || langMatches(lang(?x" + (i + 1)
							+ "), \"EN\")) . ";
				}
				sparql += "}} LIMIT 100";

				connector.executeQuery(sparql);

				ResultSet rs = connector.getResultSet();

				while (rs.hasNext()) {
					QuerySolution qs = rs.next();
					List<Triple> path = new ArrayList<Triple>();

					String subject = classCandidate.getUri();
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
							+ classCandidate.getText() + ": "
							+ rdfPath.toString());
				}

				connector.closeConnection();
			}

			classCandidate.setEnvironment(environment);
		} else {
			logger.error("Could not search vor environment of "
					+ classCandidate.getText()
					+ " as no SparqlConnector is available.");
		}

	}

}
