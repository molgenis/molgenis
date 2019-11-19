package org.molgenis.api.metadata.v3.exception;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import org.molgenis.util.exception.BadRequestException;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class UnsupportedFieldException extends BadRequestException {
  private static final String ERROR_CODE = "MAPI06";
  private String field;

  public UnsupportedFieldException(String field) {
    super(ERROR_CODE);
    this.field = requireNonNull(field);
  }

  @Override
  public String getMessage() {
    return format("field:%s", field);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {field};
  }
}
