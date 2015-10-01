package rdfgroundedstrings.impl;

import inputmanagement.candidates.impl.RelationCandidate;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.log4j.Logger;

import configuration.ConfigManager;
import configuration.impl.ConfigManagerImpl;

public class RdfGroundedStringImporter {
	private ConfigManager configManager;

	// private String pattern = ".*[(G_)S]0.*[(G_)S]1.*";
	private final String path;

	private Logger logger;

	public RdfGroundedStringImporter() {
		configManager = new ConfigManagerImpl();
		this.logger = configManager.getLogger();
		
		path = configManager.getHome() + "rdf-grounded-strings";
	}

	public Collection<RdfGroundedString> importRdfGroundedStrings() {
		Collection<RdfGroundedString> imported_grounded_strings = new ArrayList<RdfGroundedString>();

		try {
			Files.walk(Paths.get(path))
					.forEach(
							filePath -> {
								if (Files.isRegularFile(filePath)) {
									RdfGroundedString rdfGroundedString = loadRdfGroundedString(filePath);
									if (rdfGroundedString != null)
										imported_grounded_strings
												.add(rdfGroundedString);
									logger.debug("Read file "
											+ filePath.getFileName());
								}
							});
		} catch (IOException e) {
			logger.error("Failed loading rdf-grounded-strings.", e);
		}

		return imported_grounded_strings;
	}

	private RdfGroundedString loadRdfGroundedString(Path filePath) {
		RdfGroundedString rdfGroundedString = null;
		String string_pattern = "";
		List<RelationCandidate> relations = new ArrayList<RelationCandidate>();

		try (BufferedReader br = Files.newBufferedReader(filePath)) {
			String line;

			while ((line = br.readLine()) != null) {
				if (line.startsWith("part: ")) {
					string_pattern = line.substring(7, line.length() - 1);
				}

				String pattern = ".*[(G_)S]0.*[(G_)S]1.*";
				if (line.matches(pattern)) {
					String uri = line;
					uri = uri.replaceAll("\\?S[01]", "")
							.replaceAll("\\?G_[01]", "").replaceAll(" \\.", "")
							.replaceAll("[<>\\s]", "");

					relations.add(new RelationCandidate(uri, 10));
				}
			}
		} catch (IOException e) {
			logger.error("Failed reading file " + filePath, e);
		}

		if (string_pattern != null && !relations.isEmpty()) {
			for (RelationCandidate relationCandidate : relations) {
				relationCandidate.setLabel(string_pattern);
			}
			rdfGroundedString = new RdfGroundedString(filePath.getFileName()
					.toString(), string_pattern, relations);
		}
		return rdfGroundedString;
	}
}
