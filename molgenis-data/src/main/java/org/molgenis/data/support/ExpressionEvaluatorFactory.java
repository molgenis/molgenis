package org.molgenis.data.support;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;

import com.google.gson.Gson;

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
