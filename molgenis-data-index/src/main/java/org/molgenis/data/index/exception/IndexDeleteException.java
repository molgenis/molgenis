package org.molgenis.data.index.exception;

import java.util.List;

/** Thrown when an error occurs trying to delete one or more indices. */
// S2166 'Classes named like "Exception" should extend "Exception" or a subclass' often gives false
// positives at dev time
@SuppressWarnings({"squid:MaximumInheritanceDepth", "squid:S2166"})
public class IndexDeleteException extends IndexException {

  private static final String ERROR_CODE = "IX06";

  public IndexDeleteException(List<String> indices) {
    super(ERROR_CODE, indices);
  }

  public IndexDeleteException(List<String> indices, Throwable cause) {
    super(ERROR_CODE, indices, cause);
  }
}
