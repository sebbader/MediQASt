package inputmanagement.impl;

import inputmanagement.candidates.Candidate;

import java.util.Comparator;

public class CustomComparator implements Comparator<Candidate> {

	@Override
	public int compare(Candidate r1, Candidate r2) {
		double r1Score = r1.getScore();
		double r2Score = r2.getScore();
//		if (r1Score < r2Score) {
//			return 1;
//		} else if (r1Score > r2Score) {
//			return -1;
//		}
//		return 0;
		
		return - Double.compare(r1Score, r2Score);
	}

}
