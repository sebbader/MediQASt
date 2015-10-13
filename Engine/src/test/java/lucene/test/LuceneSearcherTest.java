package lucene.test;

import inputmanagement.candidates.impl.EntityCandidate;
import inputmanagement.candidates.impl.RelationCandidate;
import inputmanagement.impl.InputManagerImpl;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import lucene.LuceneSearcher;

import org.apache.log4j.Level;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryparser.classic.ParseException;
import org.junit.Before;
import org.junit.Test;

import configuration.ConfigManager;
import configuration.impl.ConfigManagerImpl;

public class LuceneSearcherTest {

	HashMap<String, String> param = new HashMap<String, String>();
	
//	private String text = "cancer";
//	 private String text = "athlete's foot";
//	 private String text = "lung cancer";
//	 private String text = "side effect"; 
	 private String text = "BSYNRYMUTXBXSQ-UHFFFAOYSA-N"; 	


	@Before
	public void prepare() {
		ConfigManagerImpl.setLogLevel(Level.INFO);
		
		param.put("resourceMapper", "luceneStandard");
		param.put("findEntityAndClass", "true");
		param.put("RelationManagerSimilarity", "Levenshtein");
		
//		param.put("LuceneStandardMapper:QueryType", "standard");
//		param.put("LuceneStandardMapper:QueryType", "boolean");
//		param.put("LuceneStandardMapper:QueryType", "DisjunctionMax");
		
		param.put("LuceneStandardMapper:AdjustFieldNorm", "true");
//		param.put("LuceneStandardMapper:AdjustFieldNorm", "false");
		
//		param.put("LuceneStandardMapper:BoostPerfectMatch", "true");
		param.put("LuceneStandardMapper:BoostPerfectMatch", "false");
		
//		param.put("LuceneStandardMapper:Lemmatize", "true");
		param.put("LuceneStandardMapper:Lemmatize", "false");
		
//		param.put("LuceneStandardMapper:StopwordRemoval", "true");
		param.put("LuceneStandardMapper:StopwordRemoval", "false");
		
		param.put("LuceneStandardMapper:SearchPerfect", "only");
//		param.put("LuceneStandardMapper:SearchPerfect", "also");
//		param.put("LuceneStandardMapper:SearchPerfect", "no");
		
		param.put("LuceneStandardMapper:DivideByOccurrence", "true");
//		param.put("LuceneStandardMapper:DivideByOccurrence", "false");
		
//		param.put("considerRelationEnvironment", false);
		
		param.put("LuceneStandardMapper:FuzzySearch", "true");
//		param.put("LuceneStandardMapper:FuzzySearch", "false");
		
		param.put("LuceneStandardMapper:FuzzyParam", "1");
	}

	@Test
	public void searchForEntityTest() throws CorruptIndexException,
			IOException, ParseException {
		// String arg = "athlete 's foot";
		// String arg = "amnesia";
		// String arg = "D002056";
		// String arg = "acid";
		// String arg = "to have";
		// String arg = "tuberculosis";
		// String arg = "Hirschfeld";
		// String arg = "acetylsalicylic acid";
		String arg = text;
		

		LuceneSearcher searcher = new LuceneSearcher();


		searcher.setNumberOfHits(20);
		List<EntityCandidate> result = searcher.searchEntity(arg,
				new InputManagerImpl("", "", param));

		System.out.println("Entities similar to " + arg);
		while (!result.isEmpty()) {
			EntityCandidate entity = (EntityCandidate) result.remove(0);
			System.out.println("Entity: " + entity.toString());
		}
		System.out.println("------");
	}

	@Test
	public void searchForClassTest() throws CorruptIndexException, IOException,
			ParseException {
		// String arg = "athlete 's foot";
		// String arg = "academic journal";
		// String arg = "acid";
		// String arg = "D002056";
		String arg = text;

		LuceneSearcher searcher = new LuceneSearcher();


		searcher.setNumberOfHits(20);
		List<EntityCandidate> result = searcher.searchClass(arg,
				new InputManagerImpl("", "", param));

		System.out.println("Classes similar to " + arg);
		while (!result.isEmpty()) {
			EntityCandidate classCandidate = (EntityCandidate) result.remove(0);
			System.out.println("Class: " + classCandidate.toString());
		}
		System.out.println("------");
	}

	@Test
	public void searchForRelationTest() throws CorruptIndexException,
			IOException, ParseException {
		// String rel = " sameAs";
		// String rel = "acetylsalicylic acid";
		// String rel = "lung cancer";
		String rel = text;

		LuceneSearcher searcher = new LuceneSearcher();
	

		List<RelationCandidate> result = searcher.searchRelation(rel,
				new InputManagerImpl("", "", param));

		System.out.println("Relations similar to " + rel);
		while (!result.isEmpty()) {
			RelationCandidate relation = (RelationCandidate) result.remove(0);
			System.out.println("Relation: " + relation.toString());
		}
		System.out.println("------");
	}

	/*
	 * @Test public void searchForLemonEntry() throws CorruptIndexException,
	 * IOException, ParseException { String entry = "agent";
	 * 
	 * LuceneSearcher searcher = new LuceneSearcher(); List<Candidate> result =
	 * searcher.searchLemonEntry(entry);
	 * 
	 * 
	 * System.out.println("Lemon Entries similar to " + entry); while
	 * (!result.isEmpty()) { Candidate candidate = result.remove(0); String
	 * label = ""; if (candidate instanceof EntityCandidate) label =
	 * ((EntityCandidate) candidate).getLabel(); if (candidate instanceof
	 * RelationCandidate) label = ((RelationCandidate) candidate).getLabel(); if
	 * (candidate instanceof GeneralCandidate) label = ((GeneralCandidate)
	 * candidate).getLabel();
	 * 
	 * System.out.println("Uri: " + candidate.getText() + " Label: " + label +
	 * " Score: " + candidate.getScore()); } System.out.println("------"); }
	 */
}
