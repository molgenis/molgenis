package org.molgenis.data.importer.emx.exception;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import org.molgenis.i18n.CodedRuntimeException;

public class UnknownEmxAttributeException extends CodedRuntimeException {
  private static final String ERROR_CODE = "IMP19";
  private final String name;
  private final String sheet;

  public UnknownEmxAttributeException(String name, String sheet) {
    super(ERROR_CODE);
    this.name = requireNonNull(name);
    this.sheet = requireNonNull(sheet);
  }

  @Override
  public String getMessage() {
    return format("name:%s, sheet:%s", name, sheet);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {name, sheet};
  }
}
