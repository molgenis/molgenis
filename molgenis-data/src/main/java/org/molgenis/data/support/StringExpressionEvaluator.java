package org.molgenis.data.support;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataConverter;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;

import com.google.gson.Gson;

public class StringExpressionEvaluator implements ExpressionEvaluator
{
	private AttributeMetaData targetAttributeMetaData;
	private AttributeMetaData sourceAttributeMetaData;

	/**
	 * Constructs a new EspressionEvaluator for an attribute whose expression is a simple string.
	 * 
	 * @param attributeMetaData
	 * @param entityMetaData
	 */
	public StringExpressionEvaluator(AttributeMetaData attributeMetaData, EntityMetaData entityMetaData)
	{
		targetAttributeMetaData = attributeMetaData;
		String expression = attributeMetaData.getExpression();
		if (expression == null)
		{
			throw new NullPointerException("Attribute has no expression.");
		}
		Gson gson = new Gson();
		String attributeName = gson.fromJson(expression, String.class);
		sourceAttributeMetaData = entityMetaData.getAttribute(attributeName);
		if (sourceAttributeMetaData == null)
		{
			throw new IllegalArgumentException("Expression for attribute '" + attributeMetaData.getName()
					+ "' references non-existant attribute '" + attributeName + "'.");
		}
	}

	@Override
	public Object evaluate(Entity entity)
	{
		Object o = entity.get(sourceAttributeMetaData.getName());
		return DataConverter.convert(o, targetAttributeMetaData);
	}
}
