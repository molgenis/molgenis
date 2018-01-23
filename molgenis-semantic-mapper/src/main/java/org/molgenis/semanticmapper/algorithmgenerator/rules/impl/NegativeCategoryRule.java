package org.molgenis.semanticmapper.algorithmgenerator.rules.impl;

import com.google.common.collect.Sets;

import java.util.Set;

public class NegativeCategoryRule extends InternalAbstractCategoryRule
{
	private final static Set<String> NEGATIVE_WORDS = Sets.newHashSet("not", "no", "never");

	public NegativeCategoryRule()
	{
		super(NEGATIVE_WORDS);
	}
}