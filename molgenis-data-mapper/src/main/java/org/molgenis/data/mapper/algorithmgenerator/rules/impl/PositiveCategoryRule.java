package org.molgenis.data.mapper.algorithmgenerator.rules.impl;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.mapper.algorithmgenerator.bean.Category;
import org.molgenis.data.mapper.algorithmgenerator.rules.CategoryRule;

import com.google.common.collect.Sets;

public class PositiveCategoryRule implements CategoryRule
{
	private final static Set<String> POSITIVE_WORDS = Sets.newHashSet("yes", "ever", "has");
	private final static String SPLITTER = "\\s";

	@Override
	public boolean isRuleApplied(Category targetCategory, Category sourceCategory)
	{
		String targetLabel = targetCategory.getLabel();
		String sourceLabel = sourceCategory.getLabel();

		return labelContainsPositiveWords(sourceLabel) && labelContainsPositiveWords(targetLabel);
	}

	boolean labelContainsPositiveWords(String label)
	{
		if (StringUtils.isNotBlank(label))
		{
			for (String token : label.toLowerCase().split(SPLITTER))
			{
				if (POSITIVE_WORDS.contains(token)) return true;
			}
		}
		return false;
	}
}