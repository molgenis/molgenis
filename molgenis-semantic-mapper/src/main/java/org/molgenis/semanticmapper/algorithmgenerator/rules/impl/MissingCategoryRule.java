package org.molgenis.semanticmapper.algorithmgenerator.rules.impl;

import com.google.common.collect.Sets;

import java.util.Set;

public class MissingCategoryRule extends InternalAbstractCategoryRule
{
	private final static Set<String> MISSING_WORDS = Sets.newHashSet("missing", "unknown", "not know", "dont know");

	public MissingCategoryRule()
	{
		super(MISSING_WORDS);
	}

}
