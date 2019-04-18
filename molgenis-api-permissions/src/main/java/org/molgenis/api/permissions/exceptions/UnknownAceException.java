package org.molgenis.api.permissions.exceptions;

import static java.util.Objects.requireNonNull;
import static org.molgenis.api.permissions.UserRoleTools.getName;

import org.molgenis.i18n.CodedRuntimeException;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;

public class UnknownAceException extends CodedRuntimeException {
  private static final String ERROR_CODE = "PRM04";
  private final ObjectIdentity objectIdentity;
  private final Sid sid;
  private final String operation;

  public UnknownAceException(ObjectIdentity objectIdentity, Sid sid, String operation) {
    super(ERROR_CODE);

    this.objectIdentity = requireNonNull(objectIdentity);
    this.sid = requireNonNull(sid);
    this.operation = requireNonNull(operation);
  }

  @Override
  public String getMessage() {
    return String.format(
        "typeId:%s, identifier:%s, sid:%s, operation:%s",
        objectIdentity.getType(), objectIdentity.getIdentifier(), sid, operation);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {
      objectIdentity.getType(), objectIdentity.getIdentifier(), getName(sid), operation
    };
  }
}
