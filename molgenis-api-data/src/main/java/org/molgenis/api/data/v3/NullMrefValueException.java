package org.molgenis.api.data.v3;

import org.molgenis.data.meta.model.Attribute;
import org.molgenis.util.exception.CodedRuntimeException;

public class NullMrefValueException extends CodedRuntimeException {
  private static final String ERROR_CODE = "DAPI02";

  private final String attributeName;

  NullMrefValueException(Attribute attribute) {
    super(ERROR_CODE);
    this.attributeName = attribute.getName();
  }

  @Override
  public String getMessage() {
    return String.format("attribute:%s", attributeName);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {attributeName};
  }
}
