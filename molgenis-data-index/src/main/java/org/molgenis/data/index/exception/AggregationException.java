package org.molgenis.data.index.exception;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import java.util.List;
import org.apache.commons.lang3.ArrayUtils;

/** Thrown when an error occurs while querying aggregates. */
// S2166 'Classes named like "Exception" should extend "Exception" or a subclass' often gives false
// positives at dev time
@SuppressWarnings({"squid:MaximumInheritanceDepth", "squid:S2166"})
public class AggregationException extends IndexException {

  private static final String ERROR_CODE = "IX01";
  private final String detailMessage;

  protected AggregationException(String errorCode, List<String> indexes) {
    super(errorCode, indexes);
    this.detailMessage = null;
  }

  public AggregationException(List<String> indexes, String detailMessage) {
    super(ERROR_CODE, indexes);
    this.detailMessage = requireNonNull(detailMessage);
  }

  public AggregationException(List<String> indexes, Throwable cause) {
    super(ERROR_CODE, indexes, cause);
    this.detailMessage = null;
  }

  @Override
  public String getErrorCode() {
    if (detailMessage == null) {
      return super.getErrorCode();
    }
    return super.getErrorCode() + "A";
  }

  @Override
  public String getMessage() {
    return super.getMessage() + format(", detailMessage:%s", detailMessage);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return ArrayUtils.addAll(super.getLocalizedMessageArguments(), detailMessage);
  }
}
