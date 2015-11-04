package inputmanagement.impl;

import inputmanagement.candidates.Candidate;
import inputmanagement.candidates.impl.EntityCandidate;
import inputmanagement.candidates.impl.RelationCandidate;

import java.util.ArrayList;
import java.util.List;

import configuration.impl.CommonMethods;
import uk.ac.shef.wit.simmetrics.similaritymetrics.CandidatesTest;
import lucene.LuceneSearcher;

/**
 * Container class to transport information from the question analyzing step to
 * resource mapping step
 * 
 * @author Sebastian Bader (sebastian.bader@student.kit.edu)
 *
 */
public class QueryTriple {

	private String entity1;
	private String predicate;
	private String entity2;

	private EntityCandidate entity1Candidate;
	private RelationCandidate predicateCandidate;
	private EntityCandidate entity2Candidate;

	private double score;
	private double entity1_score;
	private double relation_score;
	private double entity2_score;

	private List<EntityCandidate> entity1Candidates;
	private List<RelationCandidate> relationCandidates;
	private List<EntityCandidate> entity2Candidates;

	/**
	 * creates a new query triple object
	 * 
	 * @param entity1
	 *            first found entity, can be a subject/object, a question phrase
	 *            or a variable
	 * @param predicate
	 *            predicate between these entities
	 * @param entity2
	 *            second found entity, can be a subject/object, a question
	 *            phrase or a variable
	 */
	public QueryTriple(String entity1, String predicate, String entity2) {
		this.entity1 = replace(entity1);
		this.predicate = replace(predicate);
		this.entity2 = replace(entity2);
	}

	public QueryTriple(EntityCandidate entity1, RelationCandidate predicate,
			EntityCandidate entity2) {
		this.setEntity1Candidate(entity1);
		this.setPredicateCandidate(predicate);
		this.setEntity2Candidate(entity2);
	}

	public String getEntity1() {
		return entity1;
	}

	public void setEntity1(String entity1) {
		this.entity1 = entity1;
	}

	public String getPredicate() {
		return predicate;
	}

	public void setPredicate(String predicate) {
		this.predicate = predicate;
	}

	public String getEntity2() {
		return entity2;
	}

	public void setEntity2(String entity2) {
		this.entity2 = entity2;
	}

	public EntityCandidate getEntity1Candidate() {
		return entity1Candidate;
	}

	public void setEntity1Candidate(EntityCandidate entity1Candidate) {
		this.entity1Candidate = entity1Candidate;
	}

	public RelationCandidate getPredicateCandidate() {
		return predicateCandidate;
	}

	public void setPredicateCandidate(RelationCandidate predicateCandidate) {
		this.predicateCandidate = predicateCandidate;
	}

	public EntityCandidate getEntity2Candidate() {
		return entity2Candidate;
	}

	public void setEntity2Candidate(EntityCandidate entity2Candidate) {
		this.entity2Candidate = entity2Candidate;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public String getTriple() {
		return "<" + entity1 + ", " + predicate + ", " + entity2 + ">";
	}

	public String getTripleWithCandidates() {
		if (entity1Candidate != null && predicateCandidate != null
				&& entity2Candidate != null) {
			return "<" + entity1Candidate.getUri() + ", "
					+ predicateCandidate.getUri() + ", "
					+ entity2Candidate.getUri() + ">";
		}

		if (entity1Candidates != null && relationCandidates != null
				&& entity2Candidates != null) {
			String result = "{";

			if (entity1Candidate != null)
				result += entity1Candidate + " | ";
			for (EntityCandidate candidate : entity1Candidates) {
				result += candidate + " | ";
			}
			result = result.substring(0, result.length() - 3) + "]   [";

			if (predicateCandidate != null)
				result += predicateCandidate + " | ";
			for (RelationCandidate candidate : relationCandidates) {
				result += candidate + " | ";
			}
			result = result.substring(0, result.length() - 3) + "]   [";

			if (entity2Candidate != null)
				result += entity2Candidate + " | ";
			for (EntityCandidate candidate : entity2Candidates) {
				result += candidate + " | ";
			}
			result = result.substring(0, result.length() - 3) + "]";

			return result;
		}

		return getTriple();
	}

	private String replace(String str) {
		return str.replace("[", "").replace("]", "").replace(", ", "_");
	}

	@Override
	public String toString() {
		if (entity1 == null && predicate == null && entity2 == null) {
			return getTripleWithCandidates();
		} else {
			return getTriple();
		}
	}

	public List<EntityCandidate> getEntity1Candidates() {
		return entity1Candidates;
	}

	public void setEntity1Candidates(List<EntityCandidate> entity1Candidates) {
		this.entity1Candidates = entity1Candidates;
		this.entity1Candidates.sort(new CustomComparator());

		if (!this.entity1Candidates.isEmpty()) {
			this.entity1_score = this.entity1Candidates.get(0).getScore();
			this.score = this.entity1_score + this.relation_score + this.entity2_score;
		}
	}

	@SuppressWarnings("unchecked")
	public void addEntity1Candidates(List<EntityCandidate> entity1Candidates) {
		if (this.entity1Candidates == null) {
			this.entity1Candidates = entity1Candidates;
		} else {
			this.entity1Candidates.addAll(entity1Candidates);
		}
		this.entity1Candidates = (List<EntityCandidate>) CommonMethods
				.removeDuplicates(this.entity1Candidates);
		this.entity1Candidates.sort(new CustomComparator());

		if (!this.entity1Candidates.isEmpty()) {
			this.entity1_score = this.entity1Candidates.get(0).getScore();
			this.score = this.entity1_score + this.relation_score + this.entity2_score;
		}
	}

	public List<RelationCandidate> getRelationCandidates() {
		return relationCandidates;
	}

	public void setRelationCandidates(List<RelationCandidate> relationCandidates) {
		this.relationCandidates = relationCandidates;
		
		if (!this.relationCandidates.isEmpty()) {
			this.relation_score = this.entity1Candidates.get(0).getScore();
			this.score = this.entity1_score + this.relation_score + this.entity2_score;
		}
	}

	@SuppressWarnings("unchecked")
	public void addRelationCandidates(List<RelationCandidate> relationCandidates) {
		if (this.relationCandidates == null) {
			this.relationCandidates = relationCandidates;
		} else {
			this.relationCandidates.addAll(relationCandidates);
		}
		this.relationCandidates = (List<RelationCandidate>) CommonMethods
				.removeDuplicates(this.relationCandidates);
		this.relationCandidates.sort(new CustomComparator());

		if (!this.relationCandidates.isEmpty()) {
			this.relation_score = this.relationCandidates.get(0).getScore();
			this.score = this.entity1_score + this.relation_score + this.entity2_score;
		}
	}

	public List<EntityCandidate> getEntity2Candidates() {
		return entity2Candidates;
	}

	@SuppressWarnings("unchecked")
	public void addEntity2Candidates(List<EntityCandidate> entity2Candidates) {
		if (this.entity2Candidates == null) {
			this.entity2Candidates = entity2Candidates;
		} else {
			this.entity2Candidates.addAll(entity2Candidates);
		}
		this.entity2Candidates = (List<EntityCandidate>) CommonMethods
				.removeDuplicates(this.entity2Candidates);
		this.entity2Candidates.sort(new CustomComparator());

		if (!this.entity2Candidates.isEmpty()) {
			this.entity2_score = this.entity2Candidates.get(0).getScore();
			this.score = this.entity1_score + this.relation_score + this.entity2_score;
		}
	}

	public void setEntity2Candidates(List<EntityCandidate> entity2Candidates) {
		this.entity2Candidates = entity2Candidates;
		this.entity2Candidates.sort(new CustomComparator());

		if (!this.entity2Candidates.isEmpty()) {
			this.entity2_score = this.entity2Candidates.get(0).getScore();
			this.score = this.entity1_score + this.relation_score + this.entity2_score;
		}
	}

	public void clean() {
		// entity1 = entity1.replaceAll("VARIABLE\\-[0-9]+?", "?X");
		// predicate = predicate.replace("VARIABLE[\\-[0-9]*]?", "?X");
		// entity2 = entity2.replace("VARIABLE[\\-[0-9]]?", "?X");

		if (entity1.contains("VARIABLE")) {
			entity1 = "?variable";
		}

		if (predicate.contains("VARIABLE")) {
			predicate = "?variable";
		}

		if (entity2.contains("VARIABLE")) {
			entity2 = "?variable";
		}
	}

	public void replaceWhxx() {
		String e1 = this.entity1.toLowerCase();
		String r = this.predicate.toLowerCase();
		String e2 = this.entity2.toLowerCase();

		if (e1.matches(".*wh[o(ich)(at)].*"))
			this.entity1 = "VARIABLE";
		if (r.matches(".*wh[o(ich)(at)].*"))
			this.predicate = "VARIABLE";
		if (e2.matches(".*wh[o(ich)(at)].*"))
			this.entity2 = "VARIABLE";
	}

}
