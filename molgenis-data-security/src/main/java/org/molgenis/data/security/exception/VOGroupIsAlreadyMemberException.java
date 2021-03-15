package org.molgenis.data.security.exception;

import org.molgenis.data.security.auth.Group;
import org.molgenis.data.security.auth.VOGroup;
import org.molgenis.util.exception.CodedRuntimeException;

public class VOGroupIsAlreadyMemberException extends CodedRuntimeException {
  private static final String ERROR_CODE = "DS38";

  private final transient VOGroup voGroup;
  private final transient Group group;

  public VOGroupIsAlreadyMemberException(VOGroup voGroup, Group group) {
    super(ERROR_CODE);
    this.voGroup = voGroup;
    this.group = group;
  }

  @Override
  public String getMessage() {
    return String.format("vo group:%s group:%s", voGroup.getName(), group.getName());
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {voGroup.getName(), group.getName()};
  }
}
