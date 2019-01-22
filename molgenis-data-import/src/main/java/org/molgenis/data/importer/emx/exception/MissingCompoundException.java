package org.molgenis.data.importer.emx.exception;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import org.molgenis.i18n.CodedRuntimeException;

public class MissingCompoundException extends CodedRuntimeException {
  private static final String ERROR_CODE = "IMP10";
  private final String partOfAttribute;
  private final String attributeName;
  private final String entityTypeId;
  private final String emxAttributes;
  private final int rowIndex;

  public MissingCompoundException(
      String partOfAttribute,
      String attributeName,
      String entityTypeId,
      String emxAttributes,
      int rowIndex) {
    super(ERROR_CODE);
    this.partOfAttribute = requireNonNull(partOfAttribute);
    this.attributeName = requireNonNull(attributeName);
    this.entityTypeId = requireNonNull(entityTypeId);
    this.emxAttributes = requireNonNull(emxAttributes);
    this.rowIndex = requireNonNull(rowIndex);
  }

  @Override
  public String getMessage() {
    return format(
        "partOfAttribute:%s, attributeName:%s, entityTypeId:%s, emxAttributes:%s, rowIndex:%s",
        partOfAttribute, attributeName, entityTypeId, emxAttributes, rowIndex);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {partOfAttribute, attributeName, entityTypeId, emxAttributes, rowIndex};
  }
}
