package org.molgenis.util.i18n.format;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.text.Format;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.test.AbstractMockitoTest;
import org.molgenis.util.i18n.Identifiable;

class IdFormatFactoryTest extends AbstractMockitoTest {
  private Format idFormat;
  @Mock private Identifiable identifiable;

  @BeforeEach
  void beforeMethod() {
    idFormat = new IdFormatFactory().getFormat(null, null, null);
  }

  @Test
  void testFormatIdentifiable() {
    when(identifiable.getIdValue()).thenReturn("abcde");
    assertEquals("abcde", idFormat.format(identifiable));
  }

  @Test
  void testFormatString() {
    assertEquals("abcde", idFormat.format("abcde"));
  }

  @Test
  void testFormatNull() {
    assertEquals("null", idFormat.format(null));
  }
}
