package org.molgenis.data.importer.emx.exception;


import org.molgenis.i18n.CodedRuntimeException;

public class MissingRootPackageException extends CodedRuntimeException {
  private static final String ERROR_CODE = "IMP14";

  public MissingRootPackageException() {
    super(ERROR_CODE);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[0];
  }
}
