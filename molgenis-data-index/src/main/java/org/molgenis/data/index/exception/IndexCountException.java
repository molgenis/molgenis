package org.molgenis.data.index.exception;

import java.util.List;

/** Thrown when an error occurs trying to create one or more indices. */
// S2166 'Classes named like "Exception" should extend "Exception" or a subclass' often gives false
// positives at dev time
@SuppressWarnings({"squid:MaximumInheritanceDepth", "squid:S2166"})
public class IndexCountException extends IndexException {

  private static final String ERROR_CODE = "IX09";

  public IndexCountException(List<String> indices) {
    this(ERROR_CODE, indices);
  }

  public IndexCountException(List<String> indices, Throwable cause) {
    this(ERROR_CODE, indices, cause);
  }

  public IndexCountException(String errorCode, List<String> indices) {
    super(errorCode, indices);
  }

  public IndexCountException(String errorCode, List<String> indices, Throwable cause) {
    super(errorCode, indices, cause);
  }
}
