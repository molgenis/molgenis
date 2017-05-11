package org.molgenis.data.jobs;

import org.springframework.security.core.Authentication;
import org.springframework.transaction.support.TransactionOperations;

import java.util.concurrent.Callable;

import static java.util.Objects.requireNonNull;

/**
 * Superclass for molgenis jobs that keeps track of their progress.
 * Delegates the hard work to {@link JobExecutionTemplate}
 *
 * @deprecated Do the actual work in a Service bean with @Transactional annotation
 * and configure a {@link JobFactory} instead.
 */
@Deprecated
public abstract class TransactionalJob<T> implements Callable<T>, Job<T>
{
	private final Progress progress;
	private TransactionOperations transactionOperations;
	private Authentication authentication;
	private final JobExecutionTemplate jobExecutionTemplate = new JobExecutionTemplate();

	public TransactionalJob(Progress progress, TransactionOperations transactionOperations,
			Authentication authentication)
	{
		this.progress = requireNonNull(progress);
		this.transactionOperations = requireNonNull(transactionOperations);
		this.authentication = requireNonNull(authentication);
	}

	@Override
	public T call()
	{
		return jobExecutionTemplate.call(this, progress, authentication, transactionOperations);
	}
}
