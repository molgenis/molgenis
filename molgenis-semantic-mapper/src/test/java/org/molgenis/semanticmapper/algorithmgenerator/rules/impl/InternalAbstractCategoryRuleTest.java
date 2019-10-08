package org.molgenis.semanticmapper.algorithmgenerator.rules.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.molgenis.semanticmapper.algorithmgenerator.bean.Category;

class InternalAbstractCategoryRuleTest {
  @Test
  void testCreateNumericQualityIndicatorDoesNotThrowNPE() {
    InternalAbstractCategoryRule rule = new NegativeCategoryRule();

    Category target = mock(Category.class);
    when(target.getLabel()).thenReturn("label1");
    Category source = mock(Category.class);
    when(source.getLabel()).thenReturn("label2");

    rule.createCategoryMatchQuality(target, source);
  }
}
