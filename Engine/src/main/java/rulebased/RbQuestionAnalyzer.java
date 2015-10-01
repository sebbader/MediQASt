package rulebased;

import inputmanagement.impl.QueryTriple;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import configuration.ConfigManager;
import configuration.impl.ConfigManagerImpl;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;

/**
 * The Casia Question Analyser converts a natural language phrase into
 * information triples (=query triples). These triples serve as the input for
 * the next step in the Casia pipeline: Mapping the found phrases to existing
 * resources.
 * 
 * @author Sebastian Bader (sebastian.bader@student.kit.edu)
 *
 */
public class RbQuestionAnalyzer {

	private Logger logger;
	private ConfigManager configManager;

	public RbQuestionAnalyzer() {
		this.configManager = new ConfigManagerImpl();
		this.logger = configManager.getLogger();
	}

	/**
	 * converts the natural language phrase into a set of query triples,
	 * containing the same amount of information but in a structured form
	 * 
	 * @param question
	 *            natural language input
	 * @return a set of query triples
	 */
	public Collection<QueryTriple> getQueryTriples(String question) {
		RbParameterLoader loader = new RbParameterLoader();

		Collection<TypedDependency> dependency_tree = parseDependencyTree(question);

		Collection<QueryTriple> query_triples = new ArrayList<QueryTriple>();

		try {
			HashMap<String, Boolean> grammatiacalRelationsToMerge = loader
					.loadMergingRelations(configManager.getHome() + "parameter/casia/casia-rules.xml");
			Collection<TypedDependency> compressed_dependency_tree = mergeDependencyTree(
					dependency_tree, grammatiacalRelationsToMerge);

			List<RbRule> rules = loader
					.loadRules(configManager.getHome() + "parameter/casia/casia-rules.xml");
			for (RbRule rule : rules) {
				List<QueryTriple> newQueryTriples = rule.evaluateRule(
						compressed_dependency_tree, query_triples.isEmpty());

				if (newQueryTriples != null && !newQueryTriples.isEmpty()) {
					query_triples.addAll(newQueryTriples);
				}
			}

		} catch (Exception e) {
			logger.error("Error in CasiaQuestionAnalyser.getQueryTriples("
					+ question + ") :", e);
		}

		query_triples.forEach(query_triple -> query_triple.replaceWhxx());

		return query_triples;
	}

	private Collection<TypedDependency> mergeDependencyTree(
			Collection<TypedDependency> dependency_tree,
			HashMap<String, Boolean> grammatiacalRelationsToMerge) {
		Collection<TypedDependency> merged_dependency_tree = new ArrayList<TypedDependency>(
				dependency_tree);

		for (TypedDependency dependency : dependency_tree) {
			String relName = dependency.reln().toString();
			String word1 = dependency.gov().backingLabel().toString();
			String word2 = dependency.dep().backingLabel().toString();

			for (String gr : grammatiacalRelationsToMerge.keySet()) {
				if (gr.equalsIgnoreCase(relName)) {
					List<String> newWords = new ArrayList<String>();
					newWords.addAll(Arrays.asList(word1.split(" ")));
					newWords.addAll(Arrays.asList(word2.split(" ")));
					newWords.sort(new PhraseComparator());
					String newWord = "";
					for (String word : newWords) {
						newWord += word + " ";
					}

					if (grammatiacalRelationsToMerge.get(gr)) {
						// delete the dependency, as it is written in the rule
						// file
						merged_dependency_tree.remove(dependency);
					}

					// replace the new phrase in the other dependencies
					for (TypedDependency typedDependency : merged_dependency_tree) {
						// if (dependency.equals(typedDependency)) continue;

						String rel = typedDependency.reln().toString();
						String gov = typedDependency.gov().backingLabel()
								.toString().toLowerCase();
						String dep = typedDependency.dep().backingLabel()
								.toString().toLowerCase();

						// do not change relations which will be deleted
						if (grammatiacalRelationsToMerge.containsKey(rel)) {
							if (grammatiacalRelationsToMerge.get(rel))
								continue;
						}

						String new_gov = new String(gov);
						String new_dep = new String(dep);

						if (contains(word1, gov)) {
							new_gov = getNewWord(gov + " " + newWord);
						}
						if (contains(word2, gov)) {
							new_gov = getNewWord(gov + " " + newWord);
						}
						if (contains(word1, dep)) {
							new_dep = getNewWord(dep + " " + newWord);
						}
						if (contains(word2, dep)) {
							new_dep = getNewWord(dep + " " + newWord);
						}

						// if the newly created words are the same, they are
						// just overwriting themselfes
						// therefore ignore them
						if (new_gov.equalsIgnoreCase(new_dep)) {
							// do nothing
						} else {
							typedDependency.setGov(new IndexedWord(new Word(
									new_gov)));
							typedDependency.setDep(new IndexedWord(new Word(
									new_dep)));
						}

					}
				}
			}
		}
		logger.info("Compressed Dependency Tree: " + merged_dependency_tree);
		return merged_dependency_tree;
	}

	private boolean contains(String newtext, String oldtext) {
		String[] newTextSet = newtext.split(" ");
		String[] oldTextSet = oldtext.split(" ");

		for (int i = 0; i < newTextSet.length; i++) {
			for (int j = 0; j < oldTextSet.length; j++) {
				if (newTextSet[i].equalsIgnoreCase(oldTextSet[j]))
					return true;
			}
		}
		return false;
	}

	private String getNewWord(String word) {
		List<String> words = Arrays.asList(word.split("\\s+"));
		words.sort(new PhraseComparator());

		String newWord = "";
		for (String w : words) {
			if (!newWord.toLowerCase().contains(w.toLowerCase()))
				newWord += w + " ";
		}
		return newWord;
	}

	/**
	 * Nested helper class to combine the splitted words in correct order.
	 * 
	 * @author Sebastian Bader (sebastian.bader@student.kit.edu)
	 *
	 */
	private class PhraseComparator implements Comparator<String> {

		public int compare(String o1, String o2) {
			int number1 = Integer.parseInt(o1.split("-")[1]);
			int number2 = Integer.parseInt(o2.split("-")[1]);

			if (number1 > number2) {
				return 1;
			} else {
				return -1;
			}
		}

	}

	/**
	 * Creates a Stanford Dependency Tree from the input question
	 * 
	 * @param question
	 *            the input question in natural language
	 * @return the dependency tree
	 */
	public Collection<TypedDependency> parseDependencyTree(String question) {
		AbstractSequenceClassifier<CoreLabel> classifier;
		Collection<TypedDependency> tdl = null;

		try {
			classifier = CRFClassifier.getClassifier(configManager.getHome()
					+ "classifiers/english.all.3class.distsim.crf.ser.gz");
			List<List<CoreLabel>> words = classifier.classify(question);

			// set up grammar and options as appropriate
			LexicalizedParser lp = LexicalizedParser
					.loadModel(configManager.getHome()
							+ "edu_stanford_nlp/models/lexparser/englishPCFG.ser.gz");

			// This option shows loading, sentence-segmenting and tokenizing
			// a file using DocumentPreprocessor.
			TreebankLanguagePack tlp = lp.treebankLanguagePack(); // a
																	// PennTreebankLanguagePack
																	// for
																	// English
			GrammaticalStructureFactory gsf = null;
			if (tlp.supportsGrammaticalStructures()) {
				gsf = tlp.grammaticalStructureFactory();
			}
			// You could also create a tokenizer here (as below) and pass it
			// to DocumentPreprocessor

			Tree parse = lp.apply(words.get(0));
			// parse.pennPrint();

			if (gsf != null) {
				GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
				tdl = gs.typedDependenciesCCprocessed();

				logger.info("Dependency Tree: " + tdl);
			}

		} catch (ClassCastException e) {
			logger.error(e);
		} catch (ClassNotFoundException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		}
		return tdl;
	}

}
