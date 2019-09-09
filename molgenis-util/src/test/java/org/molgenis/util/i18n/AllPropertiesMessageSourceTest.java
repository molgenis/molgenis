package org.molgenis.util.i18n;

import static java.util.Locale.ENGLISH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.google.common.collect.ImmutableSetMultimap;
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
        propertiesMessageSource.getAllMessageIds(),
        ImmutableSetMultimap.of(
            "test", "EN_ONLY", "test", "EN_PLUS_NL", "test", "BIOBANK_UTF8", "test", "NL_ONLY"));
  }

  @Test
  void testGetMessageSpecifiedInBundle() throws Exception {
    assertEquals(
        propertiesMessageSource.resolveCodeWithoutArguments("NL_ONLY", DUTCH), "alleen Nederlands");
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
        propertiesMessageSource.resolveCodeWithoutArguments("EN_PLUS_NL", ENGLISH),
        "English plus Dutch");
    assertEquals(
        propertiesMessageSource.resolveCodeWithoutArguments("EN_PLUS_NL", new Locale("nl")),
        "Engels plus Nederlands");
  }

  @Test
  void testGetMessageUTF8() {
    assertEquals(
        propertiesMessageSource.resolveCodeWithoutArguments("BIOBANK_UTF8", ENGLISH),
        "Biøbånk\uD83D\uDC00");
  }
}
