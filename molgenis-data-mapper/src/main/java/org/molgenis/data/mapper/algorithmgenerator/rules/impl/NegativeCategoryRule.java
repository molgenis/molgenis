package org.molgenis.data.mapper.algorithmgenerator.rules.impl;

import java.util.Set;

import com.google.common.collect.Sets;

public class NegativeCategoryRule extends InternalAbstractCategoryRule
{
	private final static Set<String> NEGATIVE_WORDS = Sets.newHashSet("not", "no", "never");

	public NegativeCategoryRule()
	{
		super(NEGATIVE_WORDS);
	}
}