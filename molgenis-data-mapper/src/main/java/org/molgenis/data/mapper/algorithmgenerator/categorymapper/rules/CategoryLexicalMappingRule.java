package org.molgenis.data.mapper.algorithmgenerator.categorymapper.rules;

import java.util.HashMap;
import java.util.Map;

public class CategoryLexicalMappingRule implements CustomRule
{
	private final Map<String, String> rules;

	public CategoryLexicalMappingRule()
	{
		this.rules = new HashMap<String, String>();
	}

	public void setupRules()
	{
		rules.put("yes", "ever");
		rules.put("no", "never");
	}

	public boolean isRuleApplied(Object[] objects)
	{
		return false;
	}
}
