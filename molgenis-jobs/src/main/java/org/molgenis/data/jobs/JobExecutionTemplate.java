package org.molgenis.data.jobs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Template to execute molgenis jobs and keeps track of their progress.
 */
public class JobExecutionTemplate
{
	private static final Logger LOG = LoggerFactory.getLogger(JobExecutionTemplate.class);
	private TransactionTemplate transactionTemplate;

	public JobExecutionTemplate(TransactionTemplate transactionTemplate)
	{
		this.transactionTemplate = transactionTemplate;
	}

	public <Result> Result call(JobInterface<Result> job, Progress progress, Authentication authentication)
	{
		progress.start();
		try
		{
			Result result;
			if (job.isTransactional())
			{
				result = transactionTemplate
						.execute((status) -> tryRunWithAuthentication(job, authentication, progress));
			}
			else
			{
				result = tryRunWithAuthentication(job, authentication, progress);
			}
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

	private <Result> Result tryRunWithAuthentication(JobInterface<Result> job, Authentication authentication,
			Progress progress)
	{
		try
		{
			return runWithAuthentication(job, authentication, progress);
		}
		catch (Exception e)
		{
			throw new JobExecutionException(e);
		}
	}

	private <Result> Result runWithAuthentication(JobInterface<Result> job, Authentication authentication,
			Progress progress) throws Exception
	{
		SecurityContext originalContext = SecurityContextHolder.getContext();
		try
		{
			SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext());
			SecurityContextHolder.getContext().setAuthentication(authentication);
			return job.call(progress);
		}
		finally
		{
			SecurityContextHolder.setContext(originalContext);
		}
	}

}
