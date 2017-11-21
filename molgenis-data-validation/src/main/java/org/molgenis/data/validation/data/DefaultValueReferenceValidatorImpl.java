package org.molgenis.data.validation.data;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.EntityTypeUtils;
import org.molgenis.data.validation.ValidationException;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.model.AttributeMetadata.*;
import static org.molgenis.data.support.AttributeUtils.getDefaultTypedValue;
import static org.molgenis.data.validation.data.DefaultValueReferenceConstraint.REFERENCE_EXISTS;

@Component
class DefaultValueReferenceValidatorImpl implements DefaultValueReferenceValidator
{
	private final DataService dataService;

	DefaultValueReferenceValidatorImpl(DataService dataService)
	{
		this.dataService = requireNonNull(dataService);
	}

	@Override
	public void validateEntityNotReferenced(Entity entity)
	{
		validateEntityNotReferencedById(entity.getIdValue(), entity.getEntityType());
	}

	@Override
	public Stream<Entity> validateEntitiesNotReferenced(Stream<Entity> entityStream, EntityType entityType)
	{
		Multimap<String, Attribute> defaultValueMap = getDefaultValueMap(entityType);
		return entityStream.filter(entity ->
		{
			validateEntityNotReferenced(entity.getIdValue(), entityType, defaultValueMap);
			return true;
		});
	}

	@Override
	public void validateEntityNotReferencedById(Object entityId, EntityType entityType)
	{
		validateEntityNotReferenced(entityId, entityType, getDefaultValueMap(entityType));
	}

	@Override
	public Stream<Object> validateEntitiesNotReferencedById(Stream<Object> entityIdStream, EntityType entityType)
	{
		Multimap<String, Attribute> defaultValueMap = getDefaultValueMap(entityType);
		return entityIdStream.filter(entityId ->
		{
			validateEntityNotReferenced(entityId, entityType, defaultValueMap);
			return true;
		});
	}

	private void validateEntityNotReferenced(Object entityId, EntityType entityType,
			Multimap<String, Attribute> defaultValueMultiMap)
	{
		if (defaultValueMultiMap.containsKey(toAttributeDefaultValue(entityId)))
		{
			Collection<Attribute> attributes = defaultValueMultiMap.get(toAttributeDefaultValue(entityId));
			throw new ValidationException(
					new DefaultValueReferenceValidationResult(REFERENCE_EXISTS, entityType, entityId, attributes));
		}
	}

	@Override
	public void validateEntityTypeNotReferenced(EntityType entityType)
	{
		if (!getDefaultValueMap(entityType).isEmpty())
		{
			throw new ValidationException(new DefaultValueReferenceValidationResult(REFERENCE_EXISTS, entityType));
		}
	}

	private Multimap<String, Attribute> getDefaultValueMap(EntityType entityType)
	{
		Multimap<String, Attribute> defaultValueMultiMap = LinkedHashMultimap.create();

		dataService.query(ATTRIBUTE_META_DATA, Attribute.class)
				   .eq(REF_ENTITY_TYPE, entityType.getIdValue())
				   .and()
				   .not()
				   .eq(DEFAULT_VALUE, null)
				   .findAll()
				   .forEach(attribute ->
				   {
					   if (EntityTypeUtils.isSingleReferenceType(attribute))
					   {
						   Entity defaultEntityValue = (Entity) getDefaultTypedValue(attribute);
						   defaultValueMultiMap.put(defaultEntityValue.getIdValue().toString(), attribute);
					   }
					   else if (EntityTypeUtils.isMultipleReferenceType(attribute))
					   {
						   @SuppressWarnings("unchecked")
						   Iterable<Entity> defaultEntitiesValue = (Iterable<Entity>) getDefaultTypedValue(attribute);
						   defaultEntitiesValue.forEach(defaultEntityValue -> defaultValueMultiMap.put(
								   defaultEntityValue.getIdValue().toString(), attribute));
					   }
				   });

		return defaultValueMultiMap;
	}

	private static String toAttributeDefaultValue(Object id)
	{
		return id.toString();
	}
}
