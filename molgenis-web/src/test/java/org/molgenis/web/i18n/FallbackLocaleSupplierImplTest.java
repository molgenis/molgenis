package org.molgenis.web.i18n;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.settings.AppSettings;
import org.molgenis.test.AbstractMockitoTest;

class FallbackLocaleSupplierImplTest extends AbstractMockitoTest {
  @Mock private AppSettings appSettings;
  private FallbackLocaleSupplierImpl fallbackLocaleSupplierImpl;

  @BeforeEach
  void setUpBeforeMethod() {
    fallbackLocaleSupplierImpl = new FallbackLocaleSupplierImpl(appSettings);
  }

  @Test
  void testFallbackLocaleSupplierImpl() {
    assertThrows(NullPointerException.class, () -> new FallbackLocaleSupplierImpl(null));
  }

  @Test
  void testGet() {
    String languageCode = "nl";
    when(appSettings.getLanguageCode()).thenReturn(languageCode);
    assertEquals(fallbackLocaleSupplierImpl.get(), Locale.forLanguageTag("nl"));
  }
}
