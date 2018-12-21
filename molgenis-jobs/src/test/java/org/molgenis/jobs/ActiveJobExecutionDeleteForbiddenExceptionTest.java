package org.molgenis.jobs;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.*;

import org.molgenis.i18n.test.exception.ExceptionMessageTest;
import org.molgenis.jobs.model.JobExecution;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ActiveJobExecutionDeleteForbiddenExceptionTest extends ExceptionMessageTest {

  @BeforeMethod
  public void setUp() {
    messageSource.addMolgenisNamespaces("jobs");
  }

  @Test(dataProvider = "languageMessageProvider")
  @Override
  public void testGetLocalizedMessage(String lang, String message) {
    JobExecution jobExecution = mock(JobExecution.class);
    assertExceptionMessageEquals(
        new ActiveJobExecutionDeleteForbiddenException(jobExecution), lang, message);
  }

  @DataProvider(name = "languageMessageProvider")
  @Override
  public Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {"en", "Deleting an active job is not allowed."},
      {"nl", "Verwijderen van een actieve job is niet toegestaan."}
    };
  }
}
