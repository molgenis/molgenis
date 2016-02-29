package org.molgenis.data.jobs;

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
public abstract class Job implements Runnable
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

	private void runWithAuthentication() throws Exception
	{
		SecurityContext originalContext = SecurityContextHolder.getContext();
		try
		{
			SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext());
			SecurityContextHolder.getContext().setAuthentication(authentication);
			run(progress);
		}
		finally
		{
			SecurityContextHolder.setContext(originalContext);
		}
	}

	@Override
	public void run()
	{
		progress.start();
		try
		{
			transactionTemplate.execute(new TransactionCallback<Void>()
			{
				@Override
				public Void doInTransaction(TransactionStatus status)
				{
					try
					{
						runWithAuthentication();
					}
					catch (Exception e)
					{
						throw new JobExecutionException(e);
					}
					return null;
				}
			});
			progress.success();
		}
		catch (JobExecutionException ex)
		{
			Exception cause = (Exception) ex.getCause();
			LOG.warn("Error executing job", cause);
			progress.failed(cause);
		}
		catch (TransactionException te)
		{
			LOG.error("Error rolling back transaction for failed job execution", te);
			progress.failed(te);
		}
	};

	/**
	 * Executes this job. For concrete subclasses to implement.
	 * 
	 * @param progress
	 * @throws Exception
	 */
	public abstract void run(Progress progress) throws Exception;
}
