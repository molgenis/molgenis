package org.molgenis.dataexplorer.negotiator.exception;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import org.molgenis.util.exception.CodedRuntimeException;

/** Thrown when the negotiator does not respond with a Location header to the query POST */
// S2166 'Classes named like "Exception" should extend "Exception" or a subclass' often gives false
// positives at dev time
@SuppressWarnings({"squid:MaximumInheritanceDepth", "squid:S2166"})
public class MissingLocationException extends CodedRuntimeException {

  private static final String ERROR_CODE = "NEG01";
  private final String negotiatorURL;

  public MissingLocationException(String negotiatorURL) {
    super(ERROR_CODE);
    this.negotiatorURL = requireNonNull(negotiatorURL);
  }

  public MissingLocationException(String negotiatorURL, Throwable cause) {
    super(ERROR_CODE, cause);
    this.negotiatorURL = requireNonNull(negotiatorURL);
  }

  @Override
  public String getMessage() {
    return format("negotiatorURL: %s", negotiatorURL);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {negotiatorURL};
  }
}
