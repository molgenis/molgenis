package org.molgenis.data;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/** Thrown when a Tag is requested that doesn't exist */
// S2166 'Classes named like "Exception" should extend "Exception" or a subclass' often gives false
// positives at dev time
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class UnknownTagException extends UnknownDataException {
  private static final String ERROR_CODE = "D14";

  private final String id;

  public UnknownTagException(String id) {
    super(ERROR_CODE);
    this.id = requireNonNull(id);
  }

  @Override
  public String getMessage() {
    return format("id:%s", id);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {id};
  }
}
