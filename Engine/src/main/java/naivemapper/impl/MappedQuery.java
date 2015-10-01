package naivemapper.impl;

import inputmanagement.candidates.RdfCandidate;
import inputmanagement.candidates.RdfCandidateTypes;

import java.util.ArrayList;
import java.util.List;

import naivemapper.impl.InsertResult;

public class MappedQuery implements Cloneable {
	private List<RdfCandidate> query;

	public MappedQuery(String text) {
		String[] words = text.split(" ");
		query = new ArrayList<RdfCandidate>();
		for (int i = 0; i < words.length; i++) {
			query.add(new Word(words[i], i));
		}
	}

	public MappedQuery(List<RdfCandidate> query) {
		this.query = query;
	}

	public InsertResult insertCandidate(RdfCandidate candidate) {
		RdfCandidate candidateThatBlocked = null;
		List<RdfCandidate> tmp_query = new ArrayList<RdfCandidate>(query);
		List<Integer> positions = candidate.getPosition();

		List<RdfCandidate> wordsToReplace = new ArrayList<RdfCandidate>();
		for (Integer pos : positions) {
			for (RdfCandidate word : tmp_query) {
				if (word.getPosition().contains(pos))
					wordsToReplace.add(word);
			}
		}

		try {
			for (RdfCandidate word : wordsToReplace) {
				candidateThatBlocked = word;
				if (!word.getType().equals(RdfCandidateTypes.UNKNOWN))
					throw new NullPointerException();
				if (!tmp_query.remove(word))
					throw new NullPointerException();
			}
		} catch (NullPointerException e) {
			// the necessary space is already blocked;
			return new InsertResult(false, candidateThatBlocked);
		}

		// insert the new candidate
		tmp_query.add(candidate);
		tmp_query.sort(new WordSorter());

		query = tmp_query;
		return new InsertResult(true, null);
	}

	public List<RdfCandidate> getQuery() {
		return this.query;
	}

	@Override
	public MappedQuery clone() {
		MappedQuery clonedMappedQuery = new MappedQuery(
				new ArrayList<RdfCandidate>(this.query));
		return clonedMappedQuery;
	}

	@Override
	public String toString() {
		return query.toString();
	}
}

class Word implements RdfCandidate {
	private String term;
	private List<Integer> position;
	private double score;

	public Word(String term, int position) {
		this.setTerm(term);

		List<Integer> pos = new ArrayList<Integer>();
		pos.add(position);
		this.setPosition(pos);
		this.setScore(0);
	}

	public List<Integer> getPosition() {
		return position;
	}

	@Override
	public void setPosition(List<Integer> position) {
		this.position = position;
	}

	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	@Override
	public double getScore() {
		return this.score;
	}

	@Override
	public void setScore(double score) {
		this.score = score;
	}

	@Override
	public String getText() {
		return term;
	}

	@Override
	public RdfCandidateTypes getType() {
		return RdfCandidateTypes.UNKNOWN;
	}

	@Override
	public String toString() {
		return this.term;
	}
}
