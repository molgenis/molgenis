package org.molgenis.data.index.exception;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import java.util.List;

/** Thrown when indexing a document fails. */
// S2166 'Classes named like "Exception" should extend "Exception" or a subclass' often gives false
// positives at dev time
@SuppressWarnings({"squid:MaximumInheritanceDepth", "squid:S2166"})
public class DocumentIndexException extends IndexException {

  private static final String ERROR_CODE = "IX03";
  private final String id;

  public DocumentIndexException(String index, String id) {
    super(ERROR_CODE, index);
    this.id = requireNonNull(id);
  }

  public DocumentIndexException(String index, String id, Throwable cause) {
    super(ERROR_CODE, List.of(index), cause);
    this.id = requireNonNull(id);
  }

  public String getDocumentId() {
    return id;
  }

  @Override
  public String getMessage() {
    return super.getMessage() + format(", id:%s", id);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {id, super.getLocalizedMessageArguments()[0]};
  }
}
