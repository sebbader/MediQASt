package inputmanagement.impl;

import inputmanagement.candidates.Candidate;

import java.util.Comparator;

public class CustomComparator implements Comparator<Candidate> {

	@Override
	public int compare(Candidate r1, Candidate r2) {
		if (r1.getScore() < r2.getScore()) {
			return 1;
		} else if (r1.getScore() > r2.getScore()) {
			return -1;
		}
		return 0;
	}

}
