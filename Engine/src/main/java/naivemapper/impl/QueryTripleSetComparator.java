package naivemapper.impl;

import inputmanagement.impl.QueryTriple;

import java.util.ArrayList;
import java.util.Comparator;

public class QueryTripleSetComparator implements
		Comparator<ArrayList<QueryTriple>> {

	@Override
	public int compare(ArrayList<QueryTriple> queryTripleSet1,
			ArrayList<QueryTriple> queryTripleSet2) {
		double score1 = 0;
		for (QueryTriple triple : queryTripleSet1) {
			score1 += triple.getScore();
		}

		double score2 = 0;
		for (QueryTriple triple : queryTripleSet2) {
			score2 += triple.getScore();
		}

		if (score1 > score2) {
			return -1;
		} else {
			return 1;
		}
	}

}
