package inputmanagement.candidates;

import java.util.List;

public interface Candidate {

	/**
	 * The score from zero to infinity indicating the believe in the candidate.
	 * 
	 * @return
	 */
	public double getScore();

	/**
	 * set the score from zero to infinity indicating the believe in the
	 * candidate.
	 * 
	 * @param score
	 */
	public void setScore(double score);

	/**
	 * return the uri of the candidate
	 * 
	 * @return
	 */
	public String getText();

}
