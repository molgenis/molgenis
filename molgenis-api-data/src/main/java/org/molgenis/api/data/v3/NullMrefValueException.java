package org.molgenis.api.data.v3;

import static java.util.Objects.requireNonNull;

import org.molgenis.data.meta.model.Attribute;
import org.molgenis.util.exception.CodedRuntimeException;

public class NullMrefValueException extends CodedRuntimeException {

  private static final String ERROR_CODE = "DAPI1";
  private final Attribute attribute;

  protected NullMrefValueException(Attribute attribute) {
    super(ERROR_CODE);
    this.attribute = requireNonNull(attribute);
  }

  @Override
  public String getMessage() {
    return String.format("attribute:%s", attribute.getName());
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {attribute.getName()};
  }
}
