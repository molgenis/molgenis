package org.molgenis.data.support;

import java.io.IOException;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.data.meta.model.Attribute;

class TemplateExpressionException extends RuntimeException {
  TemplateExpressionException(Attribute attribute) {
    this(attribute, null);
  }

  TemplateExpressionException(Attribute attribute, @Nullable @CheckForNull IOException e) {
    super("Attribute " + attribute.getName() + " expression is null", e);
  }
}
