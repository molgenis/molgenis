package org.molgenis.jobs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.TransactionOperations;

import java.util.concurrent.Callable;

/**
 * Template to execute jobs.
 */
class JobExecutionTemplate
{
	private static final Logger LOG = LoggerFactory.getLogger(JobExecutionTemplate.class);

	/**
	 * Executes a job in the calling thread within a transaction.
	 *
	 * @param job                   the {@link Job} to execute.
	 * @param progress              {@link Progress} to report progress to
	 * @param authentication        {@link Authentication} to run the job with
	 * @param transactionOperations TransactionOperations to use for a transactional call
	 * @param <T>                   Job result type
	 * @return the result of the job execution
	 * @throws JobExecutionException if job execution throws an exception
	 * @deprecated Create a service bean with a @Transactional annotation instead
	 */
	@Deprecated
	<T> T call(Job<T> job, Progress progress, Authentication authentication,
			TransactionOperations transactionOperations)
	{
		return runWithAuthentication(authentication, () -> tryCallInTransaction(job, progress, transactionOperations));
	}

	/**
	 * Executes a job in the calling thread.
	 *
	 * @param job            the {@link Job} to execute
	 * @param progress       {@link Progress} to report progress to
	 * @param authentication {@link Authentication} to run the job with
	 * @param <T>            Job result type
	 * @return the result of the job execution
	 * @throws JobExecutionException if job execution throws an exception
	 */
	<T> T call(Job<T> job, Progress progress, Authentication authentication)
	{
		return runWithAuthentication(authentication, () -> tryCall(job, progress));
	}

	private <T> T runWithAuthentication(Authentication authentication, Callable<T> callable)
	{
		final SecurityContext originalContext = SecurityContextHolder.getContext();
		try
		{
			SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext());
			SecurityContextHolder.getContext().setAuthentication(authentication);
			return callable.call();
		}
		catch (RuntimeException rte)
		{
			throw rte;
		}
		catch (Exception e)
		{
			throw new IllegalStateException("Lambda should only throw runtime exception", e);
		}
		finally
		{
			SecurityContextHolder.setContext(originalContext);
		}
	}

	private <T> T tryCallInTransaction(Job<T> job, Progress progress, TransactionOperations transactionOperations)
	{
		try
		{
			return transactionOperations.execute((status) -> tryCall(job, progress));
		}
		catch (TransactionException te)
		{
			LOG.error("Transaction error while running job", te);
			progress.failed(te);
			throw te;
		}
	}

	private <T> T tryCall(Job<T> job, Progress progress)
	{
		progress.start();
		try
		{
			T result = job.call(progress);
			progress.success();
			return result;
		}
		catch (Exception ex)
		{
			LOG.warn("Error executing job", ex);
			progress.failed(ex);
			throw new JobExecutionException(ex);
		}
	}

}
