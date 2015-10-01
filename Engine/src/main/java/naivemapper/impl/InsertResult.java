package naivemapper.impl;

import inputmanagement.candidates.RdfCandidate;

public class InsertResult {
	private RdfCandidate blockingCandidate;
	private boolean insert_worked;

	public InsertResult(boolean insert_worked, RdfCandidate blockingCandidate) {
		this.insert_worked(insert_worked);
		this.setBlockingCandidate(blockingCandidate);
	}

	public RdfCandidate getBlockingCandidate() {
		return blockingCandidate;
	}

	public void setBlockingCandidate(RdfCandidate blockingCandidate) {
		this.blockingCandidate = blockingCandidate;
	}

	public boolean didInsert_worked() {
		return insert_worked;
	}

	public void insert_worked(boolean insert_worked) {
		this.insert_worked = insert_worked;
	}

}
