package org.molgenis.oneclickimporter.exceptions;

import org.molgenis.util.exception.CodedRuntimeException;

/** @deprecated use class that extends from {@link CodedRuntimeException} */
@Deprecated
public class UnknownFileTypeException extends Exception {
  public UnknownFileTypeException(String s) {
    super(s);
  }
}
