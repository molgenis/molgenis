package org.molgenis.data.support.exceptions;

import org.molgenis.util.exception.CodedRuntimeException;

public class TemplateExpressionMathInvalidParameterException extends CodedRuntimeException {
  private static final String ERROR_CODE = "D12h";

  public TemplateExpressionMathInvalidParameterException() {
    super(ERROR_CODE);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[0];
  }
}
