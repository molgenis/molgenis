package org.molgenis.data.index.exception;

import java.util.List;

/** Thrown when an index already exists. */
// S2166 'Classes named like "Exception" should extend "Exception" or a subclass' often gives false
// positives at dev time
@SuppressWarnings({"squid:MaximumInheritanceDepth", "squid:S2166"})
public class IndexAlreadyExistsException extends IndexException {

  private static final String ERROR_CODE = "IX15";

  public IndexAlreadyExistsException(String index) {
    super(ERROR_CODE, List.of(index));
  }

  public IndexAlreadyExistsException(String index, Throwable cause) {
    super(ERROR_CODE, List.of(index), cause);
  }
}
