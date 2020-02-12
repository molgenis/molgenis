package org.molgenis.data;

import static org.molgenis.util.i18n.MessageSourceHolder.getMessageSource;
import static org.springframework.context.i18n.LocaleContextHolder.getLocale;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

@SuppressWarnings("java:MaximumInheritanceDepth")
public class ExistingNullValueException extends DataConstraintViolationException {
  private static final String UNKNOWN_PLACEHOLDER = "unknown.placeholder";

  private static final String ERROR_CODE = "D21b";

  private final String entityTypeId;
  private final String attributeName;

  public ExistingNullValueException(
      @Nullable @CheckForNull String entityTypeId,
      @Nullable @CheckForNull String attributeName,
      @Nullable @CheckForNull Throwable cause) {
    super(ERROR_CODE, cause);
    this.entityTypeId = entityTypeId;
    this.attributeName = attributeName;
  }

  @Override
  public String getMessage() {
    return "entityTypeId:" + entityTypeId + " attributeName:" + attributeName;
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    String unknownPlaceholder =
        getMessageSource().getMessage(UNKNOWN_PLACEHOLDER, null, getLocale());
    return new Object[] {
      entityTypeId != null ? entityTypeId : unknownPlaceholder,
      attributeName != null ? attributeName : unknownPlaceholder
    };
  }
}
