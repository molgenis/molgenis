package org.molgenis.data.index.exception;

import static java.lang.String.format;

import java.util.List;
import org.molgenis.util.exception.CodedRuntimeException;

/** Exception class for exceptions that occur when doing operations on one or more indices */
// S2166 'Classes named like "Exception" should extend "Exception" or a subclass' often gives false
// positives at dev time
@SuppressWarnings({"squid:MaximumInheritanceDepth", "squid:S2166"})
public abstract class IndexException extends CodedRuntimeException {

  protected final transient List<String> indices;

  IndexException(String errorCode, String index) {
    super(errorCode);
    this.indices = List.of(index);
  }

  IndexException(String errorCode, List<String> indices) {
    super(errorCode);
    this.indices = indices;
  }

  IndexException(String errorCode, List<String> indices, Throwable cause) {
    super(errorCode, cause);
    this.indices = indices;
  }

  public List<String> getIndices() {
    return indices;
  }

  @Override
  public String getMessage() {
    return format("indices:%s", String.join(", ", indices));
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {String.join(", ", indices), indices.size()};
  }
}
