package org.molgenis.data.support;

import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.support.ExpressionEvaluatorFactory.createExpressionEvaluator;

/**
 * Entity decorator that computes computed attributes.
 */
public class EntityWithComputedAttributes implements Entity
{
	private final Entity decoratedEntity;
	private final Map<String, ExpressionEvaluator> expressionEvaluators;

	public EntityWithComputedAttributes(Entity decoratedEntity)
	{
		this.decoratedEntity = requireNonNull(decoratedEntity);
		expressionEvaluators = newHashMap();
		EntityType entityType = decoratedEntity.getEntityType();
		for (Attribute attribute : entityType.getAtomicAttributes())
			if (attribute.getExpression() != null)
			{
				expressionEvaluators.put(attribute.getName(), createExpressionEvaluator(attribute, entityType));
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
		return decoratedEntity.get(attributeName);
	}

	@Override
	public Iterable<String> getAttributeNames()
	{
		return decoratedEntity.getAttributeNames();
	}

	@Override
	public Boolean getBoolean(String attributeName)
	{
		ExpressionEvaluator expressionEvaluator = expressionEvaluators.get(attributeName);
		if (expressionEvaluator != null)
		{
			return (Boolean) expressionEvaluator.evaluate(this);
		}
		return decoratedEntity.getBoolean(attributeName);
	}

	@Override
	public Double getDouble(String attributeName)
	{
		ExpressionEvaluator expressionEvaluator = expressionEvaluators.get(attributeName);
		if (expressionEvaluator != null)
		{
			return (Double) expressionEvaluator.evaluate(this);
		}
		return decoratedEntity.getDouble(attributeName);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterable<Entity> getEntities(String attributeName)
	{
		ExpressionEvaluator expressionEvaluator = expressionEvaluators.get(attributeName);
		if (expressionEvaluator != null)
		{
			return (Iterable<Entity>) expressionEvaluator.evaluate(this);
		}
		return decoratedEntity.getEntities(attributeName);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E extends Entity> Iterable<E> getEntities(String attributeName, Class<E> clazz)
	{
		ExpressionEvaluator expressionEvaluator = expressionEvaluators.get(attributeName);
		if (expressionEvaluator != null)
		{
			return (Iterable<E>) expressionEvaluator.evaluate(this);
		}
		return decoratedEntity.getEntities(attributeName, clazz);
	}

	@Override
	public Entity getEntity(String attributeName)
	{
		ExpressionEvaluator expressionEvaluator = expressionEvaluators.get(attributeName);
		if (expressionEvaluator != null)
		{
			return (Entity) expressionEvaluator.evaluate(this);
		}
		return decoratedEntity.getEntity(attributeName);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E extends Entity> E getEntity(String attributeName, Class<E> clazz)
	{
		ExpressionEvaluator expressionEvaluator = expressionEvaluators.get(attributeName);
		if (expressionEvaluator != null)
		{
			return (E) expressionEvaluator.evaluate(this);
		}
		return decoratedEntity.getEntity(attributeName, clazz);
	}

	public EntityType getEntityType()
	{
		return decoratedEntity.getEntityType();
	}

	@Override
	public Object getIdValue()
	{
		return decoratedEntity.getIdValue();
	}

	@Override
	public Integer getInt(String attributeName)
	{
		ExpressionEvaluator expressionEvaluator = expressionEvaluators.get(attributeName);
		if (expressionEvaluator != null)
		{
			return (Integer) expressionEvaluator.evaluate(this);
		}
		return decoratedEntity.getInt(attributeName);
	}

	@Override
	public Object getLabelValue()
	{
		return decoratedEntity.getLabelValue();
	}

	@Override
	public Long getLong(String attributeName)
	{
		ExpressionEvaluator expressionEvaluator = expressionEvaluators.get(attributeName);
		if (expressionEvaluator != null)
		{
			return (Long) expressionEvaluator.evaluate(this);
		}
		return decoratedEntity.getLong(attributeName);
	}

	@Override
	public String getString(String attributeName)
	{
		ExpressionEvaluator expressionEvaluator = expressionEvaluators.get(attributeName);
		if (expressionEvaluator != null)
		{
			return (String) expressionEvaluator.evaluate(this);
		}
		return decoratedEntity.getString(attributeName);
	}

	@Override
	public LocalDate getLocalDate(String attributeName)
	{
		ExpressionEvaluator expressionEvaluator = expressionEvaluators.get(attributeName);
		if (expressionEvaluator != null)
		{
			return (LocalDate) expressionEvaluator.evaluate(this);
		}
		return decoratedEntity.getLocalDate(attributeName);
	}

	@Override
	public Instant getInstant(String attributeName)
	{
		ExpressionEvaluator expressionEvaluator = expressionEvaluators.get(attributeName);
		if (expressionEvaluator != null)
		{
			return (Instant) expressionEvaluator.evaluate(this);
		}
		return decoratedEntity.getInstant(attributeName);
	}

	@Override
	public void set(String attributeName, Object value)
	{
		if (expressionEvaluators.containsKey(attributeName))
		{
			throw new MolgenisDataException(format("Attribute [%s] is computed", attributeName));
		}
		decoratedEntity.set(attributeName, value);
	}

	@Override
	public void set(Entity values)
	{
		decoratedEntity.set(values);
	}

	@Override
	public void setIdValue(Object id)
	{
		decoratedEntity.setIdValue(id);
	}
}
