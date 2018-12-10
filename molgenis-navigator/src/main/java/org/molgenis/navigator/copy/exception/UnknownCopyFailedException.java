package org.molgenis.navigator.copy.exception;

// S2166 'Classes named like "Exception" should extend "Exception" or a subclass' often gives false
// positives at dev time
@SuppressWarnings({"squid:MaximumInheritanceDepth", "squid:S2166"})
public class UnknownCopyFailedException extends CopyFailedException {

  private static final String ERROR_CODE = "NAV03";

  public UnknownCopyFailedException(Throwable cause) {
    super(ERROR_CODE, cause);
  }

  @Override
  public String getMessage() {
    return null;
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[0];
  }
}
