package lucene;

import java.io.IOException;

import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.search.CollectionStatistics;
import org.apache.lucene.search.TermStatistics;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.search.similarities.TFIDFSimilarity;
import org.apache.lucene.util.BytesRef;

public class CustomSimilarity extends DefaultSimilarity {

	@Override
	public float lengthNorm(FieldInvertState arg0) {

		// return super.lengthNorm(arg0);

		float length = super.lengthNorm(arg0);
		return length;
	}

	@Override
	public float tf(float freq) {

		return super.tf(freq);

		// float super_freq = super.tf(freq);
		//
		// if (freq > 1){
		// System.out.println("############################");
		// return 10f;
		// } else if (freq > 0) {
		// return 1f;
		// } else {
		// return 0f;
		// }
	}

	// @Override
	// public float idf(long docFreq, long numDocs) {
	// float super_idf = super.idf(docFreq, numDocs);
	// return super_idf;
	// }

}
