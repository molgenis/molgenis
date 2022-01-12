package org.molgenis.data.index;

import org.molgenis.data.index.meta.IndexAction;
import org.molgenis.data.meta.model.EntityType;

/** Schedules {@link IndexActionService}s. */
public interface IndexActionScheduler {
  /**
   * Schedules index actions that will rebuild index for all changes made in the context of a
   * specific transaction. Does nothing if no IndexActions exist for this transactionId.
   *
   * @param transactionId the ID of the transaction.
   */
  void scheduleIndexActions(String transactionId);

  void schedule(IndexAction indexAction);

  void waitForAllIndicesStable() throws InterruptedException;

  void waitForIndexToBeStableIncludingReferences(EntityType entityType) throws InterruptedException;

  void cleanupIndexActions();
}
