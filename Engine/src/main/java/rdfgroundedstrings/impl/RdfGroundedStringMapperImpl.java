package rdfgroundedstrings.impl;

import inputmanagement.candidates.impl.RelationCandidate;
import inputmanagement.impl.CustomComparator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import configuration.ConfigManager;
import configuration.impl.CommonMethods;
import configuration.impl.ConfigManagerImpl;
import rdfgroundedstrings.RdfGroundedStringMapper;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;

/**
 * Based on the SimMetrics Library by Sam Chapman (2005)
 * @author Sebastian Bader (sebastian.bader@student.kit.edu)
 *
 */
public class RdfGroundedStringMapperImpl implements RdfGroundedStringMapper {

	private Logger logger;

	private static Collection<RdfGroundedString> rdfGroundedStrings;
	private List<RelationCandidate> relationCandidates;

	private double threshold = 0.5;

	public RdfGroundedStringMapperImpl() {
		RdfGroundedStringImporter importer = new RdfGroundedStringImporter();
		if (rdfGroundedStrings == null) rdfGroundedStrings = importer.importRdfGroundedStrings();

		ConfigManager configManager = new ConfigManagerImpl();
		this.logger = configManager.getLogger();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<RelationCandidate> findRelationCandidates(String text) {
		relationCandidates = new ArrayList<RelationCandidate>();

		Pattern p = Pattern.compile("\\?[a-zA-Z]+");
		Matcher m = p.matcher(text);
		if (m.matches()) {
			relationCandidates.add(new RelationCandidate(text, 1, ""));
			logger.info("Identified relation " + text
					+ " as variable. No further processing.");
			return relationCandidates;
		}

		rdfGroundedStrings
				.forEach(rdfGroundedString -> {
					List<RelationCandidate> tmp_relationCandidates = matchGroundedString(
							rdfGroundedString, text);
					if (tmp_relationCandidates != null)
						relationCandidates.addAll(tmp_relationCandidates);
				});

		relationCandidates = (List<RelationCandidate>) CommonMethods
				.removeDuplicates(relationCandidates);
		relationCandidates.sort(new CustomComparator());

		if (relationCandidates.isEmpty()) {
			return null;
		} else {
			for (RelationCandidate rc : relationCandidates) {
				logger.info("Found RelationCandidate: " + rc.toString());
			}
			return relationCandidates;
		}
	}

	private List<RelationCandidate> matchGroundedString(
			RdfGroundedString rdfGroundedString, String text) {
		String pattern = rdfGroundedString.getPattern();

		logger.trace("Compare '" + rdfGroundedString.getName()
				+ "' with text '" + text + "'");
		logger.trace("Pattern of  RdfGroundedString: '" + pattern);

		Levenshtein metric = new Levenshtein();
		double similarity = metric.getSimilarity(text, pattern);

		logger.trace("Calculated similarity is " + similarity);

		if (similarity >= threshold) {
			logger.info("Calculated Similarity (" + similarity + ") of '"
					+ rdfGroundedString.getName() + "' [Pattern '" + pattern
					+ "'] and predicate '" + text + "' is above threshold of "
					+ threshold
					+ " therefore adding relations of RdfGroundedString.");
			rdfGroundedString.setScores(similarity * 100);
			return rdfGroundedString.getRelations();
		} else {
			logger.trace("Calculated Similarity (" + similarity + ") of '"
					+ rdfGroundedString.getName() + "' [Pattern '" + pattern
					+ "'] and predicate '" + text + "' is below threshold of "
					+ threshold + " therefore RdfGroundedString is ignored.");
			return null;
		}
	}

}
