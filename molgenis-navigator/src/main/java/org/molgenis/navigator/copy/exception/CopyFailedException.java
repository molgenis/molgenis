package org.molgenis.navigator.copy.exception;

import org.molgenis.i18n.CodedRuntimeException;

// S2166 'Classes named like "Exception" should extend "Exception" or a subclass' often gives false
// positives at dev time
@SuppressWarnings({"squid:MaximumInheritanceDepth", "squid:S2166"})
public abstract class CopyFailedException extends CodedRuntimeException {

  public CopyFailedException(String errorCode) {
    super(errorCode);
  }

  public CopyFailedException(String errorCode, Throwable cause) {
    super(errorCode, cause);
  }
}
