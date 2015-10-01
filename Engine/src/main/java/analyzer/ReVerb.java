package analyzer;

import java.io.IOException;

public interface ReVerb {

	/**
	 * starts the ReVerb app
	 * 
	 * @param inputQuestion
	 * @throws IOException
	 */
	public void run(String inputQuestion) throws IOException;

	/**
	 * @return get the subject/object before the verb
	 */
	public String getArg1();

	/**
	 * @return get the subject/object after the verb
	 */
	public String getArg2();

	/**
	 * @return get the verb
	 */
	public String getRelation();
}
