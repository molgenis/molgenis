package org.molgenis.data.support;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import org.molgenis.data.Entity;
import org.molgenis.data.Range;
import org.molgenis.data.Sort;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Tag;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
				Attribute amd = new AttributeWithJsonExpression(targetAttribute, entry.getValue());
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

	private static class AttributeWithJsonExpression extends Attribute
	{
		private final Attribute attr;
		private final String expression;

		public AttributeWithJsonExpression(Attribute attr, String expression)
		{
			super(attr.getEntityType());
			this.attr = attr;
			this.expression = expression;
		}

		@Override
		public String getExpression()
		{
			return expression;
		}

		@Override
		public Object get(String attributeName)
		{
			return attr.get(attributeName);
		}

		@Override
		public Iterable<String> getAttributeNames()
		{
			return attr.getAttributeNames();
		}

		@Override
		public Boolean getBoolean(String attributeName)
		{
			return attr.getBoolean(attributeName);
		}

		@Override
		public Double getDouble(String attributeName)
		{
			return attr.getDouble(attributeName);
		}

		@Override
		public Iterable<Entity> getEntities(String attributeName)
		{
			return attr.getEntities(attributeName);
		}

		@Override
		public <E extends Entity> Iterable<E> getEntities(String attributeName, Class<E> clazz)
		{
			return attr.getEntities(attributeName, clazz);
		}

		@Override
		public Entity getEntity(String attributeName)
		{
			return attr.getEntity(attributeName);
		}

		@Override
		public <E extends Entity> E getEntity(String attributeName, Class<E> clazz)
		{
			return attr.getEntity(attributeName, clazz);
		}

		@Override
		public EntityType getEntityType()
		{
			return attr.getEntityType();
		}

		@Override
		public Object getIdValue()
		{
			return attr.getIdValue();
		}

		@Override
		public Integer getInt(String attributeName)
		{
			return attr.getInt(attributeName);
		}

		@Override
		public Object getLabelValue()
		{
			return attr.getLabelValue();
		}

		@Override
		public Long getLong(String attributeName)
		{
			return attr.getLong(attributeName);
		}

		@Override
		public String getString(String attributeName)
		{
			return attr.getString(attributeName);
		}

		@Override
		public LocalDate getLocalDate(String attributeName)
		{
			return attr.getLocalDate(attributeName);
		}

		@Override
		public Instant getInstant(String attributeName)
		{
			return attr.getInstant(attributeName);
		}

		@Override
		public String getIdentifier()
		{
			return attr.getIdentifier();
		}

		@Override
		public String getName()
		{
			return attr.getName();
		}

		@Override
		public Integer getSequenceNumber()
		{
			return attr.getSequenceNumber();
		}

		@Override
		public EntityType getEntity()
		{
			return attr.getEntity();
		}

		@Override
		public boolean isIdAttribute()
		{
			return attr.isIdAttribute();
		}

		@Override
		public boolean isLabelAttribute()
		{
			return attr.isLabelAttribute();
		}

		@Override
		public Integer getLookupAttributeIndex()
		{
			return attr.getLookupAttributeIndex();
		}

		@Override
		public String getLabel()
		{
			return attr.getLabel();
		}

		@Override
		public String getLabel(String languageCode)
		{
			return attr.getLabel(languageCode);
		}

		@Override
		public String getDescription()
		{
			return attr.getDescription();
		}

		@Override
		public String getDescription(String languageCode)
		{
			return attr.getDescription(languageCode);
		}

		@Override
		public AttributeType getDataType()
		{
			return attr.getDataType();
		}

		@Override
		public Iterable<Attribute> getChildren()
		{
			return attr.getChildren();
		}

		@Override
		public EntityType getRefEntity()
		{
			return attr.getRefEntity();
		}

		@Override
		public Attribute getMappedBy()
		{
			return attr.getMappedBy();
		}

		@Override
		public boolean isMappedBy()
		{
			return attr.isMappedBy();
		}

		@Override
		public Sort getOrderBy()
		{
			return attr.getOrderBy();
		}

		@Override
		public boolean hasExpression()
		{
			return attr.hasExpression();
		}

		@Override
		public boolean isNillable()
		{
			return attr.isNillable();
		}

		@Override
		public boolean isAuto()
		{
			return attr.isAuto();
		}

		@Override
		public boolean isVisible()
		{
			return attr.isVisible();
		}

		@Override
		public boolean isAggregatable()
		{
			return attr.isAggregatable();
		}

		@Override
		public List<String> getEnumOptions()
		{
			return attr.getEnumOptions();
		}

		@Override
		public Long getRangeMin()
		{
			return attr.getRangeMin();
		}

		@Override
		public Long getRangeMax()
		{
			return attr.getRangeMax();
		}

		@Override
		public boolean isReadOnly()
		{
			return attr.isReadOnly();
		}

		@Override
		public boolean isUnique()
		{
			return attr.isUnique();
		}

		@Override
		public String getNullableExpression()
		{
			return attr.getNullableExpression();
		}

		@Override
		public String getVisibleExpression()
		{
			return attr.getVisibleExpression();
		}

		@Override
		public String getValidationExpression()
		{
			return attr.getValidationExpression();
		}

		@Override
		public String getDefaultValue()
		{
			return attr.getDefaultValue();
		}

		@Override
		public Range getRange()
		{
			return attr.getRange();
		}

		@Override
		public Attribute getParent()
		{
			return attr.getParent();
		}

		@Override
		public Attribute getChild(String attrName)
		{
			return attr.getChild(attrName);
		}

		@Override
		public Iterable<Tag> getTags()
		{
			return attr.getTags();
		}

		@Override
		public String toString()
		{
			return attr.toString();
		}

		@Override
		public Attribute getInversedBy()
		{
			return attr.getInversedBy();
		}

		@Override
		public boolean isInversedBy()
		{
			return attr.isInversedBy();
		}
	}
}
