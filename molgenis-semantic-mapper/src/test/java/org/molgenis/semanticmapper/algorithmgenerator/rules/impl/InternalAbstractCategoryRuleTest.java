package org.molgenis.semanticmapper.algorithmgenerator.rules.impl;

import org.molgenis.semanticmapper.algorithmgenerator.bean.Category;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InternalAbstractCategoryRuleTest
{
	@Test
	public void testCreateNumericQualityIndicatorDoesNotThrowNPE()
	{
		InternalAbstractCategoryRule rule = new NegativeCategoryRule();

		Category target = mock(Category.class);
		when(target.getLabel()).thenReturn("label1");
		Category source = mock(Category.class);
		when(source.getLabel()).thenReturn("label2");

		rule.createCategoryMatchQuality(target, source);
	}
}