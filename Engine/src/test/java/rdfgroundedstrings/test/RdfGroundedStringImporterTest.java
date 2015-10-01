package rdfgroundedstrings.test;

import static org.junit.Assert.*;

import java.util.Collection;

import org.junit.Test;

import rdfgroundedstrings.impl.RdfGroundedString;
import rdfgroundedstrings.impl.RdfGroundedStringImporter;

public class RdfGroundedStringImporterTest {

	@Test
	public void importerTest() {
		System.out.println("Load rdf-grounded-strings:");
		RdfGroundedStringImporter importer = new RdfGroundedStringImporter();
		Collection<RdfGroundedString> groundedStrings = importer
				.importRdfGroundedStrings();

		for (RdfGroundedString groundedString : groundedStrings) {
			System.out.println(groundedString);
		}

		assertNotNull(groundedStrings);
	}

}
