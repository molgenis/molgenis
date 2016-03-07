package org.molgenis.util;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.springframework.beans.BeanUtils;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class EntityUtils
{

	/**
	 * Checks if an entity contains data or not
	 * 
	 * @param entity
	 */
	public static boolean isEmpty(Entity entity)
	{
		for (String attr : entity.getAttributeNames())
		{
			if (entity.get(attr) != null)
			{
				return false;
			}
		}

		return true;
	}

	public static List<Pair<EntityMetaData, List<AttributeMetaData>>> getReferencingEntityMetaData(
			EntityMetaData entityMetaData, DataService dataService)
	{
		List<Pair<EntityMetaData, List<AttributeMetaData>>> referencingEntityMetaData = new ArrayList<Pair<EntityMetaData, List<AttributeMetaData>>>();

		// get entity types that referencing the given entity (including self)
		String entityName = entityMetaData.getName();
		dataService.getEntityNames().forEach(otherEntityName -> {
			EntityMetaData otherEntityMetaData = dataService.getEntityMetaData(otherEntityName);

			// get referencing attributes for other entity
			List<AttributeMetaData> referencingAttributes = null;
			for (AttributeMetaData attributeMetaData : otherEntityMetaData.getAtomicAttributes())
			{
				EntityMetaData refEntityMetaData = attributeMetaData.getRefEntity();
				if (refEntityMetaData != null && refEntityMetaData.getName().equals(entityName))
				{
					if (referencingAttributes == null) referencingAttributes = new ArrayList<AttributeMetaData>();
					referencingAttributes.add(attributeMetaData);
				}
			}

			// store references
			if (referencingAttributes != null)
			{
				referencingEntityMetaData.add(
						new Pair<EntityMetaData, List<AttributeMetaData>>(otherEntityMetaData, referencingAttributes));
			}
		});

		return referencingEntityMetaData;
	}

	/**
	 * Gets all attribute names of an EntityMetaData (atomic + compound)
	 * 
	 * @param entityMetaData
	 * @return
	 */
	public static Iterable<String> getAttributeNames(EntityMetaData entityMetaData)
	{
		// atomic
		Iterable<String> atomicAttributes = Iterables.transform(entityMetaData.getAtomicAttributes(),
				new Function<AttributeMetaData, String>()
				{

					@Override
					public String apply(AttributeMetaData attributeMetaData)
					{
						return attributeMetaData.getName();
					}
				});

		// compound
		Iterable<String> compoundAttributes = Iterables
				.transform(Iterables.filter(entityMetaData.getAttributes(), new Predicate<AttributeMetaData>()
				{
					@Override
					public boolean apply(AttributeMetaData attributeMetaData)
					{
						return attributeMetaData.getDataType().getEnumType() == FieldTypeEnum.COMPOUND;
					}
				}), new Function<AttributeMetaData, String>()
				{
					@Override
					public String apply(AttributeMetaData attributeMetaData)
					{
						return attributeMetaData.getName();
					}
				});

		// all = atomic + compound
		return Iterables.concat(atomicAttributes, compoundAttributes);
	}

	/**
	 * Convert a untyped entity to a typed entity.
	 * 
	 * If the entity is of the correct class it simply returns it. If entity of other class it tries to convert it, the
	 * entity class should have a constructor with dataservice as single arg or a arg less constructor
	 * 
	 * @param entity
	 * @param entityClass
	 * @param dataService
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <E extends Entity> E convert(Entity entity, Class<E> entityClass, DataService dataService)
	{
		if (entityClass.isAssignableFrom(entity.getClass())) return (E) entity;
		E convertedEntity;

		// Find constructor with DataService as single arg
		Constructor<E> ctor = ConstructorUtils.getAccessibleConstructor(entityClass, DataService.class);
		if (ctor != null)
		{
			convertedEntity = BeanUtils.instantiateClass(ctor, dataService);
		}
		else
		{
			// Find arg less constructor
			ctor = ConstructorUtils.getAccessibleConstructor(entityClass);
			if (ctor == null) throw new RuntimeException("No usable constructor found for entity class ["
					+ entityClass.getName()
					+ "]. Entity class should have a constructor with dataservice as single arg or a constructor without arguments");

			convertedEntity = BeanUtils.instantiateClass(ctor);
		}

		convertedEntity.set(entity);

		return convertedEntity;
	}

	/**
	 * Checks if an entity has another entity as one of its parents
	 * 
	 * @param entityMetaData
	 * @param entityName
	 * @return
	 */
	public static boolean doesExtend(EntityMetaData entityMetaData, String entityName)
	{
		EntityMetaData parent = entityMetaData.getExtends();
		while (parent != null)
		{
			if (parent.getName().equalsIgnoreCase(entityName)) return true;
			parent = parent.getExtends();
		}

		return false;
	}

	/**
	 * Get an Iterable of entities as a stream of entities
	 * 
	 * @param entities
	 * @return
	 */
	public static Stream<Entity> asStream(Iterable<Entity> entities)
	{
		return StreamSupport.stream(entities.spliterator(), false);
	}
}
