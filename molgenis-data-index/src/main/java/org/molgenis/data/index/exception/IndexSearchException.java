package org.molgenis.data.index.exception;

import static java.lang.String.format;

import java.util.List;
import org.apache.commons.lang3.ArrayUtils;

/** Thrown when an error occurs trying to create one or more indices. */
// S2166 'Classes named like "Exception" should extend "Exception" or a subclass' often gives false
// positives at dev time
@SuppressWarnings({"squid:MaximumInheritanceDepth", "squid:S2166"})
public class IndexSearchException extends IndexException {

  private static final String ERROR_CODE = "IX10";
  private final transient String query;

  protected IndexSearchException(String errorCode, List<String> indices, String query) {
    super(errorCode, indices);
    this.query = query;
  }

  public IndexSearchException(List<String> indices, String query) {
    super(ERROR_CODE, indices);
    this.query = query;
  }

  public IndexSearchException(List<String> indices, String query, Throwable cause) {
    super(ERROR_CODE, indices, cause);
    this.query = query;
  }

  public String getQuery() {
    return query;
  }

  @Override
  public String getMessage() {
    return format("indices:%s, query:%s", String.join(", ", indices), query);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return ArrayUtils.addAll(super.getLocalizedMessageArguments(), query);
  }
}
