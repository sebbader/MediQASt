package sparqlgenerator.impl;

import inputmanagement.candidates.impl.SparqlCandidate;

import java.util.ArrayList;
import java.util.Iterator;

public class SparqlList extends ArrayList<SparqlCandidate> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	public void addDistinct(SparqlCandidate candidate) {

		Iterator<SparqlCandidate> iter = super.iterator();
		while (iter.hasNext()) {
			SparqlCandidate can = iter.next();
			if (can.getText().equalsIgnoreCase(candidate.getText())) {
				if (candidate.getScore() > can.getScore()) {
					can.setScore(candidate.getScore());
				}
				return;
			}
		}

		super.add(candidate);
	}

	@Override
	public boolean contains(Object object) {
		SparqlCandidate candidate = (SparqlCandidate) object;
		Iterator<SparqlCandidate> iter = super.iterator();
		while (iter.hasNext()) {
			SparqlCandidate can = iter.next();
//			String canText = can.getText();
//			String candidateText = candidate.getText();
			if (can.getText().equalsIgnoreCase(candidate.getText())) {
				return true;
			}
		}
		return false;
	}
}