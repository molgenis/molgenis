package org.molgenis.data.mapper.algorithmgenerator.categorymapper.rules;

public interface CustomRule
{
	public void setupRules();

	public boolean isRuleApplied(Object[] objects);
}
