package rulebased;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import rulebased.PhraseComparator;
import rulebased.RbQuestionAnalyzer;
import edu.stanford.nlp.trees.TypedDependency;

public class RbMergingRule {

	private List<RbCondition> conditions;

	private String gov = null;
	private String dep = null;
	private String x = null;
	private List<TypedDependency> old_dependencies = new ArrayList<TypedDependency>();

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


	/**
	 * returns the dependency tree with the grammatical dependencies 
	 * merged as implied by this rule
	 * @param dependency_tree
	 * @return
	 */
	public Collection<TypedDependency> evaluateRule(Collection<TypedDependency> typed_dependencies) {
		Collection<TypedDependency> merged_typed_dependencies = new ArrayList<TypedDependency>(typed_dependencies);

		if (conditions.size() == 2) {
			for (TypedDependency dependency : typed_dependencies) {
				for (RbCondition condition : conditions) {

					applyCondition(dependency, condition);

				}
			}
		}

		// check if a valid match was found
		if (gov != null && x != null && dep != null && !old_dependencies.isEmpty()) {

			//delete the old dependencies
			merged_typed_dependencies.removeAll(old_dependencies);

			List<String> newWords = new ArrayList<String>();
			newWords.addAll(Arrays.asList(gov.split(" ")));
			newWords.addAll(Arrays.asList(dep.split(" ")));
			newWords.sort(new PhraseComparator());
			String newWord = "";
			for (String word : newWords) {
				newWord += word + " ";
			}
			//			IndexedWord new_word = new IndexedWord(new Word(a));

			RbQuestionAnalyzer analyser = new RbQuestionAnalyzer();
			analyser.replaceWithNewPhrase(merged_typed_dependencies, newWord, gov, dep, new HashMap<String, Boolean>());

			
			// as rules can apply more than one times, try to apply it again (recursively)
			RbMergingRule new_rule = new RbMergingRule();
			new_rule.setConditions(conditions);
			merged_typed_dependencies = new_rule.evaluateRule(merged_typed_dependencies);

		}

		return merged_typed_dependencies;
	}


	private void applyCondition(TypedDependency dependency, RbCondition condition) {
		int has_matched = 0;
		String tmp_gov = null;
		String tmp_dep = null;
		String tmp_x = null;

		String governor = dependency.gov().backingLabel().toString();
		String dependant = dependency.dep().backingLabel().toString();

		String searched_relation = condition.getGrammaticalRelation();
		String reln = dependency.reln().toString();
		if (!searched_relation.equalsIgnoreCase(reln))
			return;

		if (condition.getGov().equalsIgnoreCase("first")) {
			if (gov == null || gov.equalsIgnoreCase(governor)) {
				tmp_gov = governor;
				has_matched++;
			} else {
				// do nothing
				// return null;
			}
		} else if (condition.getGov().equalsIgnoreCase("second")) {
			if (dep == null || dep.equalsIgnoreCase(governor)) {
				tmp_dep = governor;
				has_matched++;
			} else {
				// do nothing
				// return null;
			}
		} else if (condition.getGov().equalsIgnoreCase("x")) {
			if (x == null || x.equalsIgnoreCase(governor)) {
				tmp_x = governor;
				has_matched++;
			} else {
				// do nothing
				// return null;
			}
		}

		// -----------------------------------

		if (condition.getDep().equalsIgnoreCase("first")) {
			if (gov == null || gov.equalsIgnoreCase(dependant)) {
				tmp_gov = dependant;
				has_matched++;
			} else {
				// do nothing
				// return null;
			}
		} else if (condition.getDep().equalsIgnoreCase("second")) {
			if (dep == null || dep.equalsIgnoreCase(dependant)) {
				tmp_dep = dependant;
				has_matched++;
			} else {
				// do nothing
				// return null;
			}
		} else if (condition.getDep().equalsIgnoreCase("x")) {
			if (x == null || x.equalsIgnoreCase(dependant)) {
				tmp_x = dependant;
				has_matched++;
			} else {
				// do nothing
				// return null;
			}
		}
		
		
		if (has_matched == 2) {
			if (condition.isDeleteAfterCompress()) {
				old_dependencies.add(dependency);
			}
		
			// actually write the values
			if (tmp_gov != null) gov = tmp_gov;
			if (tmp_dep != null) dep = tmp_dep;
			if (tmp_x != null) x = tmp_x;
		}

	}



	@Override
	public String toString() {
		String string = "";
		if (conditions != null)
			for (RbCondition condition : this.conditions) {
				string += " con: " + condition.toString();
			}

		return string;
	}

}
