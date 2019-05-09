package org.molgenis.data.security.exception;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.security.permission.UserRoleTools.getName;

import org.molgenis.data.UnknownDataException;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;

public class UnknownAceException extends UnknownDataException {
  private static final String ERROR_CODE = "DS31";
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
