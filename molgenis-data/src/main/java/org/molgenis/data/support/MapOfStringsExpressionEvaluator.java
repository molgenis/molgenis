package org.molgenis.data.support;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;

import java.util.Map;
import java.util.Map.Entry;

import static org.molgenis.data.meta.model.EntityType.AttributeCopyMode.SHALLOW_COPY_ATTRS;

public class MapOfStringsExpressionEvaluator implements ExpressionEvaluator
{
	private final Attribute targetAttribute;
	private Map<String, ExpressionEvaluator> evaluators;

	/**
	 * Constructs a new expression evaluator for an attribute whose expression is a simple string.
	 *
	 * @param attribute  attribute meta data
	 * @param entityType entity meta data
	 */
	public MapOfStringsExpressionEvaluator(Attribute attribute, EntityType entityType)
	{
		targetAttribute = attribute;
		String expression = attribute.getExpression();
		if (expression == null)
		{
			throw new NullPointerException("Attribute has no expression.");
		}
		EntityType refEntity = attribute.getRefEntity();
		if (refEntity == null)
		{
			throw new NullPointerException("refEntity not specified.");
		}
		Gson gson = new Gson();
		try
		{
			@SuppressWarnings("unchecked")
			Map<String, String> attributeExpressions = gson.fromJson(expression, Map.class);
			ImmutableMap.Builder<String, ExpressionEvaluator> builder = ImmutableMap.builder();
			for (Entry<String, String> entry : attributeExpressions.entrySet())
			{
				Attribute targetAttribute = refEntity.getAttribute(entry.getKey());
				if (targetAttribute == null)
				{
					throw new IllegalArgumentException("Unknown target attribute: " + entry.getKey() + '.');
				}
				Attribute amd = Attribute.newInstance(targetAttribute, SHALLOW_COPY_ATTRS)
						.setExpression(entry.getValue());
				StringExpressionEvaluator evaluator = new StringExpressionEvaluator(amd, entityType);
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
		Entity result = new DynamicEntity(targetAttribute.getRefEntity());
		for (Entry<String, ExpressionEvaluator> entry : evaluators.entrySet())
		{
			result.set(entry.getKey(), entry.getValue().evaluate(entity));
		}
		return result;
	}
}
