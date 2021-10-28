package org.molgenis.data.index.queue;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.index.meta.IndexActionMetadata.IndexStatus.CANCELED;
import static org.molgenis.data.index.meta.IndexActionMetadata.IndexStatus.FAILED;
import static org.molgenis.data.index.meta.IndexActionMetadata.IndexStatus.FINISHED;
import static org.molgenis.data.index.meta.IndexActionMetadata.IndexStatus.PENDING;
import static org.molgenis.data.index.meta.IndexActionMetadata.IndexStatus.STARTED;

import java.util.Optional;
import org.molgenis.data.index.job.IndexJobService;
import org.molgenis.data.index.meta.IndexAction;
import org.molgenis.data.index.meta.IndexActionMetadata;
import org.molgenis.data.index.meta.IndexActionMetadata.IndexStatus;

public class RunnableIndexAction implements Runnable {

  private final IndexAction indexAction;
  private final IndexJobService indexJobService;
  private volatile IndexActionMetadata.IndexStatus status;

  public RunnableIndexAction(IndexAction indexAction, IndexJobService indexJobService) {
    this.indexAction = requireNonNull(indexAction);
    this.indexJobService = requireNonNull(indexJobService);
    this.status = PENDING;
  }

  @Override
  public void run() {
    if (getStatus() == CANCELED) {
      return;
    }
    setStatus(STARTED);
    try {
      var success = indexJobService.performAction(indexAction);
      setStatus(success ? FINISHED : FAILED);
    } catch (Exception e) {
      setStatus(FAILED);
      throw e;
    }
  }

  public synchronized void setStatus(IndexStatus status) {
    this.status = status;
    indexJobService.updateIndexActionStatus(indexAction, status);
  }

  public synchronized IndexStatus getStatus() {
    return status;
  }

  /**
   * Checks if the work done by this action is contained in the work done by the other action.
   *
   * @param other the other action
   * @return true if the work is contained, false otherwise
   */
  public boolean isContainedBy(RunnableIndexAction other) {
    return other.concerns(indexAction.getEntityTypeId())
        && Optional.ofNullable(other.indexAction.getEntityId())
            .map(singleRowId -> singleRowId.equals(other.indexAction.getEntityId()))
            .orElse(true);
  }

  /**
   * Checks if the work done by this action is larger than but contains the work by the other
   * action.
   *
   * @param other the other action
   * @return true if the work contains the other action, false otherwise
   */
  public boolean contains(RunnableIndexAction other) {
    return other.isContainedBy(this);
  }

  @Override
  public String toString() {
    return indexAction.toString();
  }

  public boolean concerns(String entityTypeId) {
    return indexAction.getEntityTypeId().equals(entityTypeId);
  }
}