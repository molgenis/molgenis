package org.molgenis.data.index.job;

import org.molgenis.data.index.meta.IndexAction;
import org.molgenis.data.meta.model.EntityType;

//TODO rename
/** Schedules {@link IndexJobService}s. */
public interface IndexJobScheduler {
  /**
   * TODO update docs
   * Schedules a job to rebuild the index for all changes made in the context of a specific
   * transaction. Does nothing if no IndexActionJob exists for this transactionId.
   *
   * @param transactionId the ID of the transaction.
   */
  void scheduleIndexActions(String transactionId);

  void schedule(IndexAction indexAction);

  void waitForAllIndicesStable() throws InterruptedException;

  void waitForIndexToBeStableIncludingReferences(EntityType entityType) throws InterruptedException;

  void cleanupJobExecutions();
}
