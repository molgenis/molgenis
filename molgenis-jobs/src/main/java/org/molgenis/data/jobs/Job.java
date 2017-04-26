package org.molgenis.data.jobs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Objects;
import java.util.concurrent.Callable;

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
		this.progress = Objects.requireNonNull(progress);
		this.transactionTemplate = transactionTemplate;
		this.authentication = Objects.requireNonNull(authentication);
	}

	@Override
	public Result call()
	{
		if (transactionTemplate != null)
		{
			return transactionTemplate.execute((status) -> doCallInTransaction());
		}
		else
		{
			return doCallInTransaction();
		}
	}

	private Result doCallInTransaction()
	{
		progress.start();
		try
		{
			Result result = tryRunWithAuthentication();
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
	}

	private Result tryRunWithAuthentication()
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

	/**
	 * Executes this job. For concrete subclasses to implement.
	 *
	 * @param progress
	 * @throws Exception
	 */
	public abstract Result call(Progress progress) throws Exception;
}
