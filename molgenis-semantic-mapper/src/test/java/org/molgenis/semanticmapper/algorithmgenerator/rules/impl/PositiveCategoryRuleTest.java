package org.molgenis.semanticmapper.algorithmgenerator.rules.impl;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.semanticmapper.algorithmgenerator.bean.Category;
import org.testng.Assert;
import org.testng.annotations.Test;

public class PositiveCategoryRuleTest
{
	PositiveCategoryRule rule = new PositiveCategoryRule();

	@Test
	public void isRuleApplied()
	{
		Assert.assertTrue(
				rule.createCategoryMatchQuality(Category.create("0", "has had stroke"), Category.create("1", "yes"))
					.isRuleApplied());
		Assert.assertFalse(
				rule.createCategoryMatchQuality(Category.create("0", "has had stroke"), Category.create("1", "NO"))
					.isRuleApplied());
	}

	@Test
	public void labelContainsPositiveWords()
	{
		Assert.assertFalse(StringUtils.isNotBlank(
				rule.getMatchedTermFromTheRulelabelContainsWords("string NO NEVER contain the word!")));
		Assert.assertTrue(StringUtils.isNotBlank(
				rule.getMatchedTermFromTheRulelabelContainsWords("string EVER contain the word!")));
		Assert.assertTrue(StringUtils.isNotBlank(
				rule.getMatchedTermFromTheRulelabelContainsWords("string YES contain the word!")));
		Assert.assertTrue(StringUtils.isNotBlank(
				rule.getMatchedTermFromTheRulelabelContainsWords("string HAS contain the word!")));
	}
}
