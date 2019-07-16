package org.molgenis.data;

import org.molgenis.util.exception.CodedRuntimeException;

/** @deprecated use class that extends from {@link CodedRuntimeException} */
@Deprecated
public class MolgenisInvalidFormatException extends Exception {
  private static final long serialVersionUID = 1L;

  public MolgenisInvalidFormatException(String message) {
    super(message);
  }

  public MolgenisInvalidFormatException(String message, Exception cause) {
    super(message, cause);
  }
}
