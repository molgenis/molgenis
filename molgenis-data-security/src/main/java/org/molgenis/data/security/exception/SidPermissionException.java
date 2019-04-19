package org.molgenis.data.security.exception;

import static java.util.Objects.requireNonNull;

import org.molgenis.i18n.CodedRuntimeException;

public class SidPermissionException extends CodedRuntimeException {
  private static final String ERROR_CODE = "PRM08";
  private final String sids;

  public SidPermissionException(String sids) {
    super(ERROR_CODE);
    this.sids = requireNonNull(sids);
  }

  @Override
  public String getMessage() {
    return String.format("sids:%s", sids);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {sids};
  }
}
