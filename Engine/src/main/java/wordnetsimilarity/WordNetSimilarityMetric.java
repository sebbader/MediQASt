package wordnetsimilarity;

import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.impl.HirstStOnge;
import edu.cmu.lti.ws4j.impl.JiangConrath;
import edu.cmu.lti.ws4j.impl.LeacockChodorow;
import edu.cmu.lti.ws4j.impl.Lesk;
import edu.cmu.lti.ws4j.impl.Lin;
import edu.cmu.lti.ws4j.impl.Path;
import edu.cmu.lti.ws4j.impl.Resnik;
import edu.cmu.lti.ws4j.impl.WuPalmer;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;

public class WordNetSimilarityMetric {

	private static ILexicalDatabase db = new NictWordNet();
	private static RelatednessCalculator[] rcs = { new HirstStOnge(db),
			new LeacockChodorow(db), new Lesk(db), new WuPalmer(db),
			new Resnik(db), new JiangConrath(db), new Lin(db), new Path(db) };

	public double getSimilarity(String phrase1, String phrase2) {
		WS4JConfiguration.getInstance().setMFS(true);
		for (RelatednessCalculator rc : rcs) {
			double s = rc.calcRelatednessOfWords(phrase1, phrase2);
			System.out.println(rc.getClass().getName() + "\t" + s);
		}
		return 0;
	}

	public double getPathSimilarity(String phrase1, String phrase2) {
		RelatednessCalculator rc = new Path(db);
		return rc.calcRelatednessOfWords(phrase1, phrase2);
	}
}
