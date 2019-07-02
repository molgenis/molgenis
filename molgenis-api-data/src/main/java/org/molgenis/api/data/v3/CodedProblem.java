package org.molgenis.api.data.v3;

import java.net.URI;
import java.util.Map;
import javax.annotation.Nullable;
import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;

public class CodedProblem extends AbstractThrowableProblem {

  private final String errorCode;

  public CodedProblem(String errorCode) {
    this.errorCode = errorCode;
  }

  public CodedProblem(@Nullable URI type, String errorCode) {
    super(type);
    this.errorCode = errorCode;
  }

  public CodedProblem(@Nullable URI type, @Nullable String title, String errorCode) {
    super(type, title);
    this.errorCode = errorCode;
  }

  public CodedProblem(
      @Nullable URI type, @Nullable String title, @Nullable Status status, String errorCode) {
    super(type, title, status);
    this.errorCode = errorCode;
  }

  public CodedProblem(
      @Nullable URI type,
      @Nullable String title,
      @Nullable Status status,
      @Nullable String detail,
      String errorCode) {
    super(type, title, status, detail);
    this.errorCode = errorCode;
  }

  public CodedProblem(
      @Nullable URI type,
      @Nullable String title,
      @Nullable Status status,
      @Nullable String detail,
      @Nullable URI instance,
      String errorCode) {
    super(type, title, status, detail, instance);
    this.errorCode = errorCode;
  }

  public CodedProblem(
      @Nullable URI type,
      @Nullable String title,
      @Nullable Status status,
      @Nullable String detail,
      @Nullable URI instance,
      @Nullable ThrowableProblem cause,
      String errorCode) {
    super(type, title, status, detail, instance, cause);
    this.errorCode = errorCode;
  }

  public CodedProblem(
      @Nullable URI type,
      @Nullable String title,
      @Nullable Status status,
      @Nullable String detail,
      @Nullable URI instance,
      @Nullable ThrowableProblem cause,
      @Nullable Map<String, Object> parameters,
      String errorCode) {
    super(type, title, status, detail, instance, cause, parameters);
    this.errorCode = errorCode;
  }

  public String getErrorCode() {
    return errorCode;
  }
}
