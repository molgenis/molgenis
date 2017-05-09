package org.molgenis.data.jobs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.TransactionOperations;

/**
 * Template to execute molgenis jobs.
 */
public class JobExecutionTemplate
{
	private static final Logger LOG = LoggerFactory.getLogger(JobExecutionTemplate.class);
	private TransactionOperations transactionOperations;

	/**
	 * Creates a new {@link JobExecutionTemplate}.
	 *
	 * @param transactionTemplate {@link TransactionOperations} to use for transactions, may be null.
	 */
	public JobExecutionTemplate(TransactionOperations transactionTemplate)
	{
		this.transactionOperations = transactionTemplate;
	}

	/**
	 * Executes a job in the calling thread.
	 *
	 * @param job            the {@link Job} to execute.
	 * @param progress       {@link Progress} to report progress to
	 * @param authentication {@link Authentication} to run the job with
	 * @param <Result>       type of the job execution
	 * @return the result of the job execution
	 * @throws JobExecutionException if job execution throws an exception
	 */
	public <Result> Result call(Job<Result> job, Progress progress, Authentication authentication)
	{
		final SecurityContext originalContext = SecurityContextHolder.getContext();
		try
		{
			SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext());
			SecurityContextHolder.getContext().setAuthentication(authentication);
			return authenticatedCall(job, progress);
		}
		finally
		{
			SecurityContextHolder.setContext(originalContext);
		}
	}

	private <Result> Result authenticatedCall(Job<Result> job, Progress progress)
	{
		if (transactionOperations != null)
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
		else
		{
			return tryCall(job, progress);
		}
	}

	private <Result> Result tryCall(Job<Result> job, Progress progress) throws JobExecutionException
	{
		progress.start();
		try
		{
			Result result = job.call(progress);
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
