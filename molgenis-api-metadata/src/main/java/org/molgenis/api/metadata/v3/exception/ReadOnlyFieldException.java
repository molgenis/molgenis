package org.molgenis.api.metadata.v3.exception;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import org.molgenis.util.exception.BadRequestException;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class ReadOnlyFieldException extends BadRequestException {
  private static final String ERROR_CODE = "MAPI07";
  private final String field;
  private final String entity;

  public ReadOnlyFieldException(String field, String entity) {
    super(ERROR_CODE);
    this.field = requireNonNull(field);
    this.entity = requireNonNull(entity);
  }

  @Override
  public String getMessage() {
    return format("field:%s entity:%s", field, entity);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {field, entity};
  }
}
