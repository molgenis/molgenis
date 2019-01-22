package org.molgenis.data.importer.emx.exception;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import org.molgenis.data.meta.AttributeType;
import org.molgenis.i18n.CodedRuntimeException;

public class InvalidDataTypeException extends CodedRuntimeException {
  private static final String ERROR_CODE = "IMP06";
  private final String identifier;
  private final AttributeType dataType;
  private final String attributeName;
  private final String entityType;
  private final String sheet;
  private final int rowIndex;

  public InvalidDataTypeException(
      String identifier,
      AttributeType dataType,
      String attributeName,
      String entityType,
      String sheet,
      int rowIndex) {
    super(ERROR_CODE);
    this.identifier = requireNonNull(identifier);
    this.dataType = requireNonNull(dataType);
    this.attributeName = requireNonNull(attributeName);
    this.entityType = requireNonNull(entityType);
    this.sheet = requireNonNull(sheet);
    this.rowIndex = requireNonNull(rowIndex);
  }

  @Override
  public String getMessage() {
    return format(
        "Identifier:%s, dataType:%s, attributeName:%s, entityType:%s, sheet:%s, rowIndex:%s",
        identifier, dataType, attributeName, entityType, sheet, rowIndex);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {identifier, dataType, attributeName, entityType, sheet, rowIndex};
  }
}
