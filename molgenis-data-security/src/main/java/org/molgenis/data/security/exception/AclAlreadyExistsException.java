package org.molgenis.data.security.exception;

import static java.util.Objects.requireNonNull;

import org.molgenis.data.DataAlreadyExistsException;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class AclAlreadyExistsException extends DataAlreadyExistsException {
  private static final String ERROR_CODE = "DS35";

  private final String typeId;
  private final String id;

  public AclAlreadyExistsException(String typeId, String id) {
    super(ERROR_CODE);
    this.typeId = requireNonNull(typeId);
    this.id = requireNonNull(id);
  }

  @Override
  public String getMessage() {
    return String.format("typeId:%s, id:%s", typeId, id);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {typeId, id};
  }
}
