package org.molgenis.hpofilter;

import java.util.HashMap;
import java.util.Stack;

import org.molgenis.hpofilter.data.HpoFilterDataProvider;
import org.springframework.beans.factory.annotation.Autowired;


class HpoFilterLogic {
	private HpoFilterDataProvider hpoFilterDataProvider;
	
	@Autowired
	HpoFilterLogic(HpoFilterDataProvider hpoFilterDataProvider) {
		this.hpoFilterDataProvider = hpoFilterDataProvider;
	}
	
	public boolean inputContainsGene(HashMap<Integer, Stack<HpoFilterInput>> inputGroups, String gene)
	{
		for (Stack<HpoFilterInput> inputGroup : inputGroups.values()) {
			if (inputGroupContainsGene(inputGroup, gene)) {
				return true;
			}
		}
		return false;
	}
	
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
	 * Checks if a specified hpo contains a specified gene. If 
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
}