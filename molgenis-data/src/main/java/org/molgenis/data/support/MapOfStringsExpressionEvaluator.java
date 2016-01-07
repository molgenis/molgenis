package org.molgenis.data.support;

import java.util.Map;
import java.util.Map.Entry;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;

public class MapOfStringsExpressionEvaluator implements ExpressionEvaluator
{
	private final AttributeMetaData targetAttributeMetaData;
	private Map<String, ExpressionEvaluator> evaluators;

	/**
	 * Constructs a new EspressionEvaluator for an attribute whose expression is a simple string.
	 * 
	 * @param attributeMetaData
	 * @param entityMetaData
	 */
	public MapOfStringsExpressionEvaluator(AttributeMetaData attributeMetaData, EntityMetaData entityMetaData)
	{
		targetAttributeMetaData = new DefaultAttributeMetaData(attributeMetaData);
		String expression = attributeMetaData.getExpression();
		if (expression == null)
		{
			throw new NullPointerException("Attribute has no expression.");
		}
		EntityMetaData refEntity = attributeMetaData.getRefEntity();
		if (refEntity == null)
		{
			throw new NullPointerException("refEntity not specified.");
		}
		Gson gson = new Gson();
		try
		{
			@SuppressWarnings("unchecked")
			Map<String, String> attributeExpressions = gson.fromJson(expression, Map.class);
			ImmutableMap.Builder<String, ExpressionEvaluator> builder = ImmutableMap
					.<String, ExpressionEvaluator> builder();
			for (Entry<String, String> entry : attributeExpressions.entrySet())
			{
				AttributeMetaData targetAttributeMetaData = refEntity.getAttribute(entry.getKey());
				if (targetAttributeMetaData == null)
				{
					throw new IllegalArgumentException("Unknown target attribute: " + entry.getKey() + '.');
				}
				DefaultAttributeMetaData amd = new DefaultAttributeMetaData(targetAttributeMetaData)
						.setExpression(entry.getValue());
				StringExpressionEvaluator evaluator = new StringExpressionEvaluator(amd, entityMetaData);
				builder.put(entry.getKey(), evaluator);
			}
			evaluators = builder.build();
		}
		catch (ClassCastException ex)
		{
			throw new IllegalArgumentException(
					"Nested expressions not supported, expression must be Map<String,String>.");
		}

	}

	@Override
	public Object evaluate(Entity entity)
	{
		MapEntity result = new MapEntity(targetAttributeMetaData.getRefEntity());
		for (Entry<String, ExpressionEvaluator> entry : evaluators.entrySet())
		{
			result.set(entry.getKey(), entry.getValue().evaluate(entity));
		}
		return result;
	}
}
