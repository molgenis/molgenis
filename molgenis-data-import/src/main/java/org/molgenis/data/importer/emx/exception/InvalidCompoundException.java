package org.molgenis.data.importer.emx.exception;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import org.molgenis.i18n.CodedRuntimeException;

public class InvalidCompoundException extends CodedRuntimeException {
  private static final String ERROR_CODE = "IMP05";
  private final String partOfAttribute;
  private final String attributeName;
  private final String entityTypeId;
  private final String sheet;
  private final int rowIndex;

  public InvalidCompoundException(
      String partOfAttribute,
      String attributeName,
      String entityTypeId,
      String sheet,
      int rowIndex) {
    super(ERROR_CODE);
    this.partOfAttribute = requireNonNull(partOfAttribute);
    this.attributeName = requireNonNull(attributeName);
    this.entityTypeId = requireNonNull(entityTypeId);
    this.sheet = requireNonNull(sheet);
    this.rowIndex = rowIndex;
  }

  @Override
  public String getMessage() {
    return format(
        "PartOfAttribute:%s, attributeName:%s, entityTypeId:%s, sheet:%s, rowIndex:%s",
        partOfAttribute, attributeName, entityTypeId, sheet, rowIndex);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {partOfAttribute, attributeName, entityTypeId, sheet, rowIndex};
  }
}
