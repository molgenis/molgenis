package org.molgenis.api.data.v3;

import org.molgenis.data.meta.model.Attribute;
import org.molgenis.i18n.BadRequestException;

public class UnsupportedAttributeTypeException extends BadRequestException {

  private static final String ERROR_CODE = "DAPI01";
  private Attribute attribute;

  protected UnsupportedAttributeTypeException(Attribute attribute) {
    super(ERROR_CODE);
    this.attribute = attribute;
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {attribute.getName(), attribute.getDataType()};
  }
}
