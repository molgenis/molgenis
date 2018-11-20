package org.molgenis.jobs;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Locale;
import org.mockito.Mock;
import org.molgenis.i18n.CodedRuntimeException;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.security.core.Authentication;
import org.testng.annotations.Test;

public class JobExecutionTemplateTest extends AbstractMockitoTest {
  @Mock private Job<?> job;
  @Mock private Progress progress;
  @Mock private Authentication authentication;
  private Locale locale = Locale.ENGLISH;

  @Test
  public void testCall() {
    JobExecutionContext jobExecutionContext =
        JobExecutionContext.builder().setAuthentication(authentication).setLocale(locale).build();

    new JobExecutionTemplate().call(job, progress, jobExecutionContext);
    verify(progress).start();
    verify(progress).success();
  }

  @SuppressWarnings("deprecation")
  @Test(expectedExceptions = JobExecutionException.class)
  public void testCallException() throws Exception {
    JobExecutionContext jobExecutionContext =
        JobExecutionContext.builder().setAuthentication(authentication).setLocale(locale).build();
    String message = "MyMessage";
    Exception exception = new Exception(message);
    doThrow(exception).when(job).call(progress);

    try {
      new JobExecutionTemplate().call(job, progress, jobExecutionContext);
    } finally {
      verify(progress).start();
      verify(progress).failed(message, exception);
    }
  }

  @SuppressWarnings("deprecation")
  @Test(expectedExceptions = JobExecutionException.class)
  public void testCallCodedException() throws Exception {
    JobExecutionContext jobExecutionContext =
        JobExecutionContext.builder().setAuthentication(authentication).setLocale(locale).build();
    String message = "MyMessage";
    String errorCode = "M01";
    CodedRuntimeException codedRuntimeException = mock(CodedRuntimeException.class);
    when(codedRuntimeException.getLocalizedMessage()).thenReturn(message);
    when(codedRuntimeException.getErrorCode()).thenReturn(errorCode);
    doThrow(codedRuntimeException).when(job).call(progress);

    try {
      new JobExecutionTemplate().call(job, progress, jobExecutionContext);
    } finally {
      verify(progress).start();
      verify(progress).failed("MyMessage (M01)", codedRuntimeException);
    }
  }
}
