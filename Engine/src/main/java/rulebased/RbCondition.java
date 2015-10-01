package rulebased;

public class RbCondition {

	private String first;
	private String second;
	private String grammaticalRelation;

	public RbCondition(String grammaticalRelation, String first,
			String second) {
		this.setFirst(first);
		this.setSecond(second);
		this.setGrammaticalRelation(grammaticalRelation);
	}

	public String getFirst() {
		return first;
	}

	public void setFirst(String first) {
		this.first = first;
	}

	public String getSecond() {
		return second;
	}

	public void setSecond(String second) {
		this.second = second;
	}

	public String getGrammaticalRelation() {
		return grammaticalRelation;
	}

	public void setGrammaticalRelation(String grammaticalRelation) {
		this.grammaticalRelation = grammaticalRelation;
	}

	@Override
	public String toString() {
		return this.grammaticalRelation + "(" + this.first + ", " + this.second
				+ ")";
	}
}
