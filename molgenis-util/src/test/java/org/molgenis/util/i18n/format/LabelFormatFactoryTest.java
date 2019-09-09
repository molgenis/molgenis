package org.molgenis.util.i18n.format;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.text.Format;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.test.AbstractMockitoTest;
import org.molgenis.util.i18n.Labeled;

class LabelFormatFactoryTest extends AbstractMockitoTest {
  private Format labelFormat;
  @Mock private Labeled labeled;

  @BeforeEach
  void beforeMethod() {
    labelFormat = new LabelFormatFactory().getFormat(null, null, new Locale("de"));
  }

  @Test
  void testFormatLabeled() {
    when(labeled.getLabel("de")).thenReturn("abcde");
    assertEquals(labelFormat.format(labeled), "abcde");
  }

  @Test
  void testFormatString() {
    assertEquals(labelFormat.format("abcde"), "abcde");
  }

  @Test
  void testFormatNull() {
    assertEquals(labelFormat.format(null), "null");
  }
}
