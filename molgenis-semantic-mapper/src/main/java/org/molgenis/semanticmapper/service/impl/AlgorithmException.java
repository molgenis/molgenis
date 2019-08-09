package org.molgenis.semanticmapper.service.impl;

import org.molgenis.util.exception.CodedRuntimeException;

/** @deprecated use class that extends from {@link CodedRuntimeException} */
@Deprecated
public class AlgorithmException extends RuntimeException {
  public AlgorithmException(String message) {
    super(message);
  }

  public AlgorithmException(Throwable cause) {
    super(cause);
  }
}
