package org.molgenis.data.index.exception;

import java.util.List;

/** Thrown when an error occurs trying to determine if one or more indices exist. */
// S2166 'Classes named like "Exception" should extend "Exception" or a subclass' often gives false
// positives at dev time
@SuppressWarnings({"squid:MaximumInheritanceDepth", "squid:S2166"})
public class IndexExistsException extends IndexException {

  private static final String ERROR_CODE = "IX05";

  public IndexExistsException(List<String> indices) {
    super(ERROR_CODE, indices);
  }

  public IndexExistsException(List<String> indices, Throwable cause) {
    super(ERROR_CODE, indices, cause);
  }

  public IndexExistsException(String index, Throwable cause) {
    super(ERROR_CODE, List.of(index), cause);
  }
}
