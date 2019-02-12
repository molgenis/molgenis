package org.molgenis.api.permissions.exceptions;

import static java.util.Objects.requireNonNull;

import org.molgenis.i18n.CodedRuntimeException;
import org.springframework.security.acls.model.ObjectIdentity;

public class UpdatePermissionDeniedException extends CodedRuntimeException {
  private static final String ERROR_CODE = "PRM09";
  private final ObjectIdentity objectIdentity;

  public UpdatePermissionDeniedException(ObjectIdentity objectIdentity) {
    super(ERROR_CODE);
    this.objectIdentity = requireNonNull(objectIdentity);
  }

  @Override
  public String getMessage() {
    return String.format(
        "objectIdentity type:%s, objectIdentity identifier:%s",
        objectIdentity.getType(), objectIdentity.getIdentifier());
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {objectIdentity.getType(), objectIdentity.getIdentifier()};
  }
}
