package org.molgenis.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class RegexUtilsTest {
  private static String javaCronJobRegex;
  private static String javaCommaSeparatedEmailListRegex;

  @BeforeAll
  static void setup() {
    javaCronJobRegex = toJavaRegex(RegexUtils.JAVA_SCRIPT_CRON_REGEX);
    javaCommaSeparatedEmailListRegex =
        toJavaRegex(RegexUtils.JAVA_SCRIPT_COMMA_SEPARATED_EMAIL_LIST_REGEX);
  }

  @Test
  void cronJobRegex() {
    assertTrue("0 0 12 * * ?".matches(javaCronJobRegex));
    assertTrue("".matches(javaCronJobRegex));
  }

  @Test
  void commaSeparatedEmailListRegex() {
    assertTrue("c.stroomberg@umcg.nl".matches(javaCommaSeparatedEmailListRegex));
    assertTrue(
        "c.stroomberg@umcg.nl, janjansen@gmail.com".matches(javaCommaSeparatedEmailListRegex));
    assertTrue(
        "c.stroomberg@umcg.nl,janjansen@gmail.com".matches(javaCommaSeparatedEmailListRegex));
    assertTrue("".matches(javaCommaSeparatedEmailListRegex));
    assertTrue("undefined".matches(javaCommaSeparatedEmailListRegex));
    assertTrue("null".matches(javaCommaSeparatedEmailListRegex));

    assertFalse("c.stroomberg".matches(javaCommaSeparatedEmailListRegex));
    assertFalse("c.stroomberg@".matches(javaCommaSeparatedEmailListRegex));
    assertFalse("umcg.nl".matches(javaCommaSeparatedEmailListRegex));
    assertFalse(
        "c.stroomberg@umcg.nljanjansen@gmail.com".matches(javaCommaSeparatedEmailListRegex));
    assertFalse(
        "c.stroomberg@umcg.nl janjansen@gmail.com".matches(javaCommaSeparatedEmailListRegex));
  }

  private static String toJavaRegex(String javaScriptRegex) {
    return javaScriptRegex.substring(1, javaScriptRegex.length() - 1);
  }
}
