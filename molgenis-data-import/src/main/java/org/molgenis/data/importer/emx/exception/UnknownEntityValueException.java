package org.molgenis.data.importer.emx.exception;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import org.molgenis.i18n.CodedRuntimeException;

public class UnknownEntityValueException extends CodedRuntimeException {
  private static final String ERROR_CODE = "IMP20";
  private final String sheet;
  private final String refEntityName;
  private final String emxAttribute;
  private final String entityName;
  private final int rowIndex;

  public UnknownEntityValueException(
      String refEntityName, String emxAttribute, String entityName, String sheet, int rowIndex) {
    super(ERROR_CODE);
    this.refEntityName = requireNonNull(refEntityName);
    this.emxAttribute = requireNonNull(emxAttribute);
    this.entityName = requireNonNull(entityName);
    this.rowIndex = requireNonNull(rowIndex);
    this.sheet = requireNonNull(sheet);
  }

  @Override
  public String getMessage() {
    return format(
        "refEntityName:%s, emxAttribute:%s, entityName:%s, sheet:%s, rowIndex:%s",
        refEntityName, emxAttribute, entityName, sheet, rowIndex);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {refEntityName, emxAttribute, entityName, sheet, rowIndex};
  }
}
