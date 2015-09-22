package org.molgenis.data.mapper.algorithmgenerator.rules.impl;

import org.molgenis.data.mapper.algorithmgenerator.bean.Category;
import org.testng.Assert;
import org.testng.annotations.Test;

public class MissingCategoryRuleTest
{
	MissingCategoryRule rule = new MissingCategoryRule();

	@Test
	public void isRuleApplied()
	{
		Assert.assertTrue(rule.isRuleApplied(Category.create(3, "UNKNOWN"), Category.create(3, "missing")));
		Assert.assertTrue(rule.isRuleApplied(Category.create(3, "not know"), Category.create(3, "missing")));
		Assert.assertTrue(rule.isRuleApplied(Category.create(3, "don`t know"), Category.create(3, "missing")));
		Assert.assertFalse(rule.isRuleApplied(Category.create(0, "has had stroke"), Category.create(1, "NO")));
	}

	@Test
	public void labelContainsMissingeWords()
	{
		Assert.assertTrue(rule.labelContainsWords("string unknown contain the word!"));
		Assert.assertTrue(rule.labelContainsWords("string MISSING contain the word!"));
		Assert.assertFalse(rule.labelContainsWords("string YES contain the word!"));
		Assert.assertFalse(rule.labelContainsWords("string HAS contain the word!"));
	}
}
