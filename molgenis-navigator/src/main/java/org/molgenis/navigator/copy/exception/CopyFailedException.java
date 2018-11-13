package org.molgenis.navigator.copy.exception;

import org.molgenis.i18n.CodedRuntimeException;

// S2166 'Classes named like "Exception" should extend "Exception" or a subclass' often gives false
// positives at dev time
@SuppressWarnings({"squid:MaximumInheritanceDepth", "squid:S2166"})
public class CopyFailedException extends CodedRuntimeException {

  private static final String ERROR_CODE = "N02";
  private final Throwable cause;

  public CopyFailedException(Throwable cause) {
    super(ERROR_CODE, cause);
    this.cause = cause;
  }

  @Override
  public String getMessage() {
    return cause.getMessage();
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {cause.getLocalizedMessage()};
  }
}
