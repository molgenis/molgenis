package org.molgenis.data.support;

import com.google.gson.Gson;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.util.UnexpectedEnumException;

class ExpressionEvaluatorFactory {
  private ExpressionEvaluatorFactory() {}

  static ExpressionEvaluator createExpressionEvaluator(Attribute attribute, EntityType entityType) {
    ExpressionEvaluator expressionEvaluator;

    Object expressionJson = new Gson().fromJson(attribute.getExpression(), Object.class);
    if (expressionJson instanceof String) {
      expressionEvaluator = new StringExpressionEvaluator(attribute, entityType);
    } else {
      switch (attribute.getDataType()) {
        case BOOL:
        case CATEGORICAL:
        case CATEGORICAL_MREF:
        case COMPOUND:
        case DATE:
        case DATE_TIME:
        case DECIMAL:
        case FILE:
        case INT:
        case LONG:
        case MREF:
        case ONE_TO_MANY:
        case XREF:
          expressionEvaluator = new MapOfStringsExpressionEvaluator(attribute, entityType);
          break;
        case EMAIL:
        case ENUM:
        case HTML:
        case HYPERLINK:
        case SCRIPT:
        case STRING:
        case TEXT:
          expressionEvaluator = new TemplateExpressionEvaluator(attribute, entityType);
          break;
        default:
          throw new UnexpectedEnumException(attribute.getDataType());
      }
    }

    return expressionEvaluator;
  }
}
