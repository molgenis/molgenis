package org.molgenis.jobs;

import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.jobs.model.JobExecution;
import org.molgenis.util.exception.ExceptionMessageTest;

class ActiveJobExecutionDeleteForbiddenExceptionTest extends ExceptionMessageTest {

  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("jobs");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  protected void testGetLocalizedMessage(String lang, String message) {
    JobExecution jobExecution = mock(JobExecution.class);
    assertExceptionMessageEquals(
        new ActiveJobExecutionDeleteForbiddenException(jobExecution), lang, message);
  }

  static Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {"en", "Deleting an active job is not allowed."},
      {"nl", "Verwijderen van een actieve job is niet toegestaan."}
    };
  }
}
