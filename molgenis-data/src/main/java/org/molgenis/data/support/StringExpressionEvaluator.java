package org.molgenis.data.support;

import org.molgenis.data.DataConverter;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;

import com.google.gson.Gson;

public class StringExpressionEvaluator implements ExpressionEvaluator
{
	private AttributeMetaData targetAttributeMetaData;
	private AttributeMetaData sourceAttributeMetaData;

	/**
	 * Constructs a new expression evaluator for an attribute whose expression is a simple string.
	 *
	 * @param attrMeta attribute meta data
	 * @param entityMeta entity meta data
	 */
	public StringExpressionEvaluator(AttributeMetaData attrMeta, EntityMetaData entityMeta)
	{
		targetAttributeMetaData = attrMeta;
		String expression = attrMeta.getExpression();
		if (expression == null)
		{
			throw new NullPointerException("Attribute has no expression.");
		}
		Gson gson = new Gson();
		String attributeName = gson.fromJson(expression, String.class);
		sourceAttributeMetaData = entityMeta.getAttribute(attributeName);
		if (sourceAttributeMetaData == null)
		{
			throw new IllegalArgumentException("Expression for attribute '" + attrMeta.getName()
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
