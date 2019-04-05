package org.molgenis.api.permissions.exceptions;

import static java.util.Objects.requireNonNull;
import static org.molgenis.api.permissions.SidConversionTools.getName;

import org.molgenis.i18n.CodedRuntimeException;
import org.springframework.security.acls.model.Sid;

public class UnknownAceException extends CodedRuntimeException {
  private static final String ERROR_CODE = "PRM04";
  private final String typeId;
  private final String identifier;
  private final Sid sid;
  private final String operation;

  public UnknownAceException(String typeId, String identifier, Sid sid, String operation) {
    super(ERROR_CODE);

    this.typeId = requireNonNull(typeId);
    this.identifier = requireNonNull(identifier);
    this.sid = requireNonNull(sid);
    this.operation = requireNonNull(operation);
  }

  @Override
  public String getMessage() {
    return String.format(
        "typeId:%s, identifier:%s, sid:%s, operation:%s", typeId, identifier, sid, operation);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {typeId, identifier, getName(sid), operation};
  }
}
