package org.molgenis.core.ui.style;

import java.io.IOException;
import org.molgenis.util.exception.CodedRuntimeException;

/** @deprecated use class that extends from {@link CodedRuntimeException} */
@Deprecated
public class MolgenisStyleException extends Exception {
  public MolgenisStyleException(String s) {
    super(s);
  }

  public MolgenisStyleException(String s, IOException e) {
    super(s, e);
  }
}
