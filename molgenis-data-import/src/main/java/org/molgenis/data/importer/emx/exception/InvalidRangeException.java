package org.molgenis.data.importer.emx.exception;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import org.molgenis.i18n.CodedRuntimeException;

public class InvalidRangeException extends CodedRuntimeException {
  private static final String ERROR_CODE = "IMP08";
  private final String emxRange;
  private final String emxName;
  private final String emxEntityName;
  private final String sheet;
  private final int rowIndex;
  private final String columnName;

  public InvalidRangeException(
      String emxRange,
      String emxName,
      String columnName,
      String emxEntityName,
      String sheet,
      int rowIndex) {
    super(ERROR_CODE);
    this.emxRange = requireNonNull(emxRange);
    this.emxName = requireNonNull(emxName);
    this.emxEntityName = requireNonNull(emxEntityName);
    this.sheet = requireNonNull(sheet);
    this.rowIndex = rowIndex;
    this.columnName = requireNonNull(columnName);
  }

  @Override
  public String getMessage() {
    return format(
        "emxRange:%s, columnName:%s, emxEntityName:%s, emxName:%s, sheet:%s, rowIndex:%s",
        emxRange, columnName, emxEntityName, emxName, sheet, rowIndex);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {emxRange, columnName, emxName, emxEntityName, sheet, rowIndex};
  }
}
