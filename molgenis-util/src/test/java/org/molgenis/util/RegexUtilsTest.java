package org.molgenis.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.molgenis.util.RegexUtils.COMMA_SEPARATED_EMAIL_LIST_REGEX;
import static org.molgenis.util.RegexUtils.CRON_REGEX;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class RegexUtilsTest {

  @Test
  void cronJobRegex() {
    assertTrue("0 0 12 * * ?".matches(CRON_REGEX));
    assertTrue("".matches(CRON_REGEX));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "c.stroomberg@umcg.nl",
        "c.stroomberg@umcg.nl, janjansen@gmail.com",
        "c.stroomberg@umcg.nl,janjansen@gmail.com",
        ""
      })
  void commaSeparatedEmailListRegex(String value) {
    assertTrue(value.matches(COMMA_SEPARATED_EMAIL_LIST_REGEX));

    assertFalse("c.stroomberg".matches(COMMA_SEPARATED_EMAIL_LIST_REGEX));
    assertFalse("c.stroomberg@".matches(COMMA_SEPARATED_EMAIL_LIST_REGEX));
    assertFalse("umcg.nl".matches(COMMA_SEPARATED_EMAIL_LIST_REGEX));
    assertFalse(
        "c.stroomberg@umcg.nljanjansen@gmail.com".matches(COMMA_SEPARATED_EMAIL_LIST_REGEX));
    assertFalse(
        "c.stroomberg@umcg.nl janjansen@gmail.com".matches(COMMA_SEPARATED_EMAIL_LIST_REGEX));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "c.stroomberg",
        "c.stroomberg@",
        "umcg.nl",
        "c.stroomberg@umcg.nljanjansen@gmail.com",
        "c.stroomberg@umcg.nl janjansen@gmail.com",
      })
  void commaSeparatedEmailListRegexInvalid(String value) {
    assertFalse(value.matches(COMMA_SEPARATED_EMAIL_LIST_REGEX));
  }
}
