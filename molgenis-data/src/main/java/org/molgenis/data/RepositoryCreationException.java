package org.molgenis.data;

import static java.util.Objects.requireNonNull;

import org.molgenis.data.meta.model.EntityType;
import org.molgenis.i18n.CodedRuntimeException;

public class RepositoryCreationException extends CodedRuntimeException {
  private static final String ERROR_CODE = "D08";

  private final transient EntityType entityType;

  public RepositoryCreationException(EntityType entityType) {
    super(ERROR_CODE);
    this.entityType = requireNonNull(entityType);
  }

  @Override
  public String getMessage() {
    return String.format("entityTypeId:%s", entityType.getId());
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {entityType};
  }
}
