package org.molgenis.api.metadata.v3.exception;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.ExceptionMessageTest;

class ZeroResultsExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("api-metadata");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    org.molgenis.api.model.Query q = mock(org.molgenis.api.model.Query.class);
    when(q.toString()).thenReturn("QUERY");
    assertExceptionMessageEquals(new ZeroResultsException(q), lang, message);
  }

  static Object[][] languageMessageProvider() {
    Object[] enParams = {"en", "Query 'QUERY' didn't lead to any results."};
    return new Object[][] {enParams};
  }
}
