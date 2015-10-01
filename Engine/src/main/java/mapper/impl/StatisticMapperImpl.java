package mapper.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import inputmanagement.candidates.Candidate;
import inputmanagement.impl.QueryTriple;

public class StatisticMapperImpl {

	private int considered_words;

	public StatisticMapperImpl(HashMap<String, Object> parameter) {
		if (parameter.containsKey("StasticMapper_considered_words")) {
			considered_words = (int) parameter
					.get("StasticMapper_considered_words");
		} else {
			// default
			considered_words = 3;
		}
	}

	public List<QueryTriple> findCandidates(String query) {
		List<QueryTriple> queryTriples = new ArrayList<QueryTriple>();

		String window;
		int counter = 0;
		while (true) {
			// window = query.split("A");
			break;
		}

		return queryTriples;
	}
}
