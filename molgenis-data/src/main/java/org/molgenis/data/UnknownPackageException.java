package org.molgenis.data;

import static java.util.Objects.requireNonNull;

@SuppressWarnings({"squid:MaximumInheritanceDepth"})
public class UnknownPackageException extends UnknownDataException {
  private static final String ERROR_CODE = "D13";

  private final String packageName;

  public UnknownPackageException(String packageName) {
    super(ERROR_CODE);
    this.packageName = requireNonNull(packageName);
  }

  public Object getPackageName() {
    return packageName;
  }

  @Override
  public String getMessage() {
    return String.format("package:%s", packageName);
  }

  @Override
  public String getErrorCode() {
    return ERROR_CODE;
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {packageName};
  }
}
