package org.molgenis.data.security.exception;

import static org.testng.Assert.assertEquals;

import org.molgenis.i18n.CodedRuntimeException;
import org.molgenis.i18n.test.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class SystemRlsModificationExceptionTest extends ExceptionMessageTest {
  @BeforeMethod
  public void setUp() {
    messageSource.addMolgenisNamespaces("data-security");
  }

  @Test(dataProvider = "languageMessageProvider")
  @Override
  public void testGetLocalizedMessage(String lang, String message) {
    ExceptionMessageTest.assertExceptionMessageEquals(
        new SystemRlsModificationException("type"), lang, message);
  }

  @Test
  public void testGetMessage() {
    CodedRuntimeException ex = new SystemRlsModificationException("type");
    assertEquals(ex.getMessage(), "entityType:type");
  }

  @DataProvider(name = "languageMessageProvider")
  @Override
  public Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {
        "en",
        "Adding or removing row level security is not allowed for 'type' because it is a system entity type."
      }
    };
  }
}
