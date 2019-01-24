package org.molgenis.i18n.format;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.text.Format;
import java.util.Locale;
import org.mockito.Mock;
import org.molgenis.i18n.Labeled;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class LabelFormatFactoryTest extends AbstractMockitoTest {
  private Format labelFormat;
  @Mock private Labeled labeled;

  @BeforeMethod
  public void beforeMethod() {
    labelFormat = new LabelFormatFactory().getFormat(null, null, new Locale("de"));
  }

  @Test
  public void testFormatLabeled() {
    when(labeled.getLabel("de")).thenReturn("abcde");
    assertEquals(labelFormat.format(labeled), "abcde");
  }

  @Test
  public void testFormatString() {
    assertEquals(labelFormat.format("abcde"), "abcde");
  }

  @Test
  public void testFormatNull() {
    assertEquals(labelFormat.format(null), "null");
  }
}
