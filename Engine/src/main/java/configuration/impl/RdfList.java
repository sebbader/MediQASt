package configuration.impl;

import inputmanagement.candidates.RdfCandidate;

import java.util.ArrayList;
import java.util.Iterator;

import org.netlib.util.booleanW;

public class RdfList extends ArrayList<RdfCandidate> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public void addDistinct(RdfCandidate candidate) {
		
		Iterator<RdfCandidate> iter = super.iterator();
		while (iter.hasNext()) {
			RdfCandidate can = iter.next();
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
		RdfCandidate candidate = (RdfCandidate) object;
		Iterator<RdfCandidate> iter = super.iterator();
		while (iter.hasNext()) {
			RdfCandidate can = iter.next();
			String canText = can.getText();
			String candidateText = candidate.getText();
			if (can.getText().equalsIgnoreCase(candidate.getText())) {
				return true;
			}
		}
		return false;
	}
}
