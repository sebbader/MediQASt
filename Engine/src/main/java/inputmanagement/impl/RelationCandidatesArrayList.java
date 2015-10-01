package inputmanagement.impl;

import inputmanagement.candidates.impl.RelationCandidate;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * Extending ArrayList in order have "addDistinctCandidate" method available.
 * 
 * @author Sebastian Bader (sebastian.bader@student.kit.edu)
 *
 */
@SuppressWarnings({ "serial" })
public class RelationCandidatesArrayList extends ArrayList<RelationCandidate>
		implements List<RelationCandidate> {

	/**
	 * adds a new Candidate object to the list if and only if there is no
	 * Candidate object with the same uri yet.
	 * 
	 * @param newCandidate
	 */
	public void addDistinctCandidate(RelationCandidate newCandidate) {

		boolean candidate_exists = false;

		ListIterator<RelationCandidate> iter = super.listIterator();
		if (!iter.hasNext()) {
			super.add(newCandidate);
			return;
		}
		while (iter.hasNext()) {
			RelationCandidate oldCandidate = iter.next();
			if (oldCandidate.getText().equalsIgnoreCase(
					(newCandidate).getText())) {
				// this candidate already exists. therefore do not add it again
				// TODO increase its score?
				candidate_exists = true;
				double updatedScore = Math.max(oldCandidate.getScore(),
						newCandidate.getScore());
				oldCandidate.setScore(updatedScore);
			}
		}

		if (!candidate_exists)
			super.add(newCandidate);
	}

}
