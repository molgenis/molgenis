package org.molgenis.data.validation;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import java.util.Set;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.util.exception.CodedRuntimeException;

/** Thrown when a validation expression references unknown attributes */
// S2166 'Classes named like "Exception" should extend "Exception" or a subclass' often gives false
// positives at dev time
@SuppressWarnings({"squid:MaximumInheritanceDepth", "squid:S2166"})
public class UnknownAttributesException extends CodedRuntimeException {

  private static final String ERROR_CODE = "VAL02";
  private final transient Attribute attribute;
  private final Set<String> attributeNames;

  public UnknownAttributesException(Attribute attribute, Set<String> attributeNames) {
    super(ERROR_CODE);
    this.attribute = requireNonNull(attribute);
    this.attributeNames = requireNonNull(attributeNames);
  }

  public UnknownAttributesException(
      Attribute attribute, Set<String> attributeNames, Throwable cause) {
    super(ERROR_CODE, cause);
    this.attribute = requireNonNull(attribute);
    this.attributeNames = requireNonNull(attributeNames);
  }

  @Override
  public String getMessage() {
    return format(
        "entityType: %s, attribute:%s, attributeNames:%s",
        attribute.getEntity().getId(), attribute.getName(), attributeNames);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {attributeNames, attribute};
  }
}
