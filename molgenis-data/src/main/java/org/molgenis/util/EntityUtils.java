package org.molgenis.util;

import java.lang.reflect.Constructor;

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
		Iterable<String> compoundAttributes = Iterables.transform(
				Iterables.filter(entityMetaData.getAttributes(), new Predicate<AttributeMetaData>()
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
			if (ctor == null) throw new RuntimeException(
					"No usable constructor found for entity class ["
							+ entityClass.getName()
							+ "]. Entity class should have a constructor with dataservice as single arg or a constructor without arguments");

			convertedEntity = BeanUtils.instantiateClass(ctor);
		}

		convertedEntity.set(entity);

		return convertedEntity;
	}
}
