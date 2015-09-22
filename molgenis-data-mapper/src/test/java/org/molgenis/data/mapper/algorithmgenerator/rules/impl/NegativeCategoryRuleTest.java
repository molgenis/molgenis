package org.molgenis.data.mapper.algorithmgenerator.rules.impl;

import org.molgenis.data.mapper.algorithmgenerator.bean.Category;
import org.testng.Assert;
import org.testng.annotations.Test;

public class NegativeCategoryRuleTest
{

	NegativeCategoryRule rule = new NegativeCategoryRule();

	@Test
	public void isRuleApplied()
	{
		Assert.assertTrue(rule.isRuleApplied(Category.create(0, "never had stroke"), Category.create(1, "NO")));
		Assert.assertFalse(rule.isRuleApplied(Category.create(0, "has had stroke"), Category.create(1, "NO")));
	}

	@Test
	public void labelContainsPositiveWords()
	{
		Assert.assertTrue(rule.labelContainsNegativeWords("string NO NEVER contain the word!"));
		Assert.assertFalse(rule.labelContainsNegativeWords("string EVER contain the word!"));
		Assert.assertFalse(rule.labelContainsNegativeWords("string YES contain the word!"));
		Assert.assertFalse(rule.labelContainsNegativeWords("string HAS contain the word!"));
	}
}
