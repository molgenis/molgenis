package org.molgenis.api.permissions.exceptions.rsql;

import static java.util.Objects.requireNonNull;

import cz.jirutka.rsql.parser.RSQLParserException;
import org.molgenis.i18n.BadRequestException;

public class PermissionQueryParseException extends BadRequestException {
  private static final String ERROR_CODE = "PRM07";

  private final RSQLParserException cause;

  public PermissionQueryParseException(RSQLParserException cause) {
    super(ERROR_CODE, cause);
    this.cause = requireNonNull(cause);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {cause.getLocalizedMessage()};
  }
}
