package org.molgenis.hpofilter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;

import org.molgenis.hpofilter.data.HpoFilterDataProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class HpoFilterLogic {
	private HpoFilterDataProvider hpoFilterDataProvider;
	
	@Autowired
	HpoFilterLogic(HpoFilterDataProvider hpoFilterDataProvider) {
		this.hpoFilterDataProvider = hpoFilterDataProvider;
	}
	
	/**
	 * Checks if *any* group in the user input contains a term that contains a gene
	 * @param inputGroups the set of groups, grouped by group id
	 * @param gene the target gene
	 * @return true if a group contains this gene, false if not
	 */
	public boolean inputContainsGene(HashMap<Integer, Stack<HpoFilterInput>> inputGroups, String gene)
	{
		for (Stack<HpoFilterInput> inputGroup : inputGroups.values()) {
			if (inputGroupContainsGene(inputGroup, gene)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Checks if a term in a particular input group contains a gene.
	 * This function fails-fast, and can be described as returning
	 * true if an input does not not contain a gene rather than if it
	 * does
	 * @param inputGroup the input group, containing hpo terms
	 * @param gene the target gene
	 * @return true if group contains gene, false if not
	 */
	private boolean inputGroupContainsGene(Stack<HpoFilterInput> inputGroup, String gene)
	{
		for (HpoFilterInput input : inputGroup) {
			if (!hpoContainsGene(input.hpo(), gene, input.recursive())) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Checks if a specified hpo term contains a specified gene. If 
	 * recursive = true, it will also check the specified HPO's 
	 * children.
	 * @param hpo the filter HPO term
	 * @param gene the variants' gene
	 * @param recursive true if searching children, false if not.
	 * @return true if HPO contains gene, false if hpo does not contain gene
	 */
	private boolean hpoContainsGene(String hpo, String gene, boolean recursive)
	{
		if (hpoFilterDataProvider.getAssocData().containsKey(hpo)) 
			if (hpoFilterDataProvider.getAssocData().get(hpo).contains(gene)) {
				return true;
			}
			if (recursive)
				for (String child : hpoFilterDataProvider.getHPOData().get(hpo))
					if (null != child && hpoContainsGene(child, gene, true))
						return true;
		return false;
	}
	
	public String getInputString(HashMap<Integer, Stack<HpoFilterInput>> groups)
	{
		StringBuilder sb = new StringBuilder();
		Iterator<Stack<HpoFilterInput>> e = groups.values().iterator();
		Stack<HpoFilterInput> group;
		while (e.hasNext()) {
			group = e.next();
			sb.append('(');
			sb.append(getTermGroupString(group));
			sb.append(')');
			if (e.hasNext()) {
				sb.append(" OR ");
			}
		}
		return sb.toString();
	}
	
	private String getTermGroupString(Stack<HpoFilterInput> group) {
		Iterator<HpoFilterInput> e = group.iterator();
		StringBuilder sb = new StringBuilder();
		HpoFilterInput term;
		while (e.hasNext()) {
			term = e.next();
			sb.append(term.hpo());
			if (!term.recursive()) {
				sb.append("[Non-recursive]");
			}
			if (e.hasNext()) {
				sb.append(" AND ");
			}
		}
		return sb.toString();
	}
}