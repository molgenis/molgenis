package org.molgenis.data.meta;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.fieldtypes.CompoundField;

public class MetaValidationUtils
{
	public static final int MAX_ATTRIBUTE_LENGTH = 16;

	/**
	 * Validates names of entities and packages. Rules: only [a-zA-Z0-9_] are allowed, name must start with a letter
	 */
	public static void validateName(String name)
	{
		if (!name.matches("[a-zA-Z0-9_]+"))
		{
			throw new MolgenisDataException("Invalid characters in: [" + name
					+ "] Only letters (a-z, A-Z), digits (0-9) and underscores (_) are allowed.");
		}

		if (Character.isDigit(name.charAt(0)))
		{
			throw new MolgenisDataException("Invalid name: [" + name + "] Names must start with a letter.");
		}
	}

	/**
	 * Validates one attribute name. Extends the rules of entity/package names with a maximum length.
	 */
	public static void validateAttributeName(String name)
	{
		if (name.length() > MAX_ATTRIBUTE_LENGTH)
		{
			throw new MolgenisDataException("Attribute name [" + name + "] is too long: maximum length is "
					+ MAX_ATTRIBUTE_LENGTH + " characters.");
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
			validateAttributeName(amd.getName());
			if (amd.getDataType() instanceof CompoundField)
			{
				validateAttributeNames(amd.getAttributeParts());
			}
		}
	}

	/**
	 * Validates the names of an entity and all its attributes.
	 */
	public static void validateEntityMetaData(EntityMetaData emd)
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
