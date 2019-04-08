package org.molgenis.api.permissions.exceptions;

import static java.util.Objects.requireNonNull;
import static org.molgenis.api.permissions.UserRoleTools.getName;

import org.molgenis.i18n.CodedRuntimeException;
import org.springframework.security.acls.model.Sid;

public class DuplicatePermissionException extends CodedRuntimeException {
  private static final String ERROR_CODE = "PRM12";
  private final String typeId;
  private final String identifier;
  private final Sid sid;

  public DuplicatePermissionException(String typeId, String identifier, Sid sid) {
    super(ERROR_CODE);

    this.typeId = requireNonNull(typeId);
    this.identifier = requireNonNull(identifier);
    this.sid = requireNonNull(sid);
  }

  @Override
  public String getMessage() {
    return String.format("typeId:%s, identifier:%s, sid:%s", typeId, identifier, sid);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {getName(sid), identifier, typeId};
  }
}
