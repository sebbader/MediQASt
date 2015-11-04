package lucene;

import inputmanagement.InputManager;
import inputmanagement.candidates.Candidate;
import inputmanagement.candidates.RdfCandidateTypes;
import inputmanagement.candidates.impl.EntityCandidate;
import inputmanagement.candidates.impl.GeneralCandidate;
import inputmanagement.candidates.impl.RelationCandidate;
import inputmanagement.impl.CustomComparator;
import inputmanagement.impl.GenerateSparqlException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.analyzing.AnalyzingQueryParser;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.complexPhrase.ComplexPhraseQueryParser;
import org.apache.lucene.queryparser.xml.builders.DisjunctionMaxQueryBuilder;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.DisjunctionMaxQuery;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.netlib.util.booleanW;

import com.hp.hpl.jena.sparql.function.library.max;
import com.ontologycentral.nxindexer.keyword.KeywordIndexWriter;
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


		// load index
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




		// every literal is stored in its own label
		// therefore a variable number of fields exists,
		// naming: "labels1", "labels2", ... , "labels<maxLabels>"
		String[] labels = getLabelsArray(maxLabels);
		Similarity sim = new CustomSimilarity();
		searcher.setSimilarity(sim);




		// handle empty input parameters
		if (input_term == null) {
			analyzer.close();
			return candidates;
		} else if (input_term.equalsIgnoreCase("")) {
			analyzer.close();
			return candidates;
		}

		// clean term
		String term = input_term;
		term = term.replaceAll("\\-[0-9]+_", " ").replaceAll("\\-[0-9]+", "");
		term = term.replace(" 's ", "'s ");
		term = term.toLowerCase();
		String original_term = term;


		//
		// handle search Parameter
		//
		// lemmatize if needed
		String query_term = term.replace("-", " ");
		if (inputManager.isActiveOption("LuceneStandardMapper:Lemmatize", "true") ) {
			StanfordLemmatizer lemmatizer = inputManager.getLemmatizer();
			query_term = lemmatizer.lemmatize(query_term);
		}

		// remove stopwords
		if (inputManager.isActiveOption("LuceneStandardMapper:StopwordRemoval", "true") ) {
			MyStemmer stemmer = new MyStemmer();
			query_term = stemmer.removeStopwords(query_term);
		}

		// use own lucene scoring formulae
		boolean customFormula = false;
		if (inputManager.isActiveOption("LuceneStandardMapper:Formula", "own") ) {
			customFormula = true;
		}

		// prepare term for the Lucene searcher by telling Lucene to apply AND for the words of 'term'
		boolean searchPerfectOnly = false;
		boolean searchPerfectAlso = false;
		boolean searchPerfectNo = false;
		if (inputManager.isActiveOption("LuceneStandardMapper:SearchPerfect", "only") ) {
			searchPerfectOnly = true;
			query_term = "\"" + query_term + "\"";
		} else if (inputManager.isActiveOption("LuceneStandardMapper:SearchPerfect", "also") ) {
			searchPerfectAlso = true;
			String[] query_words = query_term.split(" ");
			if (query_words.length > 1) {
				query_term = "\"" + query_term + "\" ";
				for (int i = 0; i < query_words.length; i++) {
					query_term += "\"" + query_words[i] + "\" ";
				}
				query_term = query_term.trim();
			} else {
				query_term = "\"" + query_term + "\"";
			}
		} if (inputManager.isActiveOption("LuceneStandardMapper:SearchPerfect", "no") ) {
			searchPerfectNo = true;
		}


		// prepare term for the Lucene searcher by telling Lucene to apply AND for the words of 'term'
		//boolean fuzzySearch = false;
		String fuzzyParam = "0";
		if (inputManager.isActiveOption("LuceneStandardMapper:FuzzySearch", "true") ) {
			//fuzzySearch = true;
			try {
				fuzzyParam = inputManager.getOption("LuceneStandardMapper:FuzzyParam");
				String fuzzy_query = "";

				if (searchPerfectOnly) {
					fuzzy_query += query_term + "~" + fuzzyParam + " ";
				} else if (searchPerfectAlso) {
					String[] words = query_term.split("\" \"");
					for (String word : words) {
						fuzzy_query += "\"" + word.replace("\"", "") + "\"~" + fuzzyParam + " ";
					}
				} else {
					for (String word : query_term.split(" ")) {
						fuzzy_query += word + "~" + fuzzyParam + " ";
					}
				}
				query_term = fuzzy_query.trim();
			} catch (GenerateSparqlException e) {
				logger.error("FuzzySearch activated but FuzzyParam not set. No Lucene FuzzySearch apllied.");
			}
		} 

		try {
			numberOfHits = Integer.parseInt(inputManager.getOption("LuceneStandardMapper:NumberOfHits")); 
		} catch (Exception e) {
			logger.debug(e);
			logger.error("Could not read NumberOfHits, use default parameter 20");
			numberOfHits = 20;
		}

		logger.info("Lucene Search Query Text: " + query_term);



		Query q;
		// create the query the standard way
		//		QueryParser queryParser = new ComplexPhraseQueryParser("labels", analyzer);
		//		Query q = queryParser.parse(query_term);

		// use the MultiFieldQuery to search in fields "labels", "keywords" and "nodes"
		String[] search_fields = {KeywordSearcher.LABELS, KeywordSearcher.KEYWORDS};//, KeywordSearcher.NODE};
		MultiFieldQueryParser multiFieldQueryParser = new MultiFieldQueryParser(search_fields, analyzer); 
		Query multiFieldQuery = multiFieldQueryParser.parse(query_term);
		q = multiFieldQuery;

		// customized boolean query
		if (inputManager.isActiveOption("LuceneStandardMapper:QueryType", "boolean") ) {
			q = new BooleanQuery();
			for (String field : labels) {
				PhraseQuery phraseQuery = new PhraseQuery();
				for (String term_part : query_term.split(" ")) {
					phraseQuery.add(new Term(field, term_part));
				}
				((BooleanQuery) q).add(phraseQuery, BooleanClause.Occur.SHOULD);
			}
		} else 

			// customized DisjunctionMax query
			if (inputManager.isActiveOption("LuceneStandardMapper:QueryType", "DisjunctionMax") ) {
				q = new DisjunctionMaxQuery(0.001f);


				if (searchPerfectOnly) {
					BooleanQuery booleanQuery = new BooleanQuery();
					for (String term_part : term.split(" ")) {
						booleanQuery.add(new BooleanClause(new TermQuery(new Term("labels", term_part)), BooleanClause.Occur.MUST));
					}
					q = booleanQuery;
				} else if (searchPerfectAlso) {
					for (String term_part : term.split(" ")) {
						((DisjunctionMaxQuery) q).add(new TermQuery(new Term("labels", term_part)));
					}
					((DisjunctionMaxQuery) q).add(new TermQuery(new Term("labels", term)));
				} else if (searchPerfectNo) {
					for (String term_part : term.split(" ")) {
						((DisjunctionMaxQuery) q).add(new TermQuery(new Term("labels", term_part)));
					}
				}

			}

		logger.debug("Parsed Lucene Search Query" + q.toString());


		// -------------------------------------------------

		boolean adjustFieldNorm = inputManager.isActiveOption("LuceneStandardMapper:AdjustFieldNorm", true);
		boolean divideByOccurance = inputManager.isActiveOption("LuceneStandardMapper:DivideByOccurrence", true);
		boolean boostPerfectMatch = inputManager.isActiveOption("LuceneStandardMapper:BoostPerfectMatch", true);

		// get three times more hits than necessary in order to still have
		// enough after deleting duplicates
		// (this is not deterministic)
		TopDocs hits = searcher.search(q, 1000);
		ScoreDoc[] scoreDocs = hits.scoreDocs;
		for (int n = 0; n < scoreDocs.length; ++n) {
			ScoreDoc sd = scoreDocs[n];

			float lucene_score = sd.score;
			int numberOfOccurance = 0;
			int docId = sd.doc;
			Document d = searcher.doc(docId);
			List<IndexableField> fields = d.getFields();

			String uri = "";
			String label = "";
			String keywords = "";

			List<Float> nodeScores = new ArrayList<Float>();
			List<Float> labelScores = new ArrayList<Float>();
			List<Float> keywordScores = new ArrayList<Float>();

			Explanation explanation = searcher.explain(q, sd.doc);

			try {

				for (IndexableField f : fields) {
					if (f.name().toLowerCase().contains(KeywordSearcher.NODE)) {

						// get the text
						uri = f.stringValue();

						float new_node_score = getNewScore(original_term, f, 0.1f, adjustFieldNorm, boostPerfectMatch, searchPerfectOnly, fuzzyParam, explanation); 
						if (new_node_score > 0) numberOfOccurance++;
						nodeScores.add(new_node_score);

					} else if (f.name().toLowerCase().contains(KeywordSearcher.LABELS)) {
						// get the text
						label += f.stringValue() + " - ";

						float new_label_score = getNewScore(original_term, f, 1.5f, adjustFieldNorm, boostPerfectMatch, searchPerfectOnly, fuzzyParam, explanation); 
						if (new_label_score > 0) numberOfOccurance++;
						labelScores.add(new_label_score);

					} else if ("keywords".equalsIgnoreCase(f.name())) {
						keywords += f.stringValue() + " - ";

						float new_keywords_score = getNewScore(original_term, f, 1.0f, adjustFieldNorm, boostPerfectMatch, searchPerfectOnly, fuzzyParam, explanation); 
						if (new_keywords_score > 0) numberOfOccurance++;
						keywordScores.add(new_keywords_score);

					}

					//					if (uri.equalsIgnoreCase("<http://id.who.int/icd/entity/372746086>")) {
					//						@SuppressWarnings("unused")
					//						int i = 1 + 1;
					//					}
				}
			} catch (Exception e) {
				logger.error("Error during lucene search for " + type, e);
			}

			// normalizing the score
			// score by Lucene regards all other matching fields of the
			// document, too.
			// This behaviour is not wanted here
			//			if (!divideByOccurance) {
			//				numberOfLabelsContainingTerm = 1;
			//			}
			//			if (numberOfLabelsContainingTerm == 0) {
			//				numberOfLabelsContainingTerm = 1;
			//			}
			//			if (boostPerfectMatch) {
			//				boost = 1;
			//			}


			if (logger.isDebugEnabled()) {
				Explanation explain = searcher.explain(q, sd.doc);	
				logger.debug("Explain Lucene Score:");
				logger.debug(uri);
				logger.debug(label);
				logger.debug(explain);	
			}

			if (customFormula) {
				logger.debug("Old Score by Lucene Search Engine: " + lucene_score);

				float score = 0;
				float score1 = 0;
				float score2 = 0;
				float score3 = 0;
				for (Float node_score : nodeScores) { score1 += node_score;}
				for (Float label_score : labelScores) { score2 += label_score;}
				for (Float keyword_score : keywordScores) { score3 += keyword_score;}

				float coord = getCoord(explanation);
				score = (score1 + score2 + score3 ) * coord;

				if (divideByOccurance && (numberOfOccurance > 0)) {
					//					score = (float) (score / Math.sqrt(numberOfOccurance));
					score = (float) (score / numberOfOccurance);
					logger.debug("score = (node_score(" + score1 + ") + label_score(" + score2 + ") + keyword_score(" + score3 + ") ) * coord(" + coord + ") / SquRoot(" + numberOfOccurance + ")");
				} else {
					logger.debug("score = (node_score(" + score1 + ") + label_score(" + score2 + ") + keyword_score(" + score3 + ") ) * coord(" + coord + ") ");
				}

				logger.debug("New Score " + score + " = " + score); 
				candidates.add(createCandidate(uri, score, label, keywords, type));

			} else {
				candidates.add(createCandidate(uri, lucene_score, label, keywords, type));
			}
		}

		// only regard the entity with the highest score
		candidates.sort(new CustomComparator());
		candidates = removeDuplicates(candidates);

		// normalize the scores into interval [0, 1]
		// remark: not used anymore, original score applys better
		// normalizeScores(candidates);


		// not necessary since 4.0.0
		// searcher.close();
		return candidates;
	}

	private float getNewScore(String original_term, IndexableField f,
			float fieldFactor, boolean adjustFieldNorm, boolean boostPerfectMatch, boolean searchPerfectOnly,
			String fuzzyParam, Explanation explanation) {

		float ownNorm = Float.MAX_VALUE;
		float maxNorm = 0;
		int boost = 1;

		boolean fieldContainsTerm = false;
		boolean fieldContainsPartialTerm = false;
		String[] query_words = original_term.split(" ");
		String[] field_words = f.stringValue().toLowerCase().split(" ");

		if (searchPerfectOnly) {
			// normalize the quantity of found fields 
			// in the end score
			if (f.stringValue().toLowerCase().contains(original_term) ) {
				fieldContainsTerm = true;
			}

		} else {

			for (int i = 0; i < query_words.length; i++) {
				for (int j = 0; j < field_words.length; j++) {
					// normalize the quantity of found fields 
					// in the end score

					int levenshtein = StringUtils.getLevenshteinDistance(field_words[j], query_words[i]);
					double max_absolute_distance = Double.parseDouble(fuzzyParam);
					if (levenshtein <=  max_absolute_distance ) {
						fieldContainsTerm = true;
					}

					// boost in case of a partial match
					if (field_words[j].equalsIgnoreCase(query_words[i])) {
//						fieldContainsPartialTerm = true;
						boost += 1;
					}

					// boost in case of a partial match
//					if (fieldContainsPartialTerm) {
//						boost += 1;
//					}
				}
			}

		}
		//--------


		// boost in case of a perfect match
		if (f.stringValue().equalsIgnoreCase(original_term)) {
			boost += 2;
		}
		if (!boostPerfectMatch) boost = 1;
		if (!fieldContainsTerm) return 0.0f;

		// get the score
		float field_score = getScore(f.name(), explanation);

		// compute own norm
		int length = f.stringValue().split(" ").length;
		ownNorm = (float) (1.0d / Math.sqrt(length));

		// get the fieldNorm
		float fieldNorm = 1;
		if (adjustFieldNorm) {
			fieldNorm = getFieldNorm(f.name(), explanation);
			if (ownNorm == Float.MAX_VALUE) ownNorm = maxNorm;
		} else {
			fieldNorm = 1;
			ownNorm = 1;
		}

		// //field_score * boost /  / fieldNorm * own_field_norm;
		return field_score * boost / fieldNorm * ownNorm * fieldFactor;
	}

	private float getScore(String field, Explanation explain) {
		float score = 0;
		Explanation[] fieldExplanations = explain.getDetails();
		for (Explanation explanation: fieldExplanations) {
			String fieldExplain = explanation.toString();
			if (fieldExplain.contains(field))
				for (String line : fieldExplain.split("\n")) {
					if (line.matches(".*MATCH.*weight.*")){
						score += Float.parseFloat(line.split(" =")[0]);
					}
				}

		}
		return score;
	}

	private float getCoord(Explanation explain) {
		Explanation[] fieldExplanations = explain.getDetails();
		for (Explanation explanation: fieldExplanations) {
			String fieldExplain = explanation.toString();
			for (String line : fieldExplain.split("\n")) {
				if (line.matches(".*coord.*")){
					return Float.parseFloat(line.split(" =")[0]);
				}
			}

		}
		return 1;
	}

	private float getFieldNorm(String field, Explanation explain) {
		Explanation[] fieldExplanations = explain.getDetails();
		for (Explanation explanation: fieldExplanations) {
			String fieldExplain = explanation.toString();
			if (fieldExplain.contains(field))
				for (String line : fieldExplain.split("\n")) {
					if (line.matches(".*fieldNorm.*")){
						return Float.parseFloat(line.split(" =")[0]);
					}
				}

		}
		return 0;
	}

	private String[] getLabelsArray(int maxLabels) {
		String[] labels = new String[maxLabels];

		for (int i = 1; i <= labels.length; i++) {
			labels[i - 1] = KeywordSearcher.LABELS;// + i;
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
		if (!label.isEmpty() && !keywords.isEmpty()) {
			label = label + keywords;
		} else if (!label.isEmpty() ) {
			//
		} else if (!keywords.isEmpty()) {
			label = keywords;
		}
		switch (type) {
		case CANDIDATE:
			return new GeneralCandidate(uri, score, label);
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