package org.molgenis.validation;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.join;

import java.util.Set;
import org.molgenis.i18n.CodedRuntimeException;

/** Exception that lists one or more violations in a JSON object or schema. */
// S2166 'Classes named like "Exception" should extend "Exception" or a subclass' often gives false
// positives at dev time
@SuppressWarnings({"squid:MaximumInheritanceDepth", "squid:S2166"})
public class JsonValidationException extends CodedRuntimeException {
  private static final String ERROR_CODE = "V01";
  private final Set<ConstraintViolation> violations;

  public JsonValidationException(Set<ConstraintViolation> violations) {
    super(ERROR_CODE);
    this.violations = requireNonNull(violations);
  }

  public Set<ConstraintViolation> getViolations() {
    return violations;
  }

  @Override
  public String getMessage() {
    return format("violations: %s", getViolationMessages());
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {getViolationMessages()};
  }

  private String getViolationMessages() {
    if ((violations == null) || (violations.isEmpty())) return "Unknown validation exception.";
    return join(violations.stream().map(ConstraintViolation::getMessage).collect(toList()), '.');
  }
}
