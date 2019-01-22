package org.molgenis.data.importer.emx.exception;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import org.molgenis.i18n.CodedRuntimeException;

public class InvalidRangeException extends CodedRuntimeException {
  private static final String ERROR_CODE = "IMP08";
  private final String emxRangeMin;
  private final String emxName;
  private final String emxEntityName;
  private final String sheet;
  private final int rowIndex;
  private final String columnName;

  public InvalidRangeException(
      String emxRangeMin,
      String emxName,
      String columnName,
      String emxEntityName,
      String sheet,
      int rowIndex) {
    super(ERROR_CODE);
    this.emxRangeMin = requireNonNull(emxRangeMin);
    this.emxName = requireNonNull(emxName);
    this.emxEntityName = requireNonNull(emxEntityName);
    this.sheet = requireNonNull(sheet);
    this.rowIndex = requireNonNull(rowIndex);
    this.columnName = requireNonNull(columnName);
  }

  @Override
  public String getMessage() {
    return format(
        "emxRangeMin:%s, columnName:%s, emxEntityName:%s, emxName:%s, sheet:%s, rowIndex:%s",
        emxRangeMin, columnName, emxEntityName, emxName, sheet, rowIndex);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {emxRangeMin, columnName, emxName, emxEntityName, sheet, rowIndex};
  }
}
