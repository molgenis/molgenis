package org.molgenis.jobs;

import static java.util.Objects.requireNonNull;

import org.molgenis.data.ErrorCodedDataAccessException;

public class InvalidJobExecutionTypeException extends ErrorCodedDataAccessException {
  private static final String ERROR_CODE = "JOB02";

  private final String entityTypeId;

  public InvalidJobExecutionTypeException(String entityTypeId) {
    super(ERROR_CODE);
    this.entityTypeId = requireNonNull(entityTypeId);
  }

  @Override
  public String getMessage() {
    return String.format("id:%s", entityTypeId);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {entityTypeId};
  }
}
