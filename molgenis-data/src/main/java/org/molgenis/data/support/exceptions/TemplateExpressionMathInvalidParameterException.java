package org.molgenis.data.support.exceptions;

public class TemplateExpressionMathInvalidParameterException extends RuntimeException {
  private static final String ERROR_CODE = "D12h";

  public TemplateExpressionMathInvalidParameterException() {
    super(ERROR_CODE);
  }
}
