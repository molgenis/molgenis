package org.molgenis.data.elasticsearch.index.job;

import org.molgenis.data.meta.model.EntityType;

/**
 * Schedules {@link IndexJob}s.
 */
public interface IndexService
{
	/**
	 * Schedules a job to rebuild the index for all changes made in the context of a specific transaction.
	 * Does nothing if no IndexActionJob exists for this transactionId.
	 *
	 * @param transactionId the ID of the transaction.
	 */
	void rebuildIndex(String transactionId);

	void waitForAllIndicesStable() throws InterruptedException;

	void waitForIndexToBeStableIncludingReferences(EntityType entityType) throws InterruptedException;

	void cleanupJobExecutions();
}