package org.molgenis.data.importer.emx.exception;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import org.molgenis.data.meta.model.Attribute;
import org.molgenis.i18n.CodedRuntimeException;

public class InvalidDataTypeException extends CodedRuntimeException {
  private static final String ERROR_CODE = "IMP06";
  private final String identifier;
  private final Attribute attr;
  private final String entityType;
  private final String sheet;
  private final int rowIndex;

  public InvalidDataTypeException(
      String identifier, Attribute attr, String entityType, String sheet, int rowIndex) {
    super(ERROR_CODE);
    this.identifier = requireNonNull(identifier);
    this.attr = requireNonNull(attr);
    this.entityType = requireNonNull(entityType);
    this.sheet = requireNonNull(sheet);
    this.rowIndex = requireNonNull(rowIndex);
  }

  @Override
  public String getMessage() {
    return format(
        "Identifier:%s, dataType:%s, attributeName:%s, entityType:%s, sheet:%s, rowIndex:%s",
        identifier, attr.getDataType(), attr.getName(), entityType, sheet, rowIndex);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {
      identifier, attr.getDataType(), attr.getName(), entityType, sheet, rowIndex
    };
  }
}
