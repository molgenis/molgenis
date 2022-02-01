package org.molgenis.data.support.exceptions;

public class TemplateExpressionMathNotEnoughParametersException extends RuntimeException {
  private static final String ERROR_CODE = "D12f";

  public TemplateExpressionMathNotEnoughParametersException() {
    super(ERROR_CODE);
  }
}
