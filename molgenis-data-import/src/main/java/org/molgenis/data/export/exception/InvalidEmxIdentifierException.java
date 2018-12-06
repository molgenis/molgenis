package org.molgenis.data.export.exception;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import org.molgenis.data.Entity;
import org.molgenis.i18n.CodedRuntimeException;

public class InvalidEmxIdentifierException extends CodedRuntimeException {
  private static final String ERROR_CODE = "EXP03";
  private final Entity entity;

  public InvalidEmxIdentifierException(Entity entity) {
    super(ERROR_CODE);
    this.entity = requireNonNull(entity);
  }

  @Override
  public String getMessage() {
    return format("Type:%s Id:%s", entity.getEntityType().getLabel(), entity.getIdValue());
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {entity.getEntityType(), entity.getLabelValue()};
  }
}
