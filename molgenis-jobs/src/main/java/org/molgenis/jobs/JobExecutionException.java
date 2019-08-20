package org.molgenis.jobs;

import org.molgenis.util.exception.CodedRuntimeException;

/** @deprecated use class that extends from {@link CodedRuntimeException} */
@Deprecated
public class JobExecutionException extends RuntimeException {
  /** */
  private static final long serialVersionUID = 1L;

  public JobExecutionException(Exception cause) {
    super(cause);
  }
}
