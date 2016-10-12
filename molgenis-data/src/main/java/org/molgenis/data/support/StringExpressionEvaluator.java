package org.molgenis.data.support;

import com.google.gson.Gson;
import org.molgenis.data.DataConverter;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityMetaData;

public class StringExpressionEvaluator implements ExpressionEvaluator
{
	private Attribute targetAttribute;
	private Attribute sourceAttribute;

	/**
	 * Constructs a new expression evaluator for an attribute whose expression is a simple string.
	 *
	 * @param attrMeta   attribute meta data
	 * @param entityMeta entity meta data
	 */
	public StringExpressionEvaluator(Attribute attrMeta, EntityMetaData entityMeta)
	{
		targetAttribute = attrMeta;
		String expression = attrMeta.getExpression();
		if (expression == null)
		{
			throw new NullPointerException("Attribute has no expression.");
		}
		Gson gson = new Gson();
		String attributeName = gson.fromJson(expression, String.class);
		sourceAttribute = entityMeta.getAttribute(attributeName);
		if (sourceAttribute == null)
		{
			throw new IllegalArgumentException(
					"Expression for attribute '" + attrMeta.getName() + "' references non-existant attribute '"
							+ attributeName + "'.");
		}
	}

	@Override
	public Object evaluate(Entity entity)
	{
		Object o = entity.get(sourceAttribute.getName());
		return DataConverter.convert(o, targetAttribute);
	}
}
