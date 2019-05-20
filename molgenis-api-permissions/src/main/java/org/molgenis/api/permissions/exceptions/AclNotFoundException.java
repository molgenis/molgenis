package org.molgenis.api.permissions.exceptions;

import static java.util.Objects.requireNonNull;

import org.molgenis.i18n.CodedRuntimeException;

public class AclNotFoundException extends CodedRuntimeException {
  private static final String ERROR_CODE = "PRM03";

  private final String typeId;

  public AclNotFoundException(String typeId) {
    super(ERROR_CODE);
    this.typeId = requireNonNull(typeId);
  }

  @Override
  public String getMessage() {
    return String.format("typeId:%s", typeId);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {typeId};
  }
}
