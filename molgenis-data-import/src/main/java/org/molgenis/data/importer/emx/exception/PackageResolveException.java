package org.molgenis.data.importer.emx.exception;

import org.molgenis.util.exception.CodedRuntimeException;

public class PackageResolveException extends CodedRuntimeException {
  private static final String ERROR_CODE = "IMP16";

  public PackageResolveException() {
    super(ERROR_CODE);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[0];
  }
}
