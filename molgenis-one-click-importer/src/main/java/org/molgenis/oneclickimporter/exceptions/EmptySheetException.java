package org.molgenis.oneclickimporter.exceptions;

import org.molgenis.util.exception.CodedRuntimeException;

/** @deprecated use class that extends from {@link CodedRuntimeException} */
@Deprecated
public class EmptySheetException extends Exception {
  public EmptySheetException(String s) {
    super(s);
  }
}
