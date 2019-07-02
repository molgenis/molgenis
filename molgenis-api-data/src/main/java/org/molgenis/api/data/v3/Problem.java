package org.molgenis.api.data.v3;

import org.zalando.problem.Status;

public class Problem extends CodedProblem {

  public Problem(Status status, String message, String errorCode) {
    super(null, status.getReasonPhrase(), status, message, errorCode);
  }

  @Override
  public String toString() {
    return "super="
        + super.toString()
        + "CodedProblem{"
        + "errorCode='"
        + getErrorCode()
        + '\''
        + '}';
  }
}
