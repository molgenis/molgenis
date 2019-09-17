package org.molgenis.jobs;

import static java.lang.Long.valueOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.CancellationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.jobs.model.JobExecution;
import org.molgenis.test.AbstractMockitoTest;

class ProgressCancellationDecoratorTest extends AbstractMockitoTest {
  private ProgressCancellationDecorator progressCancellationDecorator;
  @Mock private Progress delegateProgress;

  @BeforeEach
  void setUpBeforeMethod() {
    progressCancellationDecorator = new ProgressCancellationDecorator(delegateProgress);
  }

  @Test
  void testProgressCancellationDecorator() {
    assertThrows(NullPointerException.class, () -> new ProgressCancellationDecorator(null));
  }

  @Test
  void testDelegate() {
    assertEquals(delegateProgress, progressCancellationDecorator.delegate());
  }

  @Test
  void testStart() {
    progressCancellationDecorator.start();
    verify(delegateProgress).start();
  }

  @Test
  void testSetProgressMax() {
    progressCancellationDecorator.setProgressMax(123);
    verify(delegateProgress).setProgressMax(123);
  }

  @Test
  void testProgress() {
    progressCancellationDecorator.progress(123, "message");
    verify(delegateProgress).progress(123, "message");
  }

  @Test
  void testProgressCanceled() {
    progressCancellationDecorator.canceling();
    assertThrows(
        CancellationException.class, () -> progressCancellationDecorator.progress(123, "message"));
  }

  @Test
  void testIncrement() {
    progressCancellationDecorator.increment(123);
    verify(delegateProgress).increment(123);
  }

  @Test
  void testStatus() {
    progressCancellationDecorator.status("message");
    verify(delegateProgress).status("message");
  }

  @Test
  void testFailed() {
    Throwable throwable = mock(Throwable.class);
    progressCancellationDecorator.failed("message", throwable);
    verify(delegateProgress).failed("message", throwable);
  }

  @Test
  void testCanceled() {
    progressCancellationDecorator.canceled();
    verify(delegateProgress).canceled();
  }

  @Test
  void testSuccess() {
    progressCancellationDecorator.success();
    verify(delegateProgress).success();
  }

  @Test
  void testTimeRunning() {
    when(delegateProgress.timeRunning()).thenReturn(123L);
    assertEquals(valueOf(123L), progressCancellationDecorator.timeRunning());
  }

  @Test
  void testSetResultUrl() {
    progressCancellationDecorator.setResultUrl("https://my.url.org/");
    verify(delegateProgress).setResultUrl("https://my.url.org/");
  }

  @Test
  void testGetJobExecution() {
    JobExecution jobExecution = mock(JobExecution.class);
    when(delegateProgress.getJobExecution()).thenReturn(jobExecution);
    assertEquals(jobExecution, progressCancellationDecorator.getJobExecution());
  }

  @Test
  void testCanceling() {
    progressCancellationDecorator.canceling();
    verify(delegateProgress).canceling();
  }
}
