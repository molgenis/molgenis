package org.molgenis.data.index.exception;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import java.util.List;

/** Thrown when explain request fails */
// S2166 'Classes named like "Exception" should extend "Exception" or a subclass' often gives false
// positives at dev time
@SuppressWarnings({"squid:MaximumInheritanceDepth", "squid:S2166"})
public class ExplainException extends IndexException {

  private static final String ERROR_CODE = "IX11";
  private final String id;
  private final transient String query;

  public ExplainException(String index, String id, String query) {
    super(ERROR_CODE, index);
    this.id = requireNonNull(id);
    this.query = requireNonNull(query);
  }

  public ExplainException(Throwable cause, String index, String id, String query) {
    super(ERROR_CODE, List.of(index), cause);
    this.id = requireNonNull(id);
    this.query = requireNonNull(query);
  }

  public String getQuery() {
    return query;
  }

  @Override
  public String getMessage() {
    return super.getMessage() + format(", id:%s, query:%s", id, query);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {id, super.getLocalizedMessageArguments()[0], query};
  }
}
