package org.molgenis.data.support;

import java.util.HashMap;
import java.util.Map;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataException;

/**
 * Entity decorator that computes computed attributes.
 */
public class EntityWithComputedAttributes extends AbstractEntity
{
	private static final long serialVersionUID = 1L;

	private final Map<String, ExpressionEvaluator> expressionEvaluators = new HashMap<String, ExpressionEvaluator>();

	public EntityWithComputedAttributes(Entity entity)
	{
		this.entity = entity;
		EntityMetaData emd = entity.getEntityMetaData();
		for (AttributeMetaData amd : emd.getAtomicAttributes())
		{
			if (amd.getExpression() != null)
			{
				expressionEvaluators.put(amd.getName(), ExpressionEvaluatorFactory.createExpressionEvaluator(amd, emd));
			}
		}
	}

	private final Entity entity;

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return entity.getEntityMetaData();
	}

	@Override
	public Iterable<String> getAttributeNames()
	{
		return entity.getAttributeNames();
	}

	@Override
	public Object getIdValue()
	{
		return entity.getIdValue();
	}

	@Override
	public Object get(String attributeName)
	{
		ExpressionEvaluator expressionEvaluator = expressionEvaluators.get(attributeName);
		if (expressionEvaluator != null)
		{
			return expressionEvaluator.evaluate(this);
		}
		return entity.get(attributeName);
	}

	@Override
	public void set(String attributeName, Object value)
	{
		if (expressionEvaluators.containsKey(attributeName))
		{
			throw new MolgenisDataException("Attribute " + attributeName + "is computed");
		}
		entity.set(attributeName, value);
	}

	@Override
	public void set(Entity values)
	{
		entity.set(values);
	}

}
