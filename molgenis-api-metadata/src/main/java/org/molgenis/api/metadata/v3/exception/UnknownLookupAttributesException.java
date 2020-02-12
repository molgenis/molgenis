package org.molgenis.api.metadata.v3.exception;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import java.util.List;
import org.apache.logging.log4j.util.Strings;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.util.exception.BadRequestException;

@SuppressWarnings("java:MaximumInheritanceDepth")
public class UnknownLookupAttributesException extends BadRequestException {

  private static final String ERROR_CODE = "MAPI05";
  private final String entityTypeId;
  private final List<String> attributeIds;

  public UnknownLookupAttributesException(EntityType entityType, List<String> attributeIds) {
    super(ERROR_CODE);
    this.entityTypeId = requireNonNull(entityType).getId();
    this.attributeIds = requireNonNull(attributeIds);
  }

  @Override
  public String getMessage() {
    return format("entityType:%s attributeIds:%s", entityTypeId, Strings.join(attributeIds, ','));
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {entityTypeId, Strings.join(attributeIds, ',')};
  }
}
