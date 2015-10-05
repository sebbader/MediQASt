package rulebased;

public class RbCondition {

	private String gov;
	private String dep;
	private String grammaticalRelation;
	private boolean deleteAfterCompress;

	public RbCondition(String grammaticalRelation, String gov,
			String dep) {
		this.setGov(gov);
		this.setDep(dep);
		this.setGrammaticalRelation(grammaticalRelation);
	}
	
	public RbCondition(String grammaticalRelation, String gov, String dep, boolean deleteAfterCompress) {
		this.setGov(gov);
		this.setDep(dep);
		this.setGrammaticalRelation(grammaticalRelation);
		this.setDeleteAfterCompress(deleteAfterCompress);
	}

	public String getGov() {
		return gov;
	}

	public void setGov(String gov) {
		this.gov = gov.trim();
	}

	public String getDep() {
		return dep;
	}

	public void setDep(String dep) {
		this.dep = dep.trim();
	}

	public String getGrammaticalRelation() {
		return grammaticalRelation;
	}

	public void setGrammaticalRelation(String grammaticalRelation) {
		this.grammaticalRelation = grammaticalRelation.trim();
	}

	@Override
	public String toString() {
		return this.grammaticalRelation + "(" + this.gov + ", " + this.dep
				+ ")";
	}

	public boolean isDeleteAfterCompress() {
		return deleteAfterCompress;
	}

	public void setDeleteAfterCompress(boolean deleteAfterCompress) {
		this.deleteAfterCompress = deleteAfterCompress;
	}
}
