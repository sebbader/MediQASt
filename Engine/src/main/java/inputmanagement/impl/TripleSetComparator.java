package inputmanagement.impl;

import java.util.ArrayList;
import java.util.Comparator;

import configuration.impl.CommonMethods;

public class TripleSetComparator implements
		Comparator<ArrayList<QueryTriple>> {

	@Override
	public int compare(ArrayList<QueryTriple> triple1, ArrayList<QueryTriple> triple2) {
		double score1 = CommonMethods.getTriplesScore(triple1);
		double score2 = CommonMethods.getTriplesScore(triple2);
		
		return - Double.compare(score1, score2);
	}

}
