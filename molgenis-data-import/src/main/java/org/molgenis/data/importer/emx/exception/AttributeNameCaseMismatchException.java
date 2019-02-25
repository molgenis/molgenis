package org.molgenis.data.importer.emx.exception;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import org.molgenis.i18n.CodedRuntimeException;

public class AttributeNameCaseMismatchException extends CodedRuntimeException {
  private static final String ERROR_CODE = "IMP01";
  private final String name;
  private final String emxAttrMetaAttr;
  private final String sheet;

  public AttributeNameCaseMismatchException(String name, String emxAttrMetaAttr, String sheet) {
    super(ERROR_CODE);
    this.name = requireNonNull(name);
    this.emxAttrMetaAttr = requireNonNull(emxAttrMetaAttr);
    this.sheet = requireNonNull(sheet);
  }

  @Override
  public String getMessage() {
    return format("Name:%s emxAttrMetaAttr:%s sheet:%s", name, emxAttrMetaAttr, sheet);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {name, emxAttrMetaAttr, sheet};
  }
}
