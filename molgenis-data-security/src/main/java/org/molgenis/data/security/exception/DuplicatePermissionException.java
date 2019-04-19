package org.molgenis.data.security.exception;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.security.permission.UserRoleTools.getName;

import org.molgenis.i18n.CodedRuntimeException;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;

public class DuplicatePermissionException extends CodedRuntimeException {
  private static final String ERROR_CODE = "PRM12";
  private final ObjectIdentity objectIdentity;
  private final Sid sid;

  public DuplicatePermissionException(ObjectIdentity objectIdentity, Sid sid) {
    super(ERROR_CODE);

    this.objectIdentity = requireNonNull(objectIdentity);
    this.sid = requireNonNull(sid);
  }

  @Override
  public String getMessage() {
    return String.format(
        "typeId:%s, identifier:%s, sid:%s",
        objectIdentity.getType(), objectIdentity.getIdentifier(), sid);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {getName(sid), objectIdentity.getIdentifier(), objectIdentity.getType()};
  }
}
