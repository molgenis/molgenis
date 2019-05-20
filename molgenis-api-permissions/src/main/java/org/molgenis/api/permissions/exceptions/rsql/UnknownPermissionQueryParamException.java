package org.molgenis.api.permissions.exceptions.rsql;

import static java.util.Objects.requireNonNull;

import org.molgenis.i18n.BadRequestException;

public class UnknownPermissionQueryParamException extends BadRequestException {
  private static final String ERROR_CODE = "PRM01";

  private final String key;

  public UnknownPermissionQueryParamException(String key) {
    super(ERROR_CODE);
    this.key = requireNonNull(key);
  }

  @Override
  public String getMessage() {
    return String.format("key:%s", key);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {key};
  }
}
