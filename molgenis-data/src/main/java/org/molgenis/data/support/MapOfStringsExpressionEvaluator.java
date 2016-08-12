package org.molgenis.data.support;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;

import java.util.Map;
import java.util.Map.Entry;

import static org.molgenis.data.meta.model.EntityMetaData.AttributeCopyMode.SHALLOW_COPY_ATTRS;

public class MapOfStringsExpressionEvaluator implements ExpressionEvaluator
{
	private final AttributeMetaData targetAttributeMetaData;
	private Map<String, ExpressionEvaluator> evaluators;

	/**
	 * Constructs a new expression evaluator for an attribute whose expression is a simple string.
	 *
	 * @param attrMeta   attribute meta data
	 * @param entityMeta entity meta data
	 */
	public MapOfStringsExpressionEvaluator(AttributeMetaData attrMeta, EntityMetaData entityMeta)
	{
		targetAttributeMetaData = attrMeta;
		String expression = attrMeta.getExpression();
		if (expression == null)
		{
			throw new NullPointerException("Attribute has no expression.");
		}
		EntityMetaData refEntity = attrMeta.getRefEntity();
		if (refEntity == null)
		{
			throw new NullPointerException("refEntity not specified.");
		}
		Gson gson = new Gson();
		try
		{
			@SuppressWarnings("unchecked") Map<String, String> attributeExpressions = gson
					.fromJson(expression, Map.class);
			ImmutableMap.Builder<String, ExpressionEvaluator> builder = ImmutableMap.builder();
			for (Entry<String, String> entry : attributeExpressions.entrySet())
			{
				AttributeMetaData targetAttributeMetaData = refEntity.getAttribute(entry.getKey());
				if (targetAttributeMetaData == null)
				{
					throw new IllegalArgumentException("Unknown target attribute: " + entry.getKey() + '.');
				}
				AttributeMetaData amd = AttributeMetaData.newInstance(targetAttributeMetaData, SHALLOW_COPY_ATTRS)
						.setExpression(entry.getValue());
				StringExpressionEvaluator evaluator = new StringExpressionEvaluator(amd, entityMeta);
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
		Entity result = new DynamicEntity(targetAttributeMetaData.getRefEntity());
		for (Entry<String, ExpressionEvaluator> entry : evaluators.entrySet())
		{
			result.set(entry.getKey(), entry.getValue().evaluate(entity));
		}
		return result;
	}
}
