package org.molgenis.data.elasticsearch.client;

import com.google.common.collect.ImmutableMap;
import org.molgenis.data.MolgenisDataException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.backoff.SleepingBackOffPolicy;
import org.springframework.retry.policy.ExceptionClassifierRetryPolicy;
import org.springframework.retry.policy.TimeoutRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.time.Duration;

@Configuration
public class ConnectionRetryConfig
{
	private static final Duration MAX_CONNECTION_TIMEOUT = Duration.ofDays(1);
	private static final Duration MAX_RETRY_WAIT = Duration.ofMinutes(5);
	private static final Duration INITIAL_RETRY_WAIT = Duration.ofSeconds(1);

	@Bean
	public RetryTemplate retryTemplate()
	{
		RetryTemplate retryTemplate = new RetryTemplate();
		retryTemplate.setRetryPolicy(retryPolicy());
		retryTemplate.setBackOffPolicy(backOffPolicy());
		return retryTemplate;
	}

	@Bean
	public RetryPolicy retryPolicy()
	{
		TimeoutRetryPolicy timeoutRetryPolicy = new TimeoutRetryPolicy();
		timeoutRetryPolicy.setTimeout(MAX_CONNECTION_TIMEOUT.toMillis());
		ExceptionClassifierRetryPolicy exceptionClassifierRetryPolicy = new ExceptionClassifierRetryPolicy();
		exceptionClassifierRetryPolicy.setPolicyMap(ImmutableMap.of(MolgenisDataException.class, timeoutRetryPolicy));
		return exceptionClassifierRetryPolicy;
	}

	@Bean
	public SleepingBackOffPolicy<ExponentialBackOffPolicy> backOffPolicy()
	{
		ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
		backOffPolicy.setInitialInterval(INITIAL_RETRY_WAIT.toMillis());
		backOffPolicy.setMaxInterval(MAX_RETRY_WAIT.toMillis());
		return backOffPolicy;
	}
}
