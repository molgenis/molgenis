package org.molgenis.data.security.exception;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.security.EntityPermission;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class EntityPermissionDeniedException extends PermissionDeniedException {
  private static final String ERROR_CODE = "DS06";

  private final EntityPermission permission;
  private final transient EntityType entityType;
  private final transient Object entityId;

  public EntityPermissionDeniedException(EntityPermission permission, Entity entity) {
    super(ERROR_CODE);
    this.permission = permission;
    this.entityType = entity.getEntityType();
    this.entityId = entity.getIdValue();
  }

  @Override
  public String getMessage() {
    return String.format("permission:%s entityTypeId:%s entityId:%s", permission, entityType.getId(), entityId);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {permission.getName(), entityId, entityType};
  }
}
