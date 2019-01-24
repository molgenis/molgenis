package org.molgenis.navigator.copy.exception;

/** Exception that's thrown when a Package is copied into itself or its children. */
// S2166 'Classes named like "Exception" should extend "Exception" or a subclass' often gives false
// positives at dev time
@SuppressWarnings({"squid:MaximumInheritanceDepth", "squid:S2166"})
public class RecursiveCopyException extends CopyFailedException {

  private static final String ERROR_CODE = "NAV02";

  public RecursiveCopyException() {
    super(ERROR_CODE);
  }

  @Override
  public String getMessage() {
    return "The target package is a subpackage of the package being copied";
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[0];
  }
}
