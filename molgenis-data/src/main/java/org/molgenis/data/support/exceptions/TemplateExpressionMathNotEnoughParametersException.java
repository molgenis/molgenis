package org.molgenis.data.support.exceptions;

import org.molgenis.util.exception.CodedRuntimeException;

public class TemplateExpressionMathNotEnoughParametersException extends CodedRuntimeException {
  private static final String ERROR_CODE = "D12f";

  public TemplateExpressionMathNotEnoughParametersException() {
    super(ERROR_CODE);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[0];
  }
}
