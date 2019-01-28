package org.molgenis.data.importer.emx.exception;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import org.molgenis.data.meta.model.Attribute;
import org.molgenis.i18n.CodedRuntimeException;

public class InvalidReferenceDataTypeException extends CodedRuntimeException {
  private static final String ERROR_CODE = "IMP21";
  private final String identifier;
  private final Attribute attribute;
  private final String entityType;
  private final String sheet;
  private final int rowIndex;

  public InvalidReferenceDataTypeException(
      String identifier, Attribute attribute, String entityType, String sheet, int rowIndex) {
    super(ERROR_CODE);
    this.identifier = requireNonNull(identifier);
    this.attribute = requireNonNull(attribute);
    this.entityType = requireNonNull(entityType);
    this.sheet = requireNonNull(sheet);
    this.rowIndex = requireNonNull(rowIndex);
  }

  @Override
  public String getMessage() {
    return format(
        "Identifier:%s, attribute: %s, entityType:%s, sheet:%s, rowIndex:%s",
        identifier, attribute.getLabel(), entityType, sheet, rowIndex);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {identifier, attribute, entityType, sheet, rowIndex};
  }
}
