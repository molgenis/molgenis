package org.molgenis.data.support;

import java.util.Map;

import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;

import autovalue.shaded.com.google.common.common.collect.Maps;

/**
 * Entity decorator that computes computed attributes.
 */
public class EntityWithComputedAttributes extends DynamicEntity
{
	private static final long serialVersionUID = 1L;

	private final Map<String, ExpressionEvaluator> expressionEvaluators;

	public EntityWithComputedAttributes(Entity entity)
	{
		super(entity.getEntityMetaData());
		expressionEvaluators = Maps.newHashMap();
		EntityMetaData emd = entity.getEntityMetaData();
		for (AttributeMetaData amd : emd.getAtomicAttributes())
		{
			if (amd.getExpression() != null)
			{
				expressionEvaluators.put(amd.getName(), ExpressionEvaluatorFactory.createExpressionEvaluator(amd, emd));
			}
		}
	}

	@Override
	public Object get(String attributeName)
	{
		ExpressionEvaluator expressionEvaluator = expressionEvaluators.get(attributeName);
		if (expressionEvaluator != null)
		{
			return expressionEvaluator.evaluate(this);
		}
		return super.get(attributeName);
	}

	@Override
	public void set(String attributeName, Object value)
	{
		if (expressionEvaluators.containsKey(attributeName))
		{
			throw new MolgenisDataException("Attribute " + attributeName + "is computed");
		}
		super.set(attributeName, value);
	}
}
