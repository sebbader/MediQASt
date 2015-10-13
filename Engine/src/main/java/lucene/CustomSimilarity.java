package lucene;

import java.io.IOException;

import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.CollectionStatistics;
import org.apache.lucene.search.TermStatistics;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.search.similarities.TFIDFSimilarity;
import org.apache.lucene.util.BytesRef;

public class CustomSimilarity extends DefaultSimilarity {

	@Override
	public float coord(int overlap, int maxOverlap) {
		// TODO Auto-generated method stub
		return super.coord(overlap, maxOverlap);
	}


	@Override
	public float idf(long docFreq, long numDocs) {
		return super.idf(docFreq, numDocs);
	/*	long max_docFreq = 0L;
		for (String field: labels) {
			try {
				long docfreq_new = reader.totalTermFreq(new Term(field, term));
				if (docfreq_new > max_docFreq) max_docFreq = docfreq_new;
			} catch (IOException e) {
				return super.idf(docFreq, numDocs);
			}
		}
		return super.idf(max_docFreq, numDocs);
	*/
	}

	@Override
	public float lengthNorm(FieldInvertState arg0) {
		// TODO Auto-generated method stub
		return super.lengthNorm(arg0);
	}

	@Override
	public float queryNorm(float sumOfSquaredWeights) {
		// TODO Auto-generated method stub
		return super.queryNorm(sumOfSquaredWeights);
	}

	@Override
	public float scorePayload(int doc, int start, int end, BytesRef payload) {
		// TODO Auto-generated method stub
		return super.scorePayload(doc, start, end, payload);
	}

	@Override
	public float sloppyFreq(int distance) {
		// TODO Auto-generated method stub
		return super.sloppyFreq(distance);
	}

	@Override
	public float tf(float freq) {
		
		if (freq > 0) {
			return 1;
		} else {
			return 0;
		}
//		return super.tf(freq);
	}



}
