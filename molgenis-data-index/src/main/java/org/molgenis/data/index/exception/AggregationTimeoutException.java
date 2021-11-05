package org.molgenis.data.index.exception;

import static java.lang.String.format;

import java.util.List;
import net.logstash.logback.encoder.org.apache.commons.lang3.ArrayUtils;

/** Thrown when aggregate query times out */
// S2166 'Classes named like "Exception" should extend "Exception" or a subclass' often gives false
// positives at dev time
@SuppressWarnings({"squid:MaximumInheritanceDepth", "squid:S2166"})
public class AggregationTimeoutException extends IndexException {

  private static final String ERROR_CODE = "IX02";
  private final long millis;

  public AggregationTimeoutException(List<String> indexes, long millis) {
    super(ERROR_CODE, indexes);
    this.millis = millis;
  }

  @Override
  public String getMessage() {
    return super.getMessage() + format(", millis:%s", millis);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return ArrayUtils.addAll(super.getLocalizedMessageArguments(), millis);
  }

  public long getMillis() {
    return millis;
  }
}
