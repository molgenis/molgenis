package org.molgenis.data.jobs;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Superclass for molgenis jobs that keeps track of their progress.
 */
public abstract class Job<Result> implements Callable<Result>
{
	private static final Logger LOG = LoggerFactory.getLogger(Job.class);
	private final Progress progress;
	private TransactionTemplate transactionTemplate;
	private Authentication authentication;

	public Job(Progress progress, TransactionTemplate transactionTemplate, Authentication authentication)
	{
		this.progress = progress;
		this.transactionTemplate = transactionTemplate;
		this.authentication = authentication;
	}

	private Result runWithAuthentication() throws Exception
	{
		SecurityContext originalContext = SecurityContextHolder.getContext();
		try
		{
			SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext());
			SecurityContextHolder.getContext().setAuthentication(authentication);
			return call(progress);
		}
		finally
		{
			SecurityContextHolder.setContext(originalContext);
		}
	}

	@Override
	public Result call()
	{
		progress.start();
		try
		{
			Result result = transactionTemplate.execute(new TransactionCallback<Result>()
			{
				@Override
				public Result doInTransaction(TransactionStatus status)
				{
					try
					{
						return runWithAuthentication();
					}
					catch (Exception e)
					{
						throw new JobExecutionException(e);
					}
				}
			});
			progress.success();
			return result;
		}
		catch (JobExecutionException ex)
		{
			Exception cause = (Exception) ex.getCause();
			LOG.warn("Error executing job", cause);
			progress.failed(cause);
			throw ex;
		}
		catch (TransactionException te)
		{
			LOG.error("Error rolling back transaction for failed job execution", te);
			progress.failed(te);
			throw te;
		}
		catch (Exception other)
		{
			LOG.error("Error logging job success", other);
			progress.failed(other);
			throw other;
		}
	};

	/**
	 * Executes this job. For concrete subclasses to implement.
	 * 
	 * @param progress
	 * @throws Exception
	 */
	public abstract Result call(Progress progress) throws Exception;
}
