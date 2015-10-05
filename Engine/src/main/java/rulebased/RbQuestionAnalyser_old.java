package rulebased;

import inputmanagement.impl.QueryTriple;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
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
import edu.stanford.nlp.trees.GrammaticalRelation;
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
public class RbQuestionAnalyser_old {

	private Logger logger;

	private enum CASE {
		subj, obj
	};

	private final String[] object_WhiteList = { "nmod", "obj" }; // these tags
																	// SHOULD be
																	// regarded
																	// during
																	// identification
																	// of query
																	// triples
	private final String[] object_BlackList = { "nmod:poss" }; // these tags
																// should NOT be
																// regarded
	private final String[] subject_WhiteList = { "subj" }; // these tags SHOULD
															// be regarded
															// during
															// identification of
															// query triples
	private final String[] subject_BlackList = {}; // these tags should NOT be
													// regarded

	public RbQuestionAnalyser_old() {
		ConfigManager configManager = new ConfigManagerImpl();
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
					.loadSingularMergingRelations("../parameter/casia/casia-rules.xml");
			Collection<TypedDependency> compressed_dependency_tree = mergeDependencyTree(
					dependency_tree, grammatiacalRelationsToMerge);

			List<RbConstructingRule> rules = loader
					.loadRules("../parameter/casia/casia-rules.xml");
			for (RbConstructingRule rule : rules) {
				// QueryTriple queryTriple =
				// rule.evaluateRule(compressed_dependency_tree,
				// query_triples.isEmpty());
				// if (queryTriple != null) query_triples.add(queryTriple);
			}

		} catch (Exception e) {
			logger.error("Error in CasiaQuestionAnalyser.getQueryTriples("
					+ question + ") :", e);
		}
		// follow Rule1: if X --subj-- Y --obj-- Z when add <X, Y, Z>
		// query_triples.addAll(findSubjObj(compressed_dependency_tree));

		// follow Rule2: if X --obj-- Y --obj-- Z when add <X, Y, Z>
		// query_triples.addAll(findObjObj(compressed_dependency_tree));

		// connect Triples following the Rule: if <A, B, C> and <B, C, D> when
		// replace with <A, B, ?C> and <?C, C, D>
		// Collection<QueryTriple> query_triples_connected =
		// connectQueryTriples(query_triples);

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
					List<String> newWords = new ArrayList<>();
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
						String rel = typedDependency.reln().toString();
						String gov = typedDependency.gov().backingLabel()
								.toString().toLowerCase();
						String dep = typedDependency.dep().backingLabel()
								.toString().toLowerCase();

						// merging dependencies shall not used here
						if (grammatiacalRelationsToMerge.containsKey(rel))
							continue;

						if (gov.contains(word1.toLowerCase())) {
							typedDependency.setGov(getNewWord(gov + " "
									+ newWord));
						}
						if (gov.contains(word2.toLowerCase())) {
							typedDependency.setGov(getNewWord(gov + " "
									+ newWord));
						}
						if (dep.contains(word1.toLowerCase())) {
							typedDependency.setDep(getNewWord(dep + " "
									+ newWord));
						}
						if (dep.contains(word2.toLowerCase())) {
							typedDependency.setDep(getNewWord(dep + " "
									+ newWord));
						}
					}
				}
			}
		}
		logger.info("Compressed Dependency Tree: " + merged_dependency_tree);
		return merged_dependency_tree;
	}

	private IndexedWord getNewWord(String word) {
		List<String> words = Arrays.asList(word.split("\\s+"));
		words.sort(new PhraseComparator());

		String newWord = "";
		for (String w : words) {
			if (!newWord.toLowerCase().contains(w.toLowerCase()))
				newWord += w + " ";
		}
		return new IndexedWord(new Word(newWord));
	}

	/**
	 * Heuristic to find query triples in a preprocessed set of
	 * TypedDependencies. Only combinations matching the pattern:
	 * "B --obj-- C --obj-- D" are collected
	 * 
	 * @param dependency_tree
	 * @return query triples
	 */
	private List<QueryTriple> findObjObj(
			Collection<TypedDependency> dependency_tree) {
		List<QueryTriple> triples = new ArrayList<QueryTriple>();

		for (TypedDependency dependency : dependency_tree) {

			// only allow obj/nmod as start dependency
			String relname = dependency.reln().getShortName();
			if (isObjectWhiteListed(relname) && !isObjectBlackListed(relname)
					&& !relname.contains("KILL")) {
				triples.addAll(findConnectingObject(dependency, CASE.obj,
						dependency_tree));
			}
		}
		return triples;
	}

	/**
	 * Heuristic to find query triples in a preprocessed set of
	 * TypedDependencies. Only combinations matching the pattern:
	 * "X --nsubj-- Y --nmod-- Z" are collected
	 * 
	 * @param dependency_tree
	 * @return query triples
	 */
	private List<QueryTriple> findSubjObj(
			Collection<TypedDependency> dependency_tree) {
		List<QueryTriple> triples = new ArrayList<QueryTriple>();

		for (TypedDependency dependency : dependency_tree) {

			// only allow nsubj as start dependency
			if (isSubjectWhiteListed(dependency.reln().getShortName())) {
				triples.addAll(findConnectingObject(dependency, CASE.subj,
						dependency_tree));
			}
		}
		return triples;
	}

	private Collection<QueryTriple> findConnectingObject(
			TypedDependency first_dependency, CASE status,
			Collection<TypedDependency> dependency_tree) {
		// search for nmod/obj which are connected by a shared term
		List<QueryTriple> triples = new ArrayList<QueryTriple>();

		String gov1 = first_dependency.gov().toString();
		String dep1 = first_dependency.dep().toString();

		for (TypedDependency second_dependency : dependency_tree) {
			// only use nmod or obj to continue
			String relname = second_dependency.reln().getShortName();
			if (!isObjectWhiteListed(relname))
				continue;
			String gov2 = second_dependency.gov().toString();
			String dep2 = second_dependency.dep().toString();

			QueryTriple newtriple = null;
			switch (status) {
			case obj:
				if (gov1.equalsIgnoreCase(dep2)) {
					newtriple = new QueryTriple(dep1, gov1, gov2);
				} else if (dep1.equalsIgnoreCase(gov2)) {
					newtriple = new QueryTriple(gov1, dep1, dep2);
				}
				break;
			case subj:
				if (gov1.equalsIgnoreCase(gov2)) {
					newtriple = new QueryTriple(dep1, gov1, dep2);
				} else if (gov1.equalsIgnoreCase(dep2)) {
					newtriple = new QueryTriple(dep1, gov1, gov2);
				} else if (dep1.equalsIgnoreCase(gov2)) {
					newtriple = new QueryTriple(gov1, dep1, dep2);
				} else if (dep1.equalsIgnoreCase(dep2)) {
					newtriple = new QueryTriple(dep2, dep1, gov2);
				}
				break;
			default:
				break;
			}

			if ((newtriple != null)
					&& (!dep1.equalsIgnoreCase(dep2) || !gov1
							.equalsIgnoreCase(gov2))) {

				triples.add(newtriple);

				// mark dependency as already used by changing its Reln type
				second_dependency.setReln(GrammaticalRelation.KILL);
			}
		}

		return triples;
	}

	/**
	 * if <A, B, C> and <B, C, D> when replace with <A, B, ?C> and <?C, C, D>
	 * 
	 * @param query_triples
	 * @return
	 */
	private Collection<QueryTriple> connectQueryTriples(
			Collection<QueryTriple> query_triples) {

		for (QueryTriple triple1 : query_triples) {
			for (QueryTriple triple2 : query_triples) {

				if (triple1.getPredicate().equalsIgnoreCase(
						triple2.getEntity1())
						&& triple1.getEntity2().equalsIgnoreCase(
								triple2.getPredicate())) {
					triple1.setEntity2("?" + triple1.getEntity2());
					triple2.setEntity1(triple1.getEntity2());
				}
			}
		}
		return query_triples;
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

	private Collection<TypedDependency> compressDependencyTree(
			Collection<TypedDependency> dependency_tree) {
		Collection<TypedDependency> compressed_dependency_tree = new ArrayList<TypedDependency>();

		Iterator<TypedDependency> iter = dependency_tree.iterator();
		while (iter.hasNext()) {
			TypedDependency dependency = iter.next();
			String relName = dependency.reln().toString();
			String word1 = dependency.gov().backingLabel().toString();
			String word2 = dependency.dep().backingLabel().toString();

			if (isSubjectWhiteListed(relName) || isObjectWhiteListed(relName)) {
				String words = findConnectedWords(word1, dependency_tree);
				IndexedWord new_gov = new IndexedWord(new Word(words));

				words = findConnectedWords(word2, dependency_tree);
				IndexedWord new_dep = new IndexedWord(new Word(words));

				TypedDependency own_dependency = new TypedDependency(
						dependency.reln(), new_gov, new_dep);

				compressed_dependency_tree.add(own_dependency);
			}
		}
		logger.info("Compressed Dependency Tree: " + compressed_dependency_tree);
		return compressed_dependency_tree;
	}

	private String findConnectedWords(String word,
			Collection<TypedDependency> dependency_tree) {
		List<String> connected_words = new ArrayList<String>();
		connected_words.add(word);

		Iterator<TypedDependency> iter = dependency_tree.iterator();
		while (iter.hasNext()) {
			TypedDependency dependency = iter.next();
			String relName = dependency.reln().toString();
			String word1 = dependency.gov().backingLabel().toString();
			String word2 = dependency.dep().backingLabel().toString();

			if (relName.contains("det") || relName.contains("compound")
					|| relName.contains("det") || relName.contains("advmod")) {
				if (word1.equalsIgnoreCase(word))
					connected_words.add(word2);
				if (word2.equalsIgnoreCase(word))
					connected_words.add(word1);
			}
		}
		connected_words.sort(new PhraseComparator());
		return connected_words.toString();
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
			classifier = CRFClassifier
					.getClassifier("../Engine/classifiers/english.all.3class.distsim.crf.ser.gz");
			List<List<CoreLabel>> words = classifier.classify(question);

			// set up grammar and options as appropriate
			LexicalizedParser lp = LexicalizedParser
					.loadModel("../Engine/edu_stanford_nlp/models/lexparser/englishPCFG.ser.gz");

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

	/**
	 * checks whether the input chunk tag is part of the White List array and
	 * therefore shall be used for triple generation
	 * 
	 * @param relname
	 * @return
	 */
	private boolean isObjectWhiteListed(String relname) {
		return isWhiteListed(relname, object_WhiteList, object_BlackList);
	}

	/**
	 * checks whether the input chunk tag is part of the White List array and
	 * therefore shall be used for triple generation
	 * 
	 * @param relname
	 * @return
	 */
	private boolean isSubjectWhiteListed(String relname) {
		return isWhiteListed(relname, subject_WhiteList, subject_BlackList);
	}

	/**
	 * checks whether the input chunk tag is part of the White List array and
	 * therefore shall be used for triple generation
	 * 
	 * @param relname
	 * @return
	 */
	private boolean isWhiteListed(String relname, String[] whiteList,
			String[] blackList) {
		for (String str : whiteList) {
			if (relname.contains(str)) {
				if (!isBlackListed(relname, blackList))
					return true;
			}
		}
		return false;
	}

	/**
	 * checks whether the input chunk tag is part of the White List array and
	 * therefore shall be used for triple generation
	 * 
	 * @param relname
	 * @return
	 */
	private boolean isObjectBlackListed(String relname) {
		return isBlackListed(relname, object_BlackList);
	}

	/**
	 * checks whether the input chunk tag is part of the White List array and
	 * therefore shall be used for triple generation
	 * 
	 * @param relname
	 * @return
	 */
	private boolean isSubjectBlackListed(String relname) {
		return isBlackListed(relname, subject_BlackList);
	}

	/**
	 * checks whether the input chunk tag is part of the White List array and
	 * therefore shall be used for triple generation
	 * 
	 * @param relname
	 * @return
	 */
	private boolean isBlackListed(String relname, String[] blackList) {
		for (String str : blackList) {
			if (relname.contains(str))
				return true;
		}
		return false;
	}
}
