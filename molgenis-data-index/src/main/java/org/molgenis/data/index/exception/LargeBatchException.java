package org.molgenis.data.index.exception;

import static java.lang.String.format;

import org.molgenis.util.exception.CodedRuntimeException;

/** Thrown when a search query queries a batch larger than the max batch size */
// S2166 'Classes named like "Exception" should extend "Exception" or a subclass' often gives false
// positives at dev time
@SuppressWarnings({"squid:MaximumInheritanceDepth", "squid:S2166"})
public class LargeBatchException extends CodedRuntimeException {

  private static final String ERROR_CODE = "IX12";
  private final int size;
  private final int maxSize;

  public LargeBatchException(int size, int maxSize) {
    super(ERROR_CODE);
    this.size = size;
    this.maxSize = maxSize;
  }

  @Override
  public String getMessage() {
    return format("size:%s, maxSize:%s", size, maxSize);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {size, maxSize};
  }
}
