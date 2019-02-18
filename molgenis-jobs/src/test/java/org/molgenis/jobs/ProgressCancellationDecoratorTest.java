package org.molgenis.jobs;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.concurrent.CancellationException;
import org.mockito.Mock;
import org.molgenis.jobs.model.JobExecution;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ProgressCancellationDecoratorTest extends AbstractMockitoTest {
  private ProgressCancellationDecorator progressCancellationDecorator;
  @Mock private Progress delegateProgress;

  @BeforeMethod
  public void setUpBeforeMethod() {
    progressCancellationDecorator = new ProgressCancellationDecorator(delegateProgress);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void testProgressCancellationDecorator() {
    new ProgressCancellationDecorator(null);
  }

  @Test
  public void testDelegate() {
    assertEquals(progressCancellationDecorator.delegate(), delegateProgress);
  }

  @Test
  public void testStart() {
    progressCancellationDecorator.start();
    verify(delegateProgress).start();
  }

  @Test
  public void testSetProgressMax() {
    progressCancellationDecorator.setProgressMax(123);
    verify(delegateProgress).setProgressMax(123);
  }

  @Test
  public void testProgress() {
    progressCancellationDecorator.progress(123, "message");
    verify(delegateProgress).progress(123, "message");
  }

  @Test(expectedExceptions = CancellationException.class)
  public void testProgressCanceled() {
    progressCancellationDecorator.canceling();
    progressCancellationDecorator.progress(123, "message");
  }

  @Test
  public void testIncrement() {
    progressCancellationDecorator.increment(123);
    verify(delegateProgress).increment(123);
  }

  @Test
  public void testStatus() {
    progressCancellationDecorator.status("message");
    verify(delegateProgress).status("message");
  }

  @Test
  public void testFailed() {
    Throwable throwable = mock(Throwable.class);
    progressCancellationDecorator.failed("message", throwable);
    verify(delegateProgress).failed("message", throwable);
  }

  @Test
  public void testCanceled() {
    progressCancellationDecorator.canceled();
    verify(delegateProgress).canceled();
  }

  @Test
  public void testSuccess() {
    progressCancellationDecorator.success();
    verify(delegateProgress).success();
  }

  @Test
  public void testTimeRunning() {
    when(delegateProgress.timeRunning()).thenReturn(123L);
    assertEquals(progressCancellationDecorator.timeRunning(), Long.valueOf(123L));
  }

  @Test
  public void testSetResultUrl() {
    progressCancellationDecorator.setResultUrl("https://my.url.org/");
    verify(delegateProgress).setResultUrl("https://my.url.org/");
  }

  @Test
  public void testGetJobExecution() {
    JobExecution jobExecution = mock(JobExecution.class);
    when(delegateProgress.getJobExecution()).thenReturn(jobExecution);
    assertEquals(progressCancellationDecorator.getJobExecution(), jobExecution);
  }

  @Test
  public void testCanceling() {
    progressCancellationDecorator.canceling();
    verify(delegateProgress).canceling();
  }
}
