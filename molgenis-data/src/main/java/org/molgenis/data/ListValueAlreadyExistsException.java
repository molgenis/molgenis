package org.molgenis.data;

import static org.molgenis.util.i18n.MessageSourceHolder.getMessageSource;
import static org.springframework.context.i18n.LocaleContextHolder.getLocale;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

@SuppressWarnings("java:MaximumInheritanceDepth")
public class ListValueAlreadyExistsException extends DataConstraintViolationException {
  private static final String UNKNOWN_PLACEHOLDER = "unknown.placeholder";

  private static final String ERROR_CODE = "D25";
  private final String entityTypeId;
  private final String attributeName;
  private final String entityId;
  private final String value;

  public ListValueAlreadyExistsException(
      @Nullable @CheckForNull String entityTypeId,
      @Nullable @CheckForNull String attributeName,
      String entityId,
      String value,
      @Nullable @CheckForNull Throwable cause) {
    super(ERROR_CODE, cause);
    this.entityTypeId = entityTypeId;
    this.attributeName = attributeName;
    this.entityId = entityId;
    this.value = value;
  }

  @Override
  public String getMessage() {
    return "entityTypeId:"
        + entityTypeId
        + " attributeName:"
        + attributeName
        + " entityId:"
        + entityId
        + " value:"
        + value;
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    String unknownPlaceholder =
        getMessageSource().getMessage(UNKNOWN_PLACEHOLDER, null, getLocale());
    return new Object[] {
      entityTypeId != null ? entityTypeId : unknownPlaceholder,
      attributeName != null ? attributeName : unknownPlaceholder,
      entityId,
      value
    };
  }
}
