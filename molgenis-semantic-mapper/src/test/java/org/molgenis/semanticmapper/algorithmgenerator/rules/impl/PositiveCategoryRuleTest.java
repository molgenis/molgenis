package org.molgenis.semanticmapper.algorithmgenerator.rules.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.molgenis.semanticmapper.algorithmgenerator.bean.Category;

class PositiveCategoryRuleTest {
  PositiveCategoryRule rule = new PositiveCategoryRule();

  @Test
  void isRuleApplied() {
    assertTrue(
        rule.createCategoryMatchQuality(
                Category.create("0", "has had stroke"), Category.create("1", "yes"))
            .isRuleApplied());
    assertFalse(
        rule.createCategoryMatchQuality(
                Category.create("0", "has had stroke"), Category.create("1", "NO"))
            .isRuleApplied());
  }

  @Test
  void labelContainsPositiveWords() {
    assertFalse(
        StringUtils.isNotBlank(
            rule.getMatchedTermFromTheRulelabelContainsWords("string NO NEVER contain the word!")));
    assertTrue(
        StringUtils.isNotBlank(
            rule.getMatchedTermFromTheRulelabelContainsWords("string EVER contain the word!")));
    assertTrue(
        StringUtils.isNotBlank(
            rule.getMatchedTermFromTheRulelabelContainsWords("string YES contain the word!")));
    assertTrue(
        StringUtils.isNotBlank(
            rule.getMatchedTermFromTheRulelabelContainsWords("string HAS contain the word!")));
  }
}
