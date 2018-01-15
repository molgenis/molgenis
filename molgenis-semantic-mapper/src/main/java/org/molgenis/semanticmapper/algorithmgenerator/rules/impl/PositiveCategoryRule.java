package org.molgenis.semanticmapper.algorithmgenerator.rules.impl;

import com.google.common.collect.Sets;

import java.util.Set;

public class PositiveCategoryRule extends InternalAbstractCategoryRule
{
	private final static Set<String> POSITIVE_WORDS = Sets.newHashSet("yes", "ever", "has");

	public PositiveCategoryRule()
	{
		super(POSITIVE_WORDS);
	}
}