package lucene;

import inputmanagement.InputManager;
import inputmanagement.candidates.Candidate;
import inputmanagement.candidates.RdfCandidateTypes;
import inputmanagement.candidates.impl.EntityCandidate;
import inputmanagement.candidates.impl.GeneralCandidate;
import inputmanagement.candidates.impl.RelationCandidate;
import inputmanagement.impl.CustomComparator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import com.ontologycentral.nxindexer.keyword.KeywordSearcher;

import configuration.ConfigManager;
import configuration.impl.ConfigManagerImpl;
import edu.stanford.lemmatizer.StanfordLemmatizer;

public class LuceneSearcher {

	private Logger logger;
	private ConfigManager configManager = new ConfigManagerImpl();

	private final String entitiesIndex = configManager.getHome()
			+ "lucene-index/entities_index/lucene";
	private final String relationsIndex = configManager.getHome()
			+ "lucene-index/relations_index/lucene";
	private final String classesIndex = configManager.getHome()
			+ "lucene-index/classes_index/lucene";
	private final String lemonDictionaryIndex = configManager.getHome()
			+ "lucene-index/dbpedia_ontology_dict/lucene";
	public static final int CANDIDATE = 0;
	public static final int ENTITY = 1;
	public static final int CLASS = 2;
	public static final int RELATION = 3;

	public static final int maxNumberEntityLabels = 610;
	public static final int maxNumberClassLabels = 16;
	public static final int maxNumberRelationLabels = 10;
	public static final int maxNumberLemonLabels = 10;

	private int numberOfHits = 20;

	public LuceneSearcher() {
		ConfigManager configManager = new ConfigManagerImpl();
		logger = configManager.getLogger();
	}

	@SuppressWarnings("unchecked")
	public List<EntityCandidate> searchEntity(String query,
			InputManager inputManager) throws CorruptIndexException,
			IOException, ParseException {
		return (List<EntityCandidate>) search(query, ENTITY, inputManager);
	}

	@SuppressWarnings("unchecked")
	public List<EntityCandidate> searchClass(String query,
			InputManager inputManager) throws CorruptIndexException,
			IOException, ParseException {
		List<EntityCandidate> classCandidates = (List<EntityCandidate>) search(
				query, CLASS, inputManager);
		classCandidates.forEach(candidate -> candidate
				.setType(RdfCandidateTypes.CLASS));
		return classCandidates;
	}

	@SuppressWarnings("unchecked")
	public List<RelationCandidate> searchRelation(String query,
			InputManager inputManager) throws CorruptIndexException,
			IOException, ParseException {
		return (List<RelationCandidate>) search(query, RELATION, inputManager);
	}

	@SuppressWarnings("unchecked")
	public List<Candidate> searchLemonEntry(String query,
			InputManager inputManager) throws CorruptIndexException,
			IOException, ParseException {
		return (List<Candidate>) search(query, CANDIDATE, inputManager);
	}

	public List<? extends Candidate> search(String input_term, int type,
			InputManager inputManager) throws CorruptIndexException,
			IOException, ParseException {
		File indexDir = null;
		int maxLabels = Integer.MAX_VALUE;
		List<Candidate> candidates = new ArrayList<Candidate>();

		// handle empty input parameters
		if (input_term == null)
			return candidates;
		if (input_term.equalsIgnoreCase(""))
			return candidates;

		String term = input_term;

		// clean term
		term = term.replaceAll("\\-[0-9]+_", " ").replaceAll("\\-[0-9]+", "");
		term = term.replace(" 's ", "'s ");
		String original_term = term.toLowerCase();

		// lemmatize if needed
		if (inputManager.isActiveOption("LuceneStandardMapper:Lemmatize", "true") ) {
			StanfordLemmatizer lemmatizer = inputManager.getLemmatizer();
			term = lemmatizer.lemmatize(term);
		}

		// remove stopwords
		if (inputManager.isActiveOption("LuceneStandardMapper:StopwordRemoval", "true") ) {
			MyStemmer stemmer = new MyStemmer();
			term = stemmer.removeStopwords(term);
		}
		
		// prepare term for the Lucene searcher
		//		term = "\"" + term + "\"";
		// term = term + "~";

		switch (type) {
		case CANDIDATE:
			indexDir = new File(lemonDictionaryIndex);
			maxLabels = 30;
			break;
		case ENTITY:
			indexDir = new File(entitiesIndex);
			maxLabels = maxNumberEntityLabels;
			break;
		case CLASS:
			indexDir = new File(classesIndex);
			maxLabels = maxNumberClassLabels;
			break;
		case RELATION:
			indexDir = new File(relationsIndex);
			maxLabels = maxNumberRelationLabels;
			break;
		default:
			throw new InvalidParameterException(
					"Wrong type parameter. Only 0=CANDIDATE, 1=ENTITY, 2=CLASS or 3=RELATION are permitted.");
		}

		StandardAnalyzer analyzer = new StandardAnalyzer();
		Path indexPath = indexDir.toPath();

		Directory fsDir = FSDirectory.open(indexPath);
		IndexReader reader = DirectoryReader.open(fsDir); // .open(indexPath);
		IndexSearcher searcher = new IndexSearcher(reader);
		// Similarity sim = new CustomSimilarity();
		// searcher.setSimilarity(sim);

		// every literal is stored in its own label
		// therefore a variable number of fields exists,
		// naming: "labels1", "labels2", ... , "labels<maxLabels>"
		String[] labels = getLabelsArray(maxLabels);
		QueryParser queryParser = new MultiFieldQueryParser(labels, analyzer);

		// create the query
		Query q = queryParser.parse(term);

		// get three times more hits than necessary in order to still have
		// enough after deleting duplicates
		// (this is not deterministic)
		TopDocs hits = searcher.search(q, numberOfHits * 3);
		ScoreDoc[] scoreDocs = hits.scoreDocs;
		for (int n = 0; n < scoreDocs.length; ++n) {
			ScoreDoc sd = scoreDocs[n];

			float score = sd.score;
			int docId = sd.doc;
			Document d = searcher.doc(docId);
			List<IndexableField> fields = d.getFields();

			String uri = "";
			String label = "";
			String keywords = "";
			int numberOfLabelsContainingTerm = 0;
			int boost = 1;
			try {
				for (IndexableField f : fields) {
					if ("node".equalsIgnoreCase(f.name())) {
						uri = f.stringValue();
					} else if (f.name().toLowerCase()
							.contains(KeywordSearcher.LABELS)) {

						String[] words = original_term.split(" ");
						for (int i = 0; i < words.length; i++) {
							// normalize the quantity of found fields in the end
							// score
							if (f.stringValue().toLowerCase()
									.contains(words[i])) {
								numberOfLabelsContainingTerm++;
							}
							//							String test = f.stringValue();
							// boost in case of a partial match
							if (f.stringValue().equalsIgnoreCase(words[i])) {
								boost += 1;
							}
						}

						// boost in case of a perfect match
						if (f.stringValue().equalsIgnoreCase(original_term)) {
							boost += 2;
						}

						label += f.stringValue() + " - ";
					} else if ("keywords".equalsIgnoreCase(f.name())) {
						keywords = f.stringValue();
					}
				}
			} catch (Exception e) {
				logger.error("Error during lucene search for " + type, e);
			}

			// normalizing the score
			// score by Lucene regards all other matching fields of the
			// document, too.
			// This behaviour is not wanted here
			if (!inputManager.isActiveOption(
					"LuceneStandardMapper:DivideByOccurrence", true))
				numberOfLabelsContainingTerm = 1;
			if (numberOfLabelsContainingTerm == 0)
				numberOfLabelsContainingTerm = 1;
			if (!inputManager.isActiveOption(
					"LuceneStandardMapper:BoostPerfectMatch", true))
				boost = 1;

			float new_score = (float) (score * boost / Math.sqrt(numberOfLabelsContainingTerm));

			candidates.add(createCandidate(uri, new_score, label, keywords, type));
		}

		// only regard the entity with the highest score
		candidates = removeDuplicates(candidates);

		// normalize the scores into interval [0, 1]
		// remark: not used anymore, original score applys better
		// normalizeScores(candidates);

		candidates.sort(new CustomComparator());

		// not necessary since 4.0.0
		// searcher.close();
		return candidates;
	}

	private String[] getLabelsArray(int maxLabels) {
		String[] labels = new String[maxLabels];

		for (int i = 1; i <= labels.length; i++) {
			labels[i - 1] = KeywordSearcher.LABELS + i;
		}
		return labels;
	}

	@SuppressWarnings("unused")
	private void normalizeScores(List<Candidate> candidates) {
		double max_score = 0.0;
		double min_score = Double.MAX_VALUE;

		for (Candidate candidate : candidates) {
			if (candidate.getScore() > max_score)
				max_score = candidate.getScore();
			if (candidate.getScore() < min_score)
				min_score = candidate.getScore();
		}

		for (Candidate candidate : candidates) {
			double new_score = (candidate.getScore() - min_score)
					/ (max_score - min_score);
			candidate.setScore(new_score);
		}

	}

	public List<Candidate> removeDuplicates(List<? extends Candidate> candidates) {
		List<Candidate> unique_candidates = new ArrayList<Candidate>();
		for (Candidate candidate1 : candidates) {
			boolean isDuplicate = false;
			for (Candidate candidate2 : unique_candidates) {
				if (candidate1.getText().equalsIgnoreCase(candidate2.getText())) {
					isDuplicate = true;
				}
			}
			if (!isDuplicate && unique_candidates.size() < numberOfHits
					&& candidate1 != null) {
				unique_candidates.add(candidate1);
			}
		}
		return unique_candidates;
	}

	private Candidate createCandidate(String uri, float score, String label,
			String keywords, int type) {
		switch (type) {
		case CANDIDATE:
			return new GeneralCandidate(uri, score, keywords);
		case ENTITY:
			return new EntityCandidate(uri, score, label);
		case CLASS:
			return new EntityCandidate(uri, score, label);
		case RELATION:
			return new RelationCandidate(uri, score, label);
		}
		return null;
	}

	public void setNumberOfHits(int numberOfHits) {
		this.numberOfHits = numberOfHits;
	}

	public int getNumberOfHits() {
		return numberOfHits;
	}

}