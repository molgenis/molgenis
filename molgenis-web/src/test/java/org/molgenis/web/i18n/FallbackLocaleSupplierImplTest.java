package org.molgenis.web.i18n;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.Locale;
import org.mockito.Mock;
import org.molgenis.settings.AppSettings;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class FallbackLocaleSupplierImplTest extends AbstractMockitoTest {
  @Mock private AppSettings appSettings;
  private FallbackLocaleSupplierImpl fallbackLocaleSupplierImpl;

  @BeforeMethod
  public void setUpBeforeMethod() {
    fallbackLocaleSupplierImpl = new FallbackLocaleSupplierImpl(appSettings);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void testFallbackLocaleSupplierImpl() {
    new FallbackLocaleSupplierImpl(null);
  }

  @Test
  public void testGet() {
    String languageCode = "nl";
    when(appSettings.getLanguageCode()).thenReturn(languageCode);
    assertEquals(fallbackLocaleSupplierImpl.get(), Locale.forLanguageTag("nl"));
  }
}
