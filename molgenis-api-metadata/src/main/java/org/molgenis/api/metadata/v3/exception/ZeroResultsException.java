package org.molgenis.api.metadata.v3.exception;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import org.molgenis.api.model.Query;
import org.molgenis.util.exception.BadRequestException;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class ZeroResultsException extends BadRequestException {

  private static final String ERROR_CODE = "MAPI01";
  private final Query query;

  public ZeroResultsException(Query q) {
    super(ERROR_CODE);
    this.query = requireNonNull(q);
  }

  @Override
  public String getMessage() {
    return format("query:%s", query);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {query};
  }
}
