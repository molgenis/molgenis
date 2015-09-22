package org.molgenis.data.mapper.algorithmgenerator.rules.impl;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.mapper.algorithmgenerator.bean.Category;
import org.molgenis.data.mapper.algorithmgenerator.rules.CategoryRule;

import com.google.common.collect.Sets;

public class NegativeCategoryRule implements CategoryRule
{
	private final static Set<String> NEGATIVE_WORDS = Sets.newHashSet("no", "never");
	private final static String SPLITTER = "\\s";

	@Override
	public boolean isRuleApplied(Category targetCategory, Category sourceCategory)
	{
		String lowerCasedTargetLabel = targetCategory.getLabel();
		String lowerCasedSourceLabel = sourceCategory.getLabel();

		return labelContainsNegativeWords(lowerCasedSourceLabel) && labelContainsNegativeWords(lowerCasedTargetLabel);
	}

	boolean labelContainsNegativeWords(String label)
	{
		if (StringUtils.isNotBlank(label))
		{
			for (String token : label.toLowerCase().split(SPLITTER))
			{
				if (NEGATIVE_WORDS.contains(token)) return true;
			}
		}
		return false;
	}
}