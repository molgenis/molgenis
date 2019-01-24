package org.molgenis.validation;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import org.molgenis.i18n.CodedRuntimeException;

/** Exception that's thrown when a JSON Schema contains errors. */
// S2166 'Classes named like "Exception" should extend "Exception" or a subclass' often gives false
// positives at dev time
@SuppressWarnings({"squid:MaximumInheritanceDepth", "squid:S2166"})
public class InvalidJsonSchemaException extends CodedRuntimeException {
  private static final String ERROR_CODE = "V02";
  private final Throwable cause;

  public InvalidJsonSchemaException(Throwable cause) {
    super(ERROR_CODE, cause);
    this.cause = requireNonNull(cause);
  }

  @Override
  public String getMessage() {
    return format("errors: %s", cause.getMessage());
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {cause.getLocalizedMessage()};
  }
}
