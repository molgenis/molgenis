package org.molgenis.data.jobs;

import org.springframework.security.core.Authentication;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * Superclass for molgenis jobs that keeps track of their progress.
 * Delegates the hard work to {@link JobExecutionTemplate}
 */
public abstract class Job<Result> implements Callable<Result>, JobInterface<Result>
{
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
		return new JobExecutionTemplate(transactionTemplate).call(this, progress, authentication);
	}

	@Override
	public boolean isTransactional()
	{
		return transactionTemplate != null;
	}

	/**
	 * Executes this job. For concrete subclasses to implement.
	 *
	 * @param progress
	 * @throws Exception
	 */
	public abstract Result call(Progress progress) throws Exception;
}
