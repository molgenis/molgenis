package org.molgenis.data.support;

import com.google.gson.Gson;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;

public class ExpressionEvaluatorFactory
{
	public static ExpressionEvaluator createExpressionEvaluator(Attribute attribute, EntityType entityType)
	{
		Object expressionJson = new Gson().fromJson(attribute.getExpression(), Object.class);
		if (expressionJson instanceof String)
		{
			return new StringExpressionEvaluator(attribute, entityType);
		}
		else
		{
			return new MapOfStringsExpressionEvaluator(attribute, entityType);
		}
	}
}
