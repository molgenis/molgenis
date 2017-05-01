package org.molgenis.data.jobs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.TransactionOperations;

import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * Superclass for molgenis jobs.
 */
public abstract class Job<Result> implements Callable<Result>
{
	private static final Logger LOG = LoggerFactory.getLogger(Job.class);
	private final Progress progress;
	private TransactionOperations transactionOperations;
	private Authentication authentication;

	/**
	 * Creates a new Job instance.
	 *
	 * @param progress              {@link Progress} instance to report job progress to
	 * @param transactionOperations A {@link TransactionOperations} to execute in. If null, the job will run without a transaction.
	 * @param authentication        The {@link Authentication} to run under
	 */
	public Job(Progress progress, TransactionOperations transactionOperations, Authentication authentication)
	{
		this.progress = Objects.requireNonNull(progress);
		this.transactionOperations = transactionOperations;
		this.authentication = Objects.requireNonNull(authentication);
	}

	@Override
	public Result call()
	{
		final SecurityContext originalContext = SecurityContextHolder.getContext();
		try
		{
			SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext());
			SecurityContextHolder.getContext().setAuthentication(authentication);
			return authenticatedCall();
		}
		finally
		{
			SecurityContextHolder.setContext(originalContext);
		}
	}

	private Result authenticatedCall()
	{
		if (transactionOperations != null)
		{
			try
			{
				return transactionOperations.execute((status) -> tryCall());
			}
			catch (TransactionException te)
			{
				LOG.error("Transaction error while running job", te);
				progress.failed(te);
				throw te;
			}

		}
		else
		{
			return tryCall();
		}
	}

	private Result tryCall() throws JobExecutionException
	{
		progress.start();
		try
		{
			Result result = call(progress);
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

	/**
	 * Executes this Job. For concrete subclasses to implement.
	 *
	 * @param progress The {@link Progress} to report job progress to
	 * @throws Exception if something goes wrong. If an exception is thrown here, the job status will be set to failed.
	 */
	public abstract Result call(Progress progress) throws Exception;
}
