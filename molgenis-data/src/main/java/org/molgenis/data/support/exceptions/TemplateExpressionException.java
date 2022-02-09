package org.molgenis.data.support.exceptions;

import java.io.IOException;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.data.meta.model.Attribute;

public class TemplateExpressionException extends RuntimeException {
  public TemplateExpressionException(Attribute attribute) {
    this(attribute, null);
  }

  public TemplateExpressionException(Attribute attribute, @Nullable @CheckForNull IOException e) {
    super("Attribute " + attribute.getName() + " expression is null", e);
  }
}
