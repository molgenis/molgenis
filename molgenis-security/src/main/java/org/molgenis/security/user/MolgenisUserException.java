package org.molgenis.security.user;

/** @deprecated use class that extends from org.molgenis.i18n.CodedRuntimeException */
@Deprecated
public class MolgenisUserException extends RuntimeException {
  public MolgenisUserException(String message) {
    super(message);
  }

  public MolgenisUserException(Exception exception) {
    super(exception.getMessage(), exception);
  }
}
