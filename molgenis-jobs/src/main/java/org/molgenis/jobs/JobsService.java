package org.molgenis.jobs;

public interface JobsService {
  /**
   * Cancels the job execution of the given type and identifier.
   *
   * @param jobExecutionType job execution type
   * @param jobExecutionId job execution identifier
   * @throws org.molgenis.data.UnknownEntityTypeException if no job type exists for the given type
   * @throws org.molgenis.data.UnknownEntityException if no job exists for the given identifier
   */
  void cancel(String jobExecutionType, String jobExecutionId);
}
