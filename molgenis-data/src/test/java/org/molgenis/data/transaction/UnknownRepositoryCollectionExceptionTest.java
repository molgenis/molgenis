package org.molgenis.data.transaction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.data.UnknownRepositoryCollectionException;
import org.molgenis.util.exception.ExceptionMessageTest;

public class UnknownRepositoryCollectionExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("data");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    assertExceptionMessageEquals(
        new UnknownRepositoryCollectionException("MyRepositoryCollection"), lang, message);
  }

  static Object[][] languageMessageProvider() {
    Object[] enParams = {"en", "Unknown repository collection 'MyRepositoryCollection'."};
    Object[] nlParams = {"nl", "Onbekende opslagplaats verzameling 'MyRepositoryCollection'."};
    return new Object[][] {enParams, nlParams};
  }
}
