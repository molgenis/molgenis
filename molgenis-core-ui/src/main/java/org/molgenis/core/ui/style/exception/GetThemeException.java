package org.molgenis.core.ui.style.exception;

import org.molgenis.util.exception.CodedRuntimeException;

public class GetThemeException extends CodedRuntimeException {
  private static final String ERROR_CODE = "CU01";
  private final String themeId;

  public GetThemeException(String themeId) {
    super(ERROR_CODE);
    this.themeId = themeId;
  }

  public GetThemeException(String themeId, Throwable e) {
    super(ERROR_CODE, e);
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
