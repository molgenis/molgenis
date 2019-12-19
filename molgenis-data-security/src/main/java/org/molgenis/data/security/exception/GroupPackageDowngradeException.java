package org.molgenis.data.security.exception;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import org.molgenis.data.meta.model.Package;
import org.molgenis.util.exception.CodedRuntimeException;

/** Thrown when a Group Package is moved or otherwise updated to be a non-root package. */
public class GroupPackageDowngradeException extends CodedRuntimeException {

  private static final String ERROR_CODE = "DS36";
  private final transient Package aPackage;

  public GroupPackageDowngradeException(Package aPackage) {
    super(ERROR_CODE);
    this.aPackage = requireNonNull(aPackage);
  }

  @Override
  public String getMessage() {
    return format("id:%s", aPackage.getId());
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {aPackage.getId()};
  }
}
