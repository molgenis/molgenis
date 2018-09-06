package org.molgenis.app.manager.exception;

import static java.util.Objects.requireNonNull;

import java.util.List;
import org.molgenis.i18n.CodedRuntimeException;

public class AppConfigMissingParametersException extends CodedRuntimeException {
  private static final String ERROR_CODE = "AM03";
  private final List<String> missingConfigParameters;

  public AppConfigMissingParametersException(List<String> missingConfigParameters) {
    super(ERROR_CODE);
    this.missingConfigParameters = requireNonNull(missingConfigParameters);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {missingConfigParameters};
  }

  @Override
  public String getMessage() {
    return String.format("missingConfigParameters:%s", missingConfigParameters);
  }
}
