package org.molgenis.data.index.job;

import org.molgenis.data.meta.model.EntityType;

/**
 * Schedules {@link IndexJobService}s.
 */
public interface IndexJobScheduler
{
	/**
	 * Schedules a job to rebuild the index for all changes made in the context of a specific transaction.
	 * Does nothing if no IndexActionJob exists for this transactionId.
	 *
	 * @param transactionId the ID of the transaction.
	 */
	void scheduleIndexJob(String transactionId);

	void waitForAllIndicesStable() throws InterruptedException;

	void waitForIndexToBeStableIncludingReferences(EntityType entityType) throws InterruptedException;

	void cleanupJobExecutions();
}