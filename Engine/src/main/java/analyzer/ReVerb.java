package analyzer;

import inputmanagement.impl.QueryTriple;

import java.io.IOException;
import java.util.List;

public interface ReVerb {

	/**
	 * starts the ReVerb app
	 * 
	 * @param inputQuestion
	 * @throws IOException
	 */
	public List<QueryTriple> run(String inputQuestion) throws IOException;

}
