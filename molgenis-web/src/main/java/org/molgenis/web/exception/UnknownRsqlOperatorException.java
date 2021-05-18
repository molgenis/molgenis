package org.molgenis.web.exception;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import org.molgenis.util.exception.CodedRuntimeException;

/** Thrown when an unknown RSQL operator is encountered. */
// S2166 'Classes named like "Exception" should extend "Exception" or a subclass' often gives false
// positives at dev time
@SuppressWarnings({"squid:MaximumInheritanceDepth", "squid:S2166"})
public class UnknownRsqlOperatorException extends CodedRuntimeException {

  private static final String ERROR_CODE = "WEB02";
  private final String operator;

  public UnknownRsqlOperatorException(String operator) {
    super(ERROR_CODE);
    this.operator = requireNonNull(operator);
  }

  public UnknownRsqlOperatorException(String operator, Throwable cause) {
    super(ERROR_CODE, cause);
    this.operator = requireNonNull(operator);
  }

  @Override
  public String getMessage() {
    return format("operator:%s", operator);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {operator};
  }
}
