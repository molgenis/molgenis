package org.molgenis.data.importer.emx.exception;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import org.molgenis.util.exception.CodedRuntimeException;

/** Thrown when filename is missing extension. */
// S2166 'Classes named like "Exception" should extend "Exception" or a subclass' often gives false
// positives at dev time
@SuppressWarnings({"squid:MaximumInheritanceDepth", "squid:S2166"})
public class NoFilenameExtensionException extends CodedRuntimeException {

  private static final String ERROR_CODE = "IMP22";
  private final String filename;

  public NoFilenameExtensionException(String filename) {
    super(ERROR_CODE);
    this.filename = requireNonNull(filename);
  }

  public NoFilenameExtensionException(String id, Throwable cause) {
    super(ERROR_CODE, cause);
    this.filename = requireNonNull(id);
  }

  @Override
  public String getMessage() {
    return format("filename:%s", filename);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {filename};
  }
}
