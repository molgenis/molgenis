package org.molgenis.security.exception;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import org.molgenis.util.exception.CodedRuntimeException;

/**
 * Thrown when a user cannot be registered using OpenID Connect because a user with the same
 * username already exists but has a different email address.
 */
// S2166 'Classes named like "Exception" should extend "Exception" or a subclass' often gives false
// positives at dev time
@SuppressWarnings({"squid:MaximumInheritanceDepth", "squid:S2166"})
public class UserHasDifferentEmailAddressException extends CodedRuntimeException {

  private static final String ERROR_CODE = "SEC05";
  private final String username;
  private final String desiredEmailAddress;

  public UserHasDifferentEmailAddressException(String username, String desiredEmailAddress) {
    super(ERROR_CODE);
    this.username = requireNonNull(username);
    this.desiredEmailAddress = requireNonNull(desiredEmailAddress);
  }

  @Override
  public String getMessage() {
    return format("username:%s, desiredEmailAddress:%s", username, desiredEmailAddress);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {username, desiredEmailAddress};
  }
}
