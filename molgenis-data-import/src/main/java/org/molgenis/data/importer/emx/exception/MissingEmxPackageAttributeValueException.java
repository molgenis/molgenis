package org.molgenis.data.importer.emx.exception;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import org.molgenis.i18n.CodedRuntimeException;

public class MissingEmxPackageAttributeValueException extends CodedRuntimeException {
  private static final String ERROR_CODE = "IMP12";
  private final String name;
  private final String sheet;
  private final int rowIndex;

  public MissingEmxPackageAttributeValueException(String name, String sheet, int rowIndex) {
    super(ERROR_CODE);
    this.name = requireNonNull(name);
    this.sheet = requireNonNull(sheet);
    this.rowIndex = requireNonNull(rowIndex);
  }

  @Override
  public String getMessage() {
    return format("name:%s, sheet:%s, rowIndex:%s", name, sheet, rowIndex);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {name, sheet, rowIndex};
  }
}
