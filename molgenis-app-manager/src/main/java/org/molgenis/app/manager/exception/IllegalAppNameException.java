package org.molgenis.app.manager.exception;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import org.molgenis.i18n.CodedRuntimeException;

/** Thrown when an app name contains illegal characters */
// S2166 'Classes named like "Exception" should extend "Exception" or a subclass' often gives false
// positives at dev time
@SuppressWarnings({"squid:MaximumInheritanceDepth", "squid:S2166"})
public class IllegalAppNameException extends CodedRuntimeException {
  private static final String ERROR_CODE = "AM10";

  private final String name;

  public IllegalAppNameException(String id) {
    super(ERROR_CODE);
    this.name = requireNonNull(id);
  }

  @Override
  public String getMessage() {
    return format("name:%s", name);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {name};
  }
}
