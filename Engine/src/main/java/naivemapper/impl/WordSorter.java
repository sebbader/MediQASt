package naivemapper.impl;

import inputmanagement.candidates.RdfCandidate;

import java.util.Comparator;

public class WordSorter implements Comparator<RdfCandidate> {

	@Override
	public int compare(RdfCandidate candidate1, RdfCandidate candidate2) {
		int candidate1_position = getMinPosition(candidate1);
		int candidate2_position = getMinPosition(candidate2);

		if (candidate1_position < candidate2_position) {
			return -1;
		} else {
			return 1;
		}

	}

	private int getMinPosition(RdfCandidate candidate) {
		if (candidate.getPosition().size() > 0) {
			return candidate.getPosition().get(0);
		}

		return 0;
	}

}
