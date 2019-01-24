package org.molgenis.data.security.exception;

import org.molgenis.i18n.BadRequestException;

public class GroupNameNotAvailableException extends BadRequestException {
  private static final String ERROR_CODE = "DS16";

  private final String groupName;

  public GroupNameNotAvailableException(String groupName) {
    super(ERROR_CODE);
    this.groupName = groupName;
  }

  @Override
  public String getMessage() {
    return String.format("groupName:%s", groupName);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {groupName};
  }
}
