package postagger;

import java.util.ArrayList;

/**
 * Uses stanford POS-Tagger to identify entities using - Stanford MaxentTagger -
 * English Chain Rules
 * 
 * @author Sebastian Bader (sebastian.bader@student.kit.edu) implementing class
 *         'PosTaggerBasedMentionDetector' by Swetlana Stickhof
 */
public interface PosTagger {

	/**
	 * 
	 * @param query
	 * @return
	 */
	public ArrayList<String> getEntities(String query);

}
