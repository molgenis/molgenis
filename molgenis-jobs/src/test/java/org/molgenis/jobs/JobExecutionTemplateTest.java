package org.molgenis.jobs;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Locale;
import java.util.concurrent.CancellationException;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.test.AbstractMockitoTest;
import org.molgenis.util.exception.CodedRuntimeException;
import org.springframework.security.core.Authentication;

class JobExecutionTemplateTest extends AbstractMockitoTest {
  @Mock private Job<?> job;
  @Mock private Progress progress;
  @Mock private Authentication authentication;
  private Locale locale = Locale.ENGLISH;

  @Test
  void testCall() {
    JobExecutionContext jobExecutionContext =
        JobExecutionContext.builder().setAuthentication(authentication).setLocale(locale).build();

    new JobExecutionTemplate().call(job, progress, jobExecutionContext);
    verify(progress).start();
    verify(progress).success();
  }

  @SuppressWarnings("deprecation")
  @Test
  void testCallCancellation() throws Exception {
    JobExecutionContext jobExecutionContext =
        JobExecutionContext.builder().setAuthentication(authentication).setLocale(locale).build();
    doThrow(new CancellationException()).when(job).call(progress);

    assertThrows(
        JobExecutionException.class,
        () -> new JobExecutionTemplate().call(job, progress, jobExecutionContext));
    verify(progress).canceled();
  }

  @SuppressWarnings("deprecation")
  @Test
  void testCallException() throws Exception {
    JobExecutionContext jobExecutionContext =
        JobExecutionContext.builder().setAuthentication(authentication).setLocale(locale).build();
    String message = "MyMessage";
    Exception exception = new Exception(message);
    doThrow(exception).when(job).call(progress);

    assertThrows(
        JobExecutionException.class,
        () -> new JobExecutionTemplate().call(job, progress, jobExecutionContext));
    verify(progress).start();
    verify(progress).failed(message, exception);
  }

  @SuppressWarnings("deprecation")
  @Test
  void testCallCodedException() throws Exception {
    JobExecutionContext jobExecutionContext =
        JobExecutionContext.builder().setAuthentication(authentication).setLocale(locale).build();
    String message = "MyMessage";
    String errorCode = "M01";
    CodedRuntimeException codedRuntimeException = mock(CodedRuntimeException.class);
    when(codedRuntimeException.getLocalizedMessage()).thenReturn(message);
    when(codedRuntimeException.getErrorCode()).thenReturn(errorCode);
    doThrow(codedRuntimeException).when(job).call(progress);

    assertThrows(
        JobExecutionException.class,
        () -> new JobExecutionTemplate().call(job, progress, jobExecutionContext));
    verify(progress).start();
    verify(progress).failed("MyMessage (M01)", codedRuntimeException);
  }
}
