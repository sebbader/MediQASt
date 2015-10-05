package lucene;

import java.io.IOException;

import org.semanticweb.yars.nx.parser.ParseException;

import com.ontologycentral.nxindexer.cli.BuildIndex;

import configuration.ConfigManager;
import configuration.impl.ConfigManagerImpl;

public class LuceneIndexer {

	public void indexEntities() throws IOException, ParseException {
		ConfigManager configManager = new ConfigManagerImpl();

		// use nxindexer (source: aifb.edu) with Lucene 5.1.0
		// String[] args = {"BuildIndex", "-q", "-i",
		// "../Engine/rdf-files/entities/Instances_allDBs_label-only-numbered_sorted_no-relations_no-classes.nq",
		// "-o", "../Engine/lucene-index/entities_index"};
		// Main.main(args);

		String inputDir = configManager.getHome()
				+ "rdf-files/entities/Instances_allDBs_LabelsNamesAndIdentifiers.nq";
		String outputDir = configManager.getHome()
				+ "lucene-index/entities_index";

		String options = "";
		options += " -q";
		// options += " -X";
		options += " -i " + inputDir;
		options += " -o " + outputDir;
		options += " -n " + LuceneSearcher.maxNumberEntityLabels;
		options += " -F " + 50;

		// run /nxindexer/src/com/ontologycentral/nxindexer/cli/BuildIndex.java
		BuildIndex.main(options.split(" "));
	}

	public void indexClasses() throws IOException, ParseException {
		ConfigManager configManager = new ConfigManagerImpl();

		// use nxindexer (source: aifb.edu) with Lucene 5.1.0
		// String[] args = {"BuildIndex", "-q", "-X", "-i",
		// "../Engine/rdf-files/classes/Classes_allDBs_allLiterals_sorted.nq",
		// "-o", "lucene-index/classes_index"};
		// Main.main(args);

		String inputDir = configManager.getHome()
				+ "rdf-files/classes/Classes_allDBs_allLiterals_sorted_noCategories.nq";
		String outputDir = configManager.getHome()
				+ "lucene-index/classes_index";

		String options = "";
		options += " -q";
		// options += " -X";
		options += " -i " + inputDir;
		options += " -o " + outputDir;
		options += " -n " + LuceneSearcher.maxNumberClassLabels;
		options += " -F " + 50;

		// run /nxindexer/src/com/ontologycentral/nxindexer/cli/BuildIndex.java
		BuildIndex.main(options.split(" "));
	}

	public void indexRelations() throws IOException, ParseException {
		ConfigManager configManager = new ConfigManagerImpl();

		// use nxindexer (source: aifb.edu) with Lucene 5.1.0
		// String[] args = {"BuildIndex", "-q", "-i",
		// "../Engine/rdf-files/relations/Relations.nq", "-o",
		// "lucene-index/relations_index"};
		// Main.main(args);

		String inputDir = configManager.getHome()
				+ "rdf-files/relations/Relations.nq";
		String outputDir = configManager.getHome()
				+ "lucene-index/relations_index";

		String options = "";
		options += " -q";
		// options += " -X";
		options += " -i " + inputDir;
		options += " -o " + outputDir;
		options += " -n " + LuceneSearcher.maxNumberRelationLabels;
		options += " -F " + 50;

		// run /nxindexer/src/com/ontologycentral/nxindexer/cli/BuildIndex.java
		BuildIndex.main(options.split(" "));
	}

	public void indexLemonDict() throws IOException, ParseException {
		ConfigManager configManager = new ConfigManagerImpl();

		// use nxindexer (source: aifb.edu) with Lucene 5.1.0
		// String[] args = {"BuildIndex", "-q", "-i",
		// "../Engine/rdf-files/lemon_dictionary/dbpedia_ontology_dict.qt",
		// "-o", "lucene-index/dbpedia_ontology_dict"};
		// Main.main(args);

		String inputDir = configManager.getHome()
				+ "rdf-files/lemon_dictionary/dbpedia_ontology_dict.qt";
		String outputDir = configManager.getHome()
				+ "lucene-index/dbpedia_ontology_dict";

		String options = "";
		options += " -q";
		// options += " -X";
		options += " -i " + inputDir;
		options += " -o " + outputDir;
		options += " -F " + 50;

		// run /nxindexer/src/com/ontologycentral/nxindexer/cli/BuildIndex.java
		BuildIndex.main(options.split(" "));

	}

}
