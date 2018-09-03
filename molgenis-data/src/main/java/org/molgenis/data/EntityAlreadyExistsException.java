package org.molgenis.data;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import javax.annotation.Nullable;

@SuppressWarnings({"squid:MaximumInheritanceDepth"})
public class EntityAlreadyExistsException extends DataAlreadyExistsException {
  private static final String ERROR_CODE = "D09";

  private final transient Entity entity;

  public EntityAlreadyExistsException(Entity entity) {
    super(ERROR_CODE);
    this.entity = requireNonNull(entity);
  }

  public EntityAlreadyExistsException(Entity entity, @Nullable Throwable cause) {
    super(ERROR_CODE, cause);
    this.entity = requireNonNull(entity);
  }

  @Override
  public String getMessage() {
    return format("type:%s id:%s", entity.getEntityType().getId(), entity.getIdValue().toString());
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {entity.getIdValue().toString(), entity.getEntityType()};
  }
}
