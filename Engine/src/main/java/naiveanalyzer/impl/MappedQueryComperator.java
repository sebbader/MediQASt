package naiveanalyzer.impl;

import java.util.Comparator;

import naivemapper.impl.MappedQuery;

public class MappedQueryComperator implements Comparator<MappedQuery> {

	@Override
	public int compare(MappedQuery mq1, MappedQuery mq2) {
		double mq1Score = mq1.getScore();
		double mq2Score = mq2.getScore();
		
		if (mq1Score < mq2Score) {
			return 1;
		} else if (mq1Score > mq2Score) {
			return -1;
		}
		return 0;
	}

}
