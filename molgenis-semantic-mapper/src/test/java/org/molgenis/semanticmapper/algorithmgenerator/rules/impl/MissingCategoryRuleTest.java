package org.molgenis.semanticmapper.algorithmgenerator.rules.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.molgenis.semanticmapper.algorithmgenerator.bean.Category;

class MissingCategoryRuleTest {
  MissingCategoryRule rule = new MissingCategoryRule();

  @Test
  void isRuleApplied() {
    assertTrue(
        rule.createCategoryMatchQuality(
                Category.create("3", "dont know"), Category.create("3", "MIssing"))
            .isRuleApplied());
    assertTrue(
        rule.createCategoryMatchQuality(
                Category.create("3", "UNKNOWN"), Category.create("3", "missing"))
            .isRuleApplied());
    assertTrue(
        rule.createCategoryMatchQuality(
                Category.create("3", "not know"), Category.create("3", "missing"))
            .isRuleApplied());
    assertTrue(
        rule.createCategoryMatchQuality(
                Category.create("3", "don`t know"), Category.create("3", "missing"))
            .isRuleApplied());
    assertTrue(
        rule.createCategoryMatchQuality(
                Category.create("3", "don't know"), Category.create("3", "missing"))
            .isRuleApplied());
    assertFalse(
        rule.createCategoryMatchQuality(
                Category.create("0", "has had stroke"), Category.create("1", "NO"))
            .isRuleApplied());
  }

  @Test
  void removeIllegalChars() {
    assertEquals("dont", rule.removeIllegalChars("don`t"));
    assertEquals("dont", rule.removeIllegalChars("don&%$t"));
    assertNotEquals(rule.removeIllegalChars("don1t"), "dont");
  }

  @Test
  void getMatchedTermFromTheRulelabelContainsWords() {
    assertTrue(
        StringUtils.isNotBlank(
            rule.getMatchedTermFromTheRulelabelContainsWords("string unknown contain the word!")));
    assertTrue(
        StringUtils.isNotBlank(
            rule.getMatchedTermFromTheRulelabelContainsWords("string MISSING contain the word!")));
    assertTrue(
        StringUtils.isBlank(
            rule.getMatchedTermFromTheRulelabelContainsWords("string YES contain the word!")));
    assertTrue(
        StringUtils.isBlank(
            rule.getMatchedTermFromTheRulelabelContainsWords("string HAS contain the word!")));
  }
}
