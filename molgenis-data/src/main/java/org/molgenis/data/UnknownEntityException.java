package org.molgenis.data;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import javax.annotation.Nullable;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;

@SuppressWarnings({"squid:MaximumInheritanceDepth"})
public class UnknownEntityException extends UnknownDataException {
  private static final String ERROR_CODE = "D02";

  @Nullable private final transient EntityType entityType;

  // the attribute you used to look up the entity, defaults to id
  private final transient Attribute attribute;

  private final String entityTypeId;

  private final transient Object entityId;

  public UnknownEntityException(EntityType entityType, Object entityId) {
    super(ERROR_CODE);
    this.entityType = requireNonNull(entityType);
    this.entityTypeId = entityType.getId();
    this.entityId = requireNonNull(entityId);
    this.attribute = entityType.getIdAttribute();
  }

  public UnknownEntityException(EntityType entityType, Attribute attribute, Object entityId) {
    super(ERROR_CODE);
    this.entityType = requireNonNull(entityType);
    this.entityTypeId = entityType.getId();
    this.entityId = requireNonNull(entityId);
    this.attribute = requireNonNull(attribute);
  }

  public UnknownEntityException(String entityTypeId, Object entityId) {
    super(ERROR_CODE);
    entityType = null;
    this.entityTypeId = requireNonNull(entityTypeId);
    this.entityId = requireNonNull(entityId);
    this.attribute = null;
  }

  public Object getEntityId() {
    return entityId;
  }

  @Override
  public String getMessage() {
    return String.format(
        "type:%s id:%s attribute:%s",
        entityTypeId,
        entityId.toString(),
        Optional.ofNullable(attribute).map(Attribute::getName).orElse("null"));
  }

  @Override
  public String getErrorCode() {
    return entityType == null ? ERROR_CODE + "a" : ERROR_CODE;
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return entityType == null
        ? new Object[] {entityTypeId, entityId}
        : new Object[] {entityType, entityId, attribute};
  }
}
