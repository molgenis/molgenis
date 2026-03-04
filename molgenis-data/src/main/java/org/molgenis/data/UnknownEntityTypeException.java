package org.molgenis.data;

import static java.util.Objects.requireNonNull;

import com.github.jknack.handlebars.internal.text.StringEscapeUtils;

@SuppressWarnings("java:S110")
public class UnknownEntityTypeException extends UnknownDataException {
  private static final String ERROR_CODE = "D01";

  private final String entityTypeId;

  public UnknownEntityTypeException(String entityTypeId) {
    super(ERROR_CODE);
    var nonNullEntityType = requireNonNull(entityTypeId);
    /** To prevent malformed entities that trigger XSS ~JV * */
    this.entityTypeId = StringEscapeUtils.escapeHtml4(nonNullEntityType);
  }

  @Override
  public String getMessage() {
    return String.format("id:%s", entityTypeId);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {entityTypeId};
  }
}
