package org.molgenis.core.ui.style.exception;

import org.molgenis.util.exception.CodedRuntimeException;

public class DuplicateThemeIdException extends CodedRuntimeException {
  private static final String ERROR_CODE = "CU03";
  private final String themeId;

  public DuplicateThemeIdException(String themeId) {
    super(ERROR_CODE);
    this.themeId = themeId;
  }

  @Override
  public String getMessage() {
    return String.format("themeId:%s", themeId);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {themeId};
  }
}
