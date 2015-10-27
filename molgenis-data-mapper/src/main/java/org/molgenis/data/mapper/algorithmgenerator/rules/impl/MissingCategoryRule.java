package org.molgenis.data.mapper.algorithmgenerator.rules.impl;

import java.util.Set;

import com.google.common.collect.Sets;

public class MissingCategoryRule extends InternalAbstractCategoryRule
{
	private final static Set<String> MISSING_WORDS = Sets.newHashSet("missing", "unknown", "not know", "dont know");

	public MissingCategoryRule()
	{
		super(MISSING_WORDS);
	}

}
