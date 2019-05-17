package org.molgenis.data.security.exception;

import static java.util.Objects.requireNonNull;

import org.molgenis.i18n.ForbiddenException;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class SystemRlsModificationException extends ForbiddenException {
  private static final String ERROR_CODE = "DS34";
  private final String entityType;

  public SystemRlsModificationException(String entityType) {
    super(ERROR_CODE);

    this.entityType = requireNonNull(entityType);
  }

  @Override
  public String getMessage() {
    return String.format("entityType:%s", entityType);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {entityType};
  }
}
