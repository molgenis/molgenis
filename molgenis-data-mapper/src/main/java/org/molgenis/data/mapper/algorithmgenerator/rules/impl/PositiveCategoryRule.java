package org.molgenis.data.mapper.algorithmgenerator.rules.impl;

import java.util.Set;

import com.google.common.collect.Sets;

public class PositiveCategoryRule extends InternalAbstractCategoryRule
{
	private final static Set<String> POSITIVE_WORDS = Sets.newHashSet("yes", "ever", "has");

	public PositiveCategoryRule()
	{
		super(POSITIVE_WORDS);
	}
}