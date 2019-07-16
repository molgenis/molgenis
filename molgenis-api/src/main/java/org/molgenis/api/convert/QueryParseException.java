package org.molgenis.api.convert;

import static java.lang.String.format;

import cz.jirutka.rsql.parser.RSQLParserException;
import org.molgenis.util.exception.CodedRuntimeException;

public class QueryParseException extends CodedRuntimeException {
  private static final String ERROR_CODE = "API03";
  private final RSQLParserException parseException;

  public QueryParseException(RSQLParserException parseException) {
    super(ERROR_CODE);

    this.parseException = parseException;
  }

  @Override
  public String getMessage() {
    return format("parseException: %s", parseException.getMessage());
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {parseException.getLocalizedMessage()};
  }
}
