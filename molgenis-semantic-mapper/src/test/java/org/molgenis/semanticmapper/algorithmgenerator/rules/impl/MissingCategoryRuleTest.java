package org.molgenis.semanticmapper.algorithmgenerator.rules.impl;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.semanticmapper.algorithmgenerator.bean.Category;
import org.testng.Assert;
import org.testng.annotations.Test;

public class MissingCategoryRuleTest
{
	MissingCategoryRule rule = new MissingCategoryRule();

	@Test
	public void isRuleApplied()
	{
		Assert.assertTrue(
				rule.createCategoryMatchQuality(Category.create("3", "dont know"), Category.create("3", "MIssing"))
					.isRuleApplied());
		Assert.assertTrue(
				rule.createCategoryMatchQuality(Category.create("3", "UNKNOWN"), Category.create("3", "missing"))
					.isRuleApplied());
		Assert.assertTrue(
				rule.createCategoryMatchQuality(Category.create("3", "not know"), Category.create("3", "missing"))
					.isRuleApplied());
		Assert.assertTrue(
				rule.createCategoryMatchQuality(Category.create("3", "don`t know"), Category.create("3", "missing"))
					.isRuleApplied());
		Assert.assertTrue(
				rule.createCategoryMatchQuality(Category.create("3", "don't know"), Category.create("3", "missing"))
					.isRuleApplied());
		Assert.assertFalse(
				rule.createCategoryMatchQuality(Category.create("0", "has had stroke"), Category.create("1", "NO"))
					.isRuleApplied());
	}

	@Test
	public void removeIllegalChars()
	{
		Assert.assertEquals(rule.removeIllegalChars("don`t"), "dont");
		Assert.assertEquals(rule.removeIllegalChars("don&%$t"), "dont");
		Assert.assertNotEquals(rule.removeIllegalChars("don1t"), "dont");
	}

	@Test
	public void getMatchedTermFromTheRulelabelContainsWords()
	{
		Assert.assertTrue(StringUtils.isNotBlank(
				rule.getMatchedTermFromTheRulelabelContainsWords("string unknown contain the word!")));
		Assert.assertTrue(StringUtils.isNotBlank(
				rule.getMatchedTermFromTheRulelabelContainsWords("string MISSING contain the word!")));
		Assert.assertTrue(
				StringUtils.isBlank(rule.getMatchedTermFromTheRulelabelContainsWords("string YES contain the word!")));
		Assert.assertTrue(
				StringUtils.isBlank(rule.getMatchedTermFromTheRulelabelContainsWords("string HAS contain the word!")));
	}
}
