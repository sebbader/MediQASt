package inputmanagement.impl;

import java.util.Comparator;

public class QueryTripleComparator implements Comparator<QueryTriple> {

	@Override
	public int compare(QueryTriple triple1, QueryTriple triple2) {
		if (triple1.getScore() > triple2.getScore()) {
			return -1;
		} else {
			return 1;
		}
	}

}
