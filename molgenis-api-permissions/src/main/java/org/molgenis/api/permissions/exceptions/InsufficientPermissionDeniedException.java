package org.molgenis.api.permissions.exceptions;

import static java.util.Objects.requireNonNull;

import org.molgenis.data.security.exception.PermissionDeniedException;
import org.springframework.security.acls.model.ObjectIdentity;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class InsufficientPermissionDeniedException extends PermissionDeniedException {
  private static final String ERROR_CODE = "PRM09";
  private final String requiredRole;
  private final String identifier;
  private final String type;

  public InsufficientPermissionDeniedException(ObjectIdentity objectIdentity, String requiredRole) {
    super(ERROR_CODE);
    requireNonNull(objectIdentity);
    this.identifier = objectIdentity.getIdentifier().toString();
    this.type = objectIdentity.getType();
    this.requiredRole = requireNonNull(requiredRole);
  }

  public InsufficientPermissionDeniedException(
      String type, String identifier, String requiredRole) {
    super(ERROR_CODE);
    this.type = requireNonNull(type);
    this.identifier = requireNonNull(identifier);
    this.requiredRole = requireNonNull(requiredRole);
  }

  @Override
  public String getMessage() {
    return String.format("type:%s, identifier:%s, role:%s", type, identifier, requiredRole);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {type, identifier, requiredRole};
  }
}
