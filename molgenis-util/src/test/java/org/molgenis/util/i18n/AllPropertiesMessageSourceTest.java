package org.molgenis.util.i18n;

import static com.google.common.collect.ImmutableSetMultimap.of;
import static java.util.Locale.ENGLISH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Locale;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AllPropertiesMessageSourceTest {
  private AllPropertiesMessageSource propertiesMessageSource;
  private Locale DUTCH = new Locale("nl");

  @BeforeEach
  void setUp() throws Exception {
    propertiesMessageSource = new AllPropertiesMessageSource();
    propertiesMessageSource.addMolgenisNamespaces("test");
  }

  @AfterEach
  void tearDown() throws Exception {}

  @Test
  void testGetAllMessageIds() throws Exception {
    assertEquals(
        of("test", "EN_ONLY", "test", "EN_PLUS_NL", "test", "BIOBANK_UTF8", "test", "NL_ONLY"),
        propertiesMessageSource.getAllMessageIds());
  }

  @Test
  void testGetMessageSpecifiedInBundle() throws Exception {
    assertEquals(
        "alleen Nederlands", propertiesMessageSource.resolveCodeWithoutArguments("NL_ONLY", DUTCH));
  }

  @Test
  void testGetMessageSpecifiedInOtherBundle() throws Exception {
    assertNull(propertiesMessageSource.resolveCodeWithoutArguments("EN_ONLY", new Locale("nl")));
  }

  @Test
  void testGetMessageNotSpecified() throws Exception {
    assertNull(propertiesMessageSource.resolveCodeWithoutArguments("MISSING", new Locale("nl")));
  }

  @Test
  void testGetMessageSpecifiedInBoth() throws Exception {
    assertEquals(
        "English plus Dutch",
        propertiesMessageSource.resolveCodeWithoutArguments("EN_PLUS_NL", ENGLISH));
    assertEquals(
        "Engels plus Nederlands",
        propertiesMessageSource.resolveCodeWithoutArguments("EN_PLUS_NL", new Locale("nl")));
  }

  @Test
  void testGetMessageUTF8() {
    assertEquals(
        "Biøbånk\uD83D\uDC00",
        propertiesMessageSource.resolveCodeWithoutArguments("BIOBANK_UTF8", ENGLISH));
  }
}
