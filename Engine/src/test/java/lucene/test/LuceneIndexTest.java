package lucene.test;

import inputmanagement.impl.InputManagerImpl;

import java.io.IOException;

import lucene.LuceneIndexer;

import org.junit.Before;
import org.junit.Test;
import org.semanticweb.yars.nx.parser.ParseException;

public class LuceneIndexTest {

	@Before
	public void prepare() throws IOException {
		@SuppressWarnings("unused")
		InputManagerImpl manager = new InputManagerImpl("http://localhost:8890/sparql", "", null);
	}

	@Test
	public void test() throws IOException, ParseException {

		LuceneIndexer indexer = new LuceneIndexer();
//		 indexer.indexEntities();
//		 indexer.indexClasses();
//		 indexer.indexRelations();
		// indexer.indexLemonDict();
	}

}
