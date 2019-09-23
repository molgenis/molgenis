package org.molgenis.i18n;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class I18nUtilsTest {

  @Test
  void isI18n() {
    assertTrue(I18nUtils.isI18n("test-nl"));
    assertTrue(I18nUtils.isI18n("test-xxx"));
    assertFalse(I18nUtils.isI18n("test"));
    assertFalse(I18nUtils.isI18n("test-NL"));
    assertFalse(I18nUtils.isI18n("test-nlnl"));
    assertFalse(I18nUtils.isI18n("test-"));
    assertFalse(I18nUtils.isI18n("test-n1"));
    assertFalse(I18nUtils.isI18n("-nl"));
  }

  @Test
  void getLanguageCode() {
    assertEquals("nl", I18nUtils.getLanguageCode("test-nl"));
    assertEquals("xxx", I18nUtils.getLanguageCode("test-xxx"));
    assertNull(I18nUtils.getLanguageCode("test"));
  }
}
