package rulebased;

import inputmanagement.impl.QueryTriple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.stanford.nlp.trees.TypedDependency;

public class RbConstructingRule {

	private List<RbCondition> conditions;
	private List<RbImplication> implications;
	private boolean isDefault;

	private String entity1 = null;
	private String relation1 = null;
	private String entity2 = null;
	private String relation2 = null;
	private String entity3 = null;

	public List<RbCondition> getConditions() {
		return conditions;
	}

	public void setConditions(List<RbCondition> conditions) {
		this.conditions = new ArrayList<RbCondition>(conditions);
	}

	public void addCondition(RbCondition condition) {
		if (this.conditions == null)
			this.conditions = new ArrayList<RbCondition>();
		this.conditions.add(condition);
	}

	public List<RbImplication> getImplications() {
		return this.implications;
	}

	public void addImplication(RbImplication implification) {
		if (this.implications == null)
			this.implications = new ArrayList<RbImplication>();
		this.implications.add(implification);
	}

	public void setImplication(List<RbImplication> implifications) {
		this.implications = implifications;
	}

	public List<QueryTriple> evaluateRule(
			Collection<TypedDependency> compressed_dependency_tree,
			boolean isEmpty) {
		List<QueryTriple> queryTriples = new ArrayList<QueryTriple>();

		// check whether the default rule shall be applied. Only if no triple
		// could be created yet.
		if (isDefault && !isEmpty)
			return null;

		for (TypedDependency dependency : compressed_dependency_tree) {

			if (conditions.size() == 3) {
				queryTriples = applyConnectedRule(conditions, dependency);
				if (queryTriples != null && !queryTriples.isEmpty())
					return queryTriples;
			}

			if (conditions.size() == 2) {
				QueryTriple newQueryTriple = applyRule(conditions, dependency);
				if (newQueryTriple != null) {
					queryTriples.add(newQueryTriple);
					return queryTriples;
				}
			}

			if (conditions.size() == 1) {
				queryTriples = applyRegexRule(conditions.get(0), dependency);
				if (queryTriples != null && !queryTriples.isEmpty())
					return queryTriples;
			}
		}

		if (queryTriples != null && !queryTriples.isEmpty()) {
			return queryTriples;
		} else {
			return null;
		}

	}

	private List<QueryTriple> applyConnectedRule(
			List<RbCondition> conditions, TypedDependency dependency) {
		List<QueryTriple> queryTriples = new ArrayList<QueryTriple>();

		int numberOfHits = 0;

		String gov = dependency.gov().backingLabel().toString();
		String dep = dependency.dep().backingLabel().toString();

		for (RbCondition condition : conditions) {

			String searched_relation = condition.getGrammaticalRelation();
			String reln = dependency.reln().toString();
			if (!searched_relation.equalsIgnoreCase(reln))
				continue;

			if (condition.getGov().equalsIgnoreCase("first")) {
				if (entity1 == null || entity1.equalsIgnoreCase(gov)) {
					entity1 = gov;
					numberOfHits++;
				} else {
					// do nothing
					// return null;

					break;
				}
			} else if (condition.getGov().equalsIgnoreCase("second")) {
				if (relation1 == null || relation1.equalsIgnoreCase(gov)) {
					relation1 = gov;
					numberOfHits++;
				} else {
					// do nothing
					// return null;
					break;
				}
			} else if (condition.getGov().equalsIgnoreCase("third")) {
				if ((entity2 == null || entity2.equalsIgnoreCase(gov))
						&& (relation2 == null || relation2
								.equalsIgnoreCase(gov))) {
					entity2 = gov;
					relation2 = gov;
					numberOfHits++;
				}

			} else if (condition.getGov().equalsIgnoreCase("fourth")) {
				if (entity3 == null || entity3.equalsIgnoreCase(gov)) {
					entity3 = gov;
					numberOfHits++;
				} else {
					// do nothing
					// return null;
					break;
				}
			}

			// -----------------------------------

			if (condition.getDep().equalsIgnoreCase("first")) {
				if (entity1 == null || entity1.equalsIgnoreCase(dep)) {
					entity1 = dep;
					numberOfHits++;
				} else {
					// do nothing
					// return null;
					break;
				}
			} else if (condition.getDep().equalsIgnoreCase("second")) {
				if (relation1 == null || relation1.equalsIgnoreCase(dep)) {
					relation1 = dep;
					numberOfHits++;
				} else {
					// do nothing
					// return null;
					break;
				}
			} else if (condition.getDep().equalsIgnoreCase("third")) {
				if ((entity2 == null || entity2.equalsIgnoreCase(dep))
						&& (relation2 == null || relation2
								.equalsIgnoreCase(dep))) {
					entity2 = dep;
					relation2 = dep;
					numberOfHits++;
				}

			} else if (condition.getDep().equalsIgnoreCase("fourth")) {
				if (entity3 == null || entity3.equalsIgnoreCase(dep)) {
					entity3 = dep;
					numberOfHits++;
				} else {
					// do nothing
					// return null;
					break;
				}
			}

			// no TypedDependency is allowed to set more than 2 entries
			if (numberOfHits >= 2) {
				condition.setGrammaticalRelation("already_applied");
				break;
			}
		}

		// each single condition of the rule has to apply
		if (entity1 != null && relation1 != null && entity2 != null
				&& relation2 != null && entity3 != null) {
			String entity2var = "?node";
			queryTriples.add(new QueryTriple(entity1, relation1, entity2var));
			queryTriples.add(new QueryTriple(entity2var, relation2, entity3));
			return queryTriples;
		} else {
			return null;
		}
	}

	private QueryTriple applyRule(List<RbCondition> conditions,
			TypedDependency dependency) {

		String gov = dependency.gov().backingLabel().toString();
		String dep = dependency.dep().backingLabel().toString();

		for (RbCondition condition : conditions) {

			String searched_relation = condition.getGrammaticalRelation();
			String reln = dependency.reln().toString();
			if (!searched_relation.equalsIgnoreCase(reln))
				continue;

			if (condition.getGov().equalsIgnoreCase("first")) {
				if (entity1 == null || entity1.equalsIgnoreCase(gov)) {
					entity1 = gov;
				} else {
					// do nothing
					// return null;
				}
			} else if (condition.getGov().equalsIgnoreCase("second")) {
				if (relation1 == null || relation1.equalsIgnoreCase(gov)) {
					relation1 = gov;
				} else {
					// do nothing
					// return null;
				}
			} else if (condition.getGov().equalsIgnoreCase("third")) {
				if (entity2 == null || entity2.equalsIgnoreCase(gov)) {
					entity2 = gov;
				} else {
					// do nothing
					// return null;
				}
			}

			// -----------------------------------

			if (condition.getDep().equalsIgnoreCase("first")) {
				if (entity1 == null || entity1.equalsIgnoreCase(dep)) {
					entity1 = dep;
				} else {
					// do nothing
					// return null;
				}
			} else if (condition.getDep().equalsIgnoreCase("second")) {
				if (relation1 == null || relation1.equalsIgnoreCase(dep)) {
					relation1 = dep;
				} else {
					// do nothing
					// return null;
				}
			} else if (condition.getDep().equalsIgnoreCase("third")) {
				if (entity2 == null || entity2.equalsIgnoreCase(dep)) {
					entity2 = dep;
				} else {
					// do nothing
					// return null;
				}
			}

			// each single condition of the rule has to apply
			if (entity1 != null && relation1 != null && entity2 != null) {
				return new QueryTriple(entity1, relation1, entity2);
			}
		}

		return null;
	}

	private List<QueryTriple> applyRegexRule(RbCondition condition,
			TypedDependency dependency) {
		String first = null;
		String second = null;
		String third = null;

		String gov = dependency.gov().backingLabel().toString().toLowerCase();
		String dep = dependency.dep().backingLabel().toString().toLowerCase();

		String searched_relation = condition.getGrammaticalRelation();
		String reln = dependency.reln().toString();
		if (!searched_relation.equalsIgnoreCase(reln))
			return null;

		// check if condition is formulated as regex
		String con_first = condition.getGov();
		if (con_first.startsWith("\"")) {

			// check if GOV matches regex, if not: abort
			String con = ".*" + condition.getGov().replace("\"", "") + ".*";
			if (!gov.matches(con))
				return null;

		} else if (condition.getGov().equalsIgnoreCase("first")) {
			// start of dependency is on place 'first' of triple <first, second, third>
			first = gov;
		} else if (condition.getGov().equalsIgnoreCase("second")) {
			// start of dependency is on place 'second' of triple <first, second, third>
			second = gov;
		} else if (condition.getGov().equalsIgnoreCase("third")) {
			// start of dependency is on place 'third' of triple <first, second, third>
			third = gov;
		}
		// check if condition is formulated as regex
		String con_second = condition.getDep();
		if (con_second.startsWith("\"")) {

			// check if DEP matches regex, if not: abort
			String con = ".*" + condition.getDep().replace("\"", "") + ".*";
			if (!dep.matches(con))
				return null;

		} else if (condition.getDep().equalsIgnoreCase("first")) {
			// end of dependency is on place 'first' of triple <first, second, third>
			first = dep;
		} else if (condition.getDep().equalsIgnoreCase("second")) {
			// end of dependency is on place 'second' of triple <first, second, third>
			second = dep;
		} else if (condition.getDep().equalsIgnoreCase("third")) {
			// end of dependency is on place 'third' of triple <first, second, third>
			third = dep;
		}

		List<QueryTriple> triples = new ArrayList<QueryTriple>();
		for (RbImplication implication : implications) {
			if (implication == null)
				continue;
			String entity1 = implication.getEntity1();
			String relation = implication.getRelation();
			String entity2 = implication.getEntity2();

			if (entity1.equalsIgnoreCase("first")) {
				entity1 = first;
			} else if (entity1.equalsIgnoreCase("second")) {
				entity1 = second;
			} else if (entity1.equalsIgnoreCase("third")) {
				entity1 = third;
			}

			if (relation.equalsIgnoreCase("first")) {
				relation = first;
			} else if (relation.equalsIgnoreCase("second")) {
				relation = second;
			} else if (relation.equalsIgnoreCase("third")) {
				relation = third;
			}

			if (entity2.equalsIgnoreCase("first")) {
				entity2 = first;
			} else if (entity2.equalsIgnoreCase("second")) {
				entity2 = second;
			} else if (entity2.equalsIgnoreCase("third")) {
				entity2 = third;
			}

			if (notEmpty(entity1) && notEmpty(relation) && notEmpty(entity2)) {
				triples.add(new QueryTriple(entity1, relation, entity2));
			} else {
				// do nothing
			}
		}
		return triples;
	}

	private boolean notEmpty(String str) {
		if (str == null)
			return false;
		// if (str.equalsIgnoreCase("")) return false;
		return true;
	}

	@Override
	public String toString() {
		String string = "";
		if (conditions != null)
			for (RbCondition condition : this.conditions) {
				string += " con: " + condition.toString();
			}

		if (implications != null)
			for (RbImplication implication : this.implications) {
				string += " implication: " + implication.toString();
			}

		return string;
	}

	public boolean isDefault() {
		return isDefault;
	}

	public void setDefault(boolean isDefault) {
		this.isDefault = isDefault;
	}
}
