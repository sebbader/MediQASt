package mapper.impl;

import java.util.ArrayList;
import java.util.List;

import mapper.LemonMapper;
import inputmanagement.candidates.Candidate;
import inputmanagement.candidates.impl.EntityCandidate;
import inputmanagement.candidates.impl.RelationCandidate;

public class LemonMapperImpl implements LemonMapper {

	private List<Candidate> candidates = new ArrayList<Candidate>();
	private List<Candidate> entities = new ArrayList<Candidate>();
	private List<Candidate> relations = new ArrayList<Candidate>();

	private String[] queryArray;

	public LemonMapperImpl(String query) {
		// String TODO
	}

	public List<RelationCandidate> getRelationCandidates() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<EntityCandidate> getEntityCandidates() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Candidate> getCandidates() {
		// TODO Auto-generated method stub
		return null;
	}

	public void getCandidates(List<EntityCandidate> entities,
			List<RelationCandidate> relations) {
		// TODO Auto-generated method stub

	}

}
