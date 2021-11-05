package org.molgenis.data.index.exception;

import java.util.List;

/** Thrown when an error occurs trying to create one or more indices. */
// S2166 'Classes named like "Exception" should extend "Exception" or a subclass' often gives false
// positives at dev time
@SuppressWarnings({"squid:MaximumInheritanceDepth", "squid:S2166"})
public class IndexCreateException extends IndexException {

  private static final String ERROR_CODE = "IX08";

  public IndexCreateException(String index) {
    super(ERROR_CODE, index);
  }

  public IndexCreateException(String index, Throwable cause) {
    super(ERROR_CODE, List.of(index), cause);
  }
}
