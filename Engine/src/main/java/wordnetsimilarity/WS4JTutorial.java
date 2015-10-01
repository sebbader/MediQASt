package wordnetsimilarity;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import edu.cmu.lti.jawjaw.pobj.POS;
import edu.cmu.lti.jawjaw.pobj.Synset;
import edu.cmu.lti.jawjaw.util.WordNetUtil;
import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.lexical_db.data.Concept;
import edu.cmu.lti.ws4j.Relatedness;
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

public class WS4JTutorial {

	private static ILexicalDatabase db = new NictWordNet();
	private static RelatednessCalculator[] rcs = { new HirstStOnge(db),
			new LeacockChodorow(db), new Lesk(db), new WuPalmer(db),
			new Resnik(db), new JiangConrath(db), new Lin(db), new Path(db) };

	private static void run(String word1, String word2) {
		WS4JConfiguration.getInstance().setMFS(true);
		for (RelatednessCalculator rc : rcs) {
			double s = rc.calcRelatednessOfWords(word1, word2);
			System.out.println(rc.getClass().getName() + "\t" + s);
		}
	}

	public static void start() {
		long t0 = System.currentTimeMillis();
		run("play", "act");
		long t1 = System.currentTimeMillis();
		System.out.println("Done in " + (t1 - t0) + " msec.");
	}
}
