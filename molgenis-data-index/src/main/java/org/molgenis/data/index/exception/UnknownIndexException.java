package org.molgenis.data.index.exception;

import java.util.List;

/** Thrown when an index does not exist. */
// S2166 'Classes named like "Exception" should extend "Exception" or a subclass' often gives false
// positives at dev time
@SuppressWarnings({"squid:MaximumInheritanceDepth", "squid:S2166"})
public class UnknownIndexException extends IndexException {

  private static final String ERROR_CODE = "IX16";

  public UnknownIndexException(String index) {
    this(List.of(index));
  }

  public UnknownIndexException(List<String> indices) {
    super(ERROR_CODE, indices);
  }

  public UnknownIndexException(String index, Throwable cause) {
    this(List.of(index), cause);
  }

  public UnknownIndexException(List<String> indices, Throwable cause) {
    super(ERROR_CODE, indices, cause);
  }
}
