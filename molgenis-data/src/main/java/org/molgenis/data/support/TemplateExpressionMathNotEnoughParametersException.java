package org.molgenis.data.support;

import org.molgenis.util.exception.CodedRuntimeException;

public class TemplateExpressionMathNotEnoughParametersException extends CodedRuntimeException {
  private static final String ERROR_CODE = "D12f";

  TemplateExpressionMathNotEnoughParametersException() {
    super(ERROR_CODE);
  }

  @Override
  public String getMessage() {
    return "";
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {};
  }
}
