package org.molgenis.data.elasticsearch.client;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.elasticsearch.client.Client;
import org.junit.jupiter.api.Test;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.test.AbstractMockitoSpringContextTests;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.BackOffContext;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.backoff.SleepingBackOffPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = ConnectionRetryConfig.class)
class ConnectionRetryConfigTest extends AbstractMockitoSpringContextTests {
  @Autowired RetryTemplate retryTemplate;

  @Autowired SleepingBackOffPolicy<ExponentialBackOffPolicy> backOffPolicy;

  @Autowired RetryPolicy retryPolicy;

  private ExecutorService executorService = Executors.newSingleThreadExecutor();

  @Test
  void testInterruptFailingTries() throws Exception {
    Future<Client> result =
        executorService.submit(
            () -> {
              RetryCallback<Client, RuntimeException> fail =
                  c -> {
                    throw new MolgenisDataException();
                  };
              return retryTemplate.execute(fail);
            });

    result.cancel(true);
    try {
      result.get(100, TimeUnit.MILLISECONDS);
      fail("Should throw cancellation exception!");
    } catch (CancellationException ignore) {
    }
    assertTrue(result.isDone());
    assertTrue(result.isCancelled());
  }

  @Test
  void testBackOffPolicyIsExponential() {
    List<Long> sleeps = newArrayList();
    ExponentialBackOffPolicy fakeBackOff = this.backOffPolicy.withSleeper(sleeps::add);
    BackOffContext context = fakeBackOff.start(null);
    for (int i = 0; i < 11; i++) {
      fakeBackOff.backOff(context);
    }
    assertEquals(
        asList(
            1000L, 2000L, 4000L, 8000L, 16000L, 32000L, 64000L, 128000L, 256000L, 300000L, 300000L),
        sleeps);
  }

  @Test
  void testRetryPolicyInterrupted() {
    RetryContext context = retryPolicy.open(null);
    retryPolicy.registerThrowable(context, new InterruptedException("Going down"));
    assertFalse(retryPolicy.canRetry(context));
  }

  @Test
  void testRetryPolicyMolgenisDataException() {
    RetryContext context = retryPolicy.open(null);
    retryPolicy.registerThrowable(context, new MolgenisDataException("Failed to connect"));
    assertTrue(retryPolicy.canRetry(context));
  }
}
