package org.molgenis.data.importer.emx.exception;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import org.molgenis.i18n.CodedRuntimeException;

public class InconsistentPackageStructureException extends CodedRuntimeException {
  private static final String ERROR_CODE = "IMP04";
  private final String name;
  private final String parentName;
  private final String sheet;
  private final int rowIndex;

  public InconsistentPackageStructureException(
      String name, String parentName, String sheet, int rowIndex) {
    super(ERROR_CODE);
    this.name = requireNonNull(name);
    this.parentName = requireNonNull(parentName);
    this.sheet = requireNonNull(sheet);
    this.rowIndex = requireNonNull(rowIndex);
  }

  @Override
  public String getMessage() {
    return format(
        "Name:%s, parentName:%s, sheet:%s, rowIndex:%s", name, parentName, sheet, rowIndex);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {name, parentName, sheet, rowIndex};
  }
}
