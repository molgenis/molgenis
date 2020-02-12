package org.molgenis.data;

import static org.molgenis.util.i18n.MessageSourceHolder.getMessageSource;
import static org.springframework.context.i18n.LocaleContextHolder.getLocale;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

@SuppressWarnings("java:S110")
public class ReadonlyValueException extends DataConstraintViolationException {
  private static final String UNKNOWN_PLACEHOLDER = "unknown.placeholder";

  private static final String ERROR_CODE = "D18";

  private final String entityTypeId;
  private final String attributeName;
  private final String entityId;

  public ReadonlyValueException(
      @Nullable @CheckForNull String entityTypeId,
      @Nullable @CheckForNull String attributeName,
      String entityId,
      @Nullable @CheckForNull Throwable cause) {
    super(ERROR_CODE, cause);
    this.entityTypeId = entityTypeId;
    this.attributeName = attributeName;
    this.entityId = entityId;
  }

  @Override
  public String getMessage() {
    return "entityTypeId:"
        + entityTypeId
        + " attributeName:"
        + attributeName
        + " entityId:"
        + entityId;
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    String unknownPlaceholder =
        getMessageSource().getMessage(UNKNOWN_PLACEHOLDER, null, getLocale());
    return new Object[] {
      entityTypeId != null ? entityTypeId : unknownPlaceholder,
      attributeName != null ? attributeName : unknownPlaceholder,
      entityId
    };
  }
}
