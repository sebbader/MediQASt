package rulebased;

public class RbImplication {
	private String entity1 = null;
	private String relation = null;
	private String entity2 = null;

	public RbImplication(String entity1, String relation, String entity2) {
		setEntity1(entity1);
		setRelation(relation);
		setEntity2(entity2);
	}

	public String getEntity1() {
		return entity1;
	}

	public void setEntity1(String entity1) {
		this.entity1 = entity1.replace("\"", "");
	}

	public String getRelation() {
		return relation;
	}

	public void setRelation(String relation) {
		this.relation = relation.replace("\"", "");
	}

	public String getEntity2() {
		return entity2;
	}

	public void setEntity2(String entity2) {
		this.entity2 = entity2.replace("\"", "");
	}

	@Override
	public String toString() {
		return "<" + this.entity1 + " " + this.relation + " " + this.entity2
				+ ">";
	}
}
