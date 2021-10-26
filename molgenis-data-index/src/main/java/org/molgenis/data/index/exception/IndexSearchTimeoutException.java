package org.molgenis.data.index.exception;

import static java.lang.String.format;

import java.util.List;
import org.apache.commons.lang3.ArrayUtils;

/** Thrown when an error occurs trying to create one or more indices. */
// S2166 'Classes named like "Exception" should extend "Exception" or a subclass' often gives false
// positives at dev time
@SuppressWarnings({"squid:MaximumInheritanceDepth", "squid:S2166"})
public class IndexSearchTimeoutException extends IndexSearchException {

  private static final String ERROR_CODE = "IX13";
  private final long millis;

  public IndexSearchTimeoutException(List<String> indices, String query, long millis) {
    super(ERROR_CODE, indices, query);
    this.millis = millis;
  }

  public long getMillis() {
    return millis;
  }

  @Override
  public String getMessage() {
    return super.getMessage() + format(", millis:%s", millis);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return ArrayUtils.addAll(super.getLocalizedMessageArguments(), millis);
  }
}
