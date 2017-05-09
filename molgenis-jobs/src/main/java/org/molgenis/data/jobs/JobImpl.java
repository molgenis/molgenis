package org.molgenis.data.jobs;

import org.springframework.security.core.Authentication;
import org.springframework.transaction.support.TransactionOperations;

import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * Superclass for molgenis jobs that keeps track of their progress.
 * Delegates the hard work to {@link JobExecutionTemplate}
 */
public abstract class JobImpl<Result> implements Callable<Result>, Job<Result>
{
	private final Progress progress;
	private TransactionOperations transactionOperations;
	private Authentication authentication;

	public JobImpl(Progress progress, TransactionOperations transactionOperations, Authentication authentication)
	{
		this.progress = Objects.requireNonNull(progress);
		this.transactionOperations = transactionOperations;
		this.authentication = Objects.requireNonNull(authentication);
	}

	@Override
	public Result call()
	{
		return new JobExecutionTemplate(transactionOperations).call(this, progress, authentication);
	}

	@Override
	public boolean isTransactional()
	{
		return transactionOperations != null;
	}

	/**
	 * Executes this JobImpl. For concrete subclasses to implement.
	 *
	 * @param progress The {@link Progress} to report job progress to
	 * @throws Exception if something goes wrong. If an exception is thrown here, the job status will be set to failed.
	 */
	public abstract Result call(Progress progress) throws Exception;
}
