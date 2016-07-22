package org.molgenis.data.elasticsearch.reindex.job;

/**
 * Schedules {@link ReindexJob}s.
 */
public interface ReindexService
{
	/**
	 * Schedules a job to rebuild the index for all changes made in the context of a specific transaction.
	 * Does nothing if no ReindexActionJob exists for this transactionId.
	 *
	 * @param transactionId the ID of the transaction.
	 */
	void rebuildIndex(String transactionId);

	void waitForAllIndicesStable() throws InterruptedException;

	void waitForIndexToBeStableIncludingReferences(String entityName) throws InterruptedException;

	void cleanupJobExecutions();
}