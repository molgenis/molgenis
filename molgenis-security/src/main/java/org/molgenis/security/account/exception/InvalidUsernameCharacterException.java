package org.molgenis.security.account.exception;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import org.molgenis.i18n.CodedRuntimeException;

public class InvalidUsernameCharacterException extends CodedRuntimeException {
  private static final String ERROR_CODE = "SEC02";
  private final Character character;
  private String username;
  private int position;

  public InvalidUsernameCharacterException(String username, int position, char character) {
    super(ERROR_CODE);
    this.username = requireNonNull(username);
    this.position = position;
    this.character = requireNonNull(character);
  }

  @Override
  public String getMessage() {
    return format("username:%s, position:%d", username, position);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {username, position, character};
  }
}
