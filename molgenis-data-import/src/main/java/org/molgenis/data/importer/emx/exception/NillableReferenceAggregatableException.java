package org.molgenis.data.importer.emx.exception;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import org.molgenis.data.meta.AttributeType;
import org.molgenis.i18n.CodedRuntimeException;

public class NillableReferenceAggregatableException extends CodedRuntimeException {
  private static final String ERROR_CODE = "IMP15";
  private final String emxEntityName;
  private final String emxName;
  private final AttributeType dataType;
  private final String emxAttributes;
  private final int rowIndex;

  public NillableReferenceAggregatableException(
      String emxEntityName,
      String emxName,
      AttributeType dataType,
      String emxAttributes,
      int rowIndex) {
    super(ERROR_CODE);
    this.emxEntityName = requireNonNull(emxEntityName);
    this.emxName = requireNonNull(emxName);
    this.dataType = requireNonNull(dataType);
    this.emxAttributes = requireNonNull(emxAttributes);
    this.rowIndex = requireNonNull(rowIndex);
  }

  @Override
  public String getMessage() {
    return format(
        "emxEntityName:%s, emxName:%s, dataType:%s, emxAttributes:%s, rowIndex:%s",
        emxEntityName, emxName, dataType, emxAttributes, rowIndex);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {emxEntityName, emxName, dataType, emxAttributes, rowIndex};
  }
}
