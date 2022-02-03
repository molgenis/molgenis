package org.molgenis.data.support;

import com.google.gson.Gson;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;

class ExpressionEvaluatorFactory {
  private ExpressionEvaluatorFactory() {}

  static ExpressionEvaluator createExpressionEvaluator(Attribute attribute, EntityType entityType) {
    Object expressionJson = new Gson().fromJson(attribute.getExpression(), Object.class);
    if (expressionJson instanceof String) {
      return new StringExpressionEvaluator(attribute, entityType);
    } else {
      return switch (attribute.getDataType()) {
        case BOOL, CATEGORICAL, CATEGORICAL_MREF, COMPOUND, DATE,
            DATE_TIME, DECIMAL, FILE, INT, LONG, MREF, ONE_TO_MANY, XREF ->
            new MapOfStringsExpressionEvaluator(attribute, entityType);
        case EMAIL, ENUM, HTML, HYPERLINK, SCRIPT, STRING, TEXT ->
            new TemplateExpressionEvaluator(attribute, entityType);
      };
    }
  }
}
