package org.molgenis.data.support;

import com.google.gson.Gson;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;

public class ExpressionEvaluatorFactory
{
	public static ExpressionEvaluator createExpressionEvaluator(AttributeMetaData amd, EntityMetaData emd)
	{
		Object expressionJson = new Gson().fromJson(amd.getExpression(), Object.class);
		if (expressionJson instanceof String)
		{
			return new StringExpressionEvaluator(amd, emd);
		}
		else
		{
			return new MapOfStringsExpressionEvaluator(amd, emd);
		}
	}
}
