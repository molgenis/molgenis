package org.molgenis.data.importer.emx.exception;

import static java.util.Objects.requireNonNull;

import org.molgenis.i18n.CodedRuntimeException;

public class MissingEmxAttributeAttributeValueException extends CodedRuntimeException {
  private static final String ERROR_CODE = "IMP11";
  private final String enum_options;
  private final String name;
  private final String emxEntityName;
  private final String sheet;
  private final int rowIndex;

  public MissingEmxAttributeAttributeValueException(
      String enum_options, String name, String emxEntityName, String sheet, int rowIndex) {
    super(ERROR_CODE);
    this.enum_options = requireNonNull(enum_options);
    this.name = requireNonNull(name);
    this.emxEntityName = requireNonNull(emxEntityName);
    this.sheet = requireNonNull(sheet);
    this.rowIndex = requireNonNull(rowIndex);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {enum_options, name, emxEntityName, sheet, rowIndex};
  }
}
