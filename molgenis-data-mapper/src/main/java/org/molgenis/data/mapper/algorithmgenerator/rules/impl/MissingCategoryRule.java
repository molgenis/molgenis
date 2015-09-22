package org.molgenis.data.mapper.algorithmgenerator.rules.impl;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.mapper.algorithmgenerator.bean.Category;
import org.molgenis.data.mapper.algorithmgenerator.rules.CategoryRule;

import com.google.common.collect.Sets;

public class MissingCategoryRule implements CategoryRule
{
	private final static Set<String> MISSING_WORDS = Sets.newHashSet("missing", "unknown");
	private final static String SPLITTER = "\\s";

	@Override
	public boolean isRuleApplied(Category targetCategory, Category sourceCategory)
	{
		String lowerCasedTargetLabel = targetCategory.getLabel();
		String lowerCasedSourceLabel = sourceCategory.getLabel();

		return labelContainsMissingWords(lowerCasedSourceLabel) && labelContainsMissingWords(lowerCasedTargetLabel);
	}

	boolean labelContainsMissingWords(String label)
	{
		if (StringUtils.isNotBlank(label))
		{
			for (String token : label.toLowerCase().split(SPLITTER))
			{
				if (MISSING_WORDS.contains(token)) return true;
			}
		}
		return false;
	}
}
