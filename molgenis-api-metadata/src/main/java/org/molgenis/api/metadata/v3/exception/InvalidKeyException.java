package org.molgenis.api.metadata.v3.exception;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import org.molgenis.util.exception.BadRequestException;

public class InvalidKeyException extends BadRequestException {
  private final static String ERROR_CODE = "MAPI04";
  private String target;
  private String key;

  public InvalidKeyException(String target, String key) {
    super(ERROR_CODE);
    this.target = requireNonNull(target);
    this.key = requireNonNull(key);
  }

  @Override
  public String getMessage() {
    return format("target:%s key:%s", target, key);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[]{target, key};
  }
}
