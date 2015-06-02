package org.molgenis.data.meta;

import java.util.List;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataAccessException;
import org.molgenis.data.MolgenisDataException;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;

public class MetaUtils
{
	public static List<AttributeMetaData> updateEntityMeta(MetaDataService metaDataService, EntityMetaData entityMeta,
			boolean sync)
	{
		EntityMetaData existingEntityMetaData = metaDataService.getEntityMetaData(entityMeta.getName());
		List<AttributeMetaData> addedAttributes = Lists.newArrayList();

		for (AttributeMetaData attr : existingEntityMetaData.getAttributes())
		{
			if (entityMeta.getAttribute(attr.getName()) == null)
			{
				throw new MolgenisDataException(
						"Removing of existing attributes is currently not supported. You tried to remove attribute ["
								+ attr.getName() + "] of entity [" + entityMeta.getName() + "]");
			}
		}

		for (AttributeMetaData attr : entityMeta.getAttributes())
		{
			AttributeMetaData currentAttribute = existingEntityMetaData.getAttribute(attr.getName());
			if (currentAttribute != null)
			{
				if (!currentAttribute.isSameAs(attr))
				{
					throw new MolgenisDataException(
							"Changing existing attributes is not currently supported. You tried to alter attribute ["
									+ attr.getName() + "] of entity [" + entityMeta.getName()
									+ "]. Only adding of new attributes is supported.");
				}
			}
			else if (!attr.isNillable())
			{
				throw new MolgenisDataException(
						"Adding non-nillable attributes is not currently supported.  You tried to add non-nillable attribute ["
								+ attr.getName() + "] of entity [" + entityMeta.getName() + "].");
			}
			else
			{
				if (sync) metaDataService.addAttributeSync(entityMeta.getName(), attr);
				else metaDataService.addAttribute(entityMeta.getName(), attr);

				addedAttributes.add(attr);
			}
		}

		return addedAttributes;
	}

	/**
	 * Convert a list of AttributeMetaDataEntity to AttributeMetaData
	 * 
	 * @param entityMetaData
	 * @param attributeMetaDataEntities
	 * @return
	 */
	public static Iterable<AttributeMetaData> toExistingAttributeMetaData(EntityMetaData entityMetaData,
			Iterable<Entity> attributeMetaDataEntities)
	{
		return FluentIterable.from(attributeMetaDataEntities).transform(new Function<Entity, AttributeMetaData>()
		{
			public AttributeMetaData apply(Entity attributeMetaDataEntity)
			{
				String attributeName = attributeMetaDataEntity.getString(AttributeMetaDataMetaData.NAME);
				AttributeMetaData attribute = entityMetaData.getAttribute(attributeName);
				if (attribute == null) throw new MolgenisDataAccessException("The attributeMetaData : " + attributeName
						+ " does not exsit in EntityMetaData : " + entityMetaData.getName());
				return attribute;
			}
		}).toList();
	}

	/**
	 * Validates names of entities and packages. Rules: only [a-zA-Z0-9] are allowed, name must start with a letter
	 */
	public static void validateName(String name)
	{
		if (!name.matches("[a-zA-Z0-9]+"))
		{
			throw new MolgenisDataException("Invalid characters in: [" + name
					+ "] Only letters (a-z, A-Z) and digits (0-9) are allowed.");
		}

		if (Character.isDigit(name.charAt(0)))
		{
			throw new MolgenisDataException("Invalid name: [" + name + "] Names must start with a letter.");
		}
	}

	/**
	 * Validates attribute names. Extends the rules of entity/package names with a maximum length of 16 characters.
	 */
	public static void validateAttributeName(String name)
	{
		if (name.length() > 16)
		{
			throw new MolgenisDataException("Attribute name [" + name
					+ "] is too long: maximum length is 16 characters.");
		}
		validateName(name);
	}

	/**
	 * Recursively traverses attributes and validates the names.
	 */
	public static void validateAttributeNames(Iterable<AttributeMetaData> amds)
	{
		for (AttributeMetaData amd : amds)
		{
			if (amd.getDataType().getEnumType().equals(MolgenisFieldTypes.COMPOUND))
			{
				validateAttributeNames(amd.getAttributeParts());
			}
			else
			{
				validateAttributeName(amd.getName());
			}
		}
	}

	/**
	 * Validates the names of an entity and all its attributes.
	 */
	public static void validateEntity(EntityMetaData emd)
	{
		validateName(emd.getSimpleName());

		try
		{
			validateAttributeNames(emd.getAttributes());
		}
		catch (MolgenisDataException e)
		{
			throw new MolgenisDataException("Validation error in entity [" + emd.getName() + "]: " + e.getMessage(), e);
		}
	}
}
