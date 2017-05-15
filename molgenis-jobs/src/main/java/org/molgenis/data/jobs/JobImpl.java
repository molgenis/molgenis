package org.molgenis.data.jobs;

import org.springframework.security.core.Authentication;
import org.springframework.transaction.support.TransactionOperations;

import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * Superclass for molgenis jobs that keeps track of their progress.
 * Delegates the hard work to {@link JobExecutionTemplate}
 */
public abstract class JobImpl<T> implements Callable<T>, Job<T>
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
	public T call()
	{
		return new JobExecutionTemplate(transactionOperations).call(this, progress, authentication);
	}
}
