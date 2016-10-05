package org.molgenis.data.meta;

import org.molgenis.MolgenisFieldTypes.AttributeType;
import org.molgenis.ReservedKeywords;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;

import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static org.molgenis.MolgenisFieldTypes.AttributeType.COMPOUND;
import static org.molgenis.data.meta.model.AttributeMetaDataMetaData.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_META_DATA;
import static org.molgenis.data.meta.model.PackageMetaData.PACKAGE;

/**
 * Validates if metadata is internally consistent and correct.
 */
public class MetaValidationUtils
{
	public static final int MAX_ATTRIBUTE_LENGTH = 30;

	public static final Set<String> KEYWORDS = newHashSet();

	static
	{
		// we can generate java(script) files with freemarker, so prevent use of reserved words
		KEYWORDS.addAll(ReservedKeywords.JAVASCRIPT_KEYWORDS);

		// some words are reserved for the RestAPI and default packages/entities/attributes, etc.
		KEYWORDS.addAll(ReservedKeywords.MOLGENIS_KEYWORDS);
	}

	/**
	 * Checks if a name is a reserved keyword.
	 */
	private static void checkForKeyword(String name)
	{
		if (KEYWORDS.contains(name) || KEYWORDS.contains(name.toUpperCase()))
		{
			throw new MolgenisDataException("Name [" + name + "] is not allowed because it is a reserved keyword.");
		}
	}

	/**
	 * Validates names of entities, packages and attributes. Rules: only [a-zA-Z0-9_#] are allowed, name must start with
	 * a letter
	 *
	 * @throws MolgenisDataException
	 */
	public static void validateName(String name)
	{
		checkForKeyword(name);

		if (name.length() > MAX_ATTRIBUTE_LENGTH)
		{
			throw new MolgenisDataException(
					"Name [" + name + "] is too long: maximum length is " + MAX_ATTRIBUTE_LENGTH + " characters.");
		}

		if (!name.matches("[a-zA-Z0-9_#]+(-[a-z]{2,3})??$"))
		{
			throw new MolgenisDataException("Invalid characters in: [" + name
					+ "] Only letters (a-z, A-Z), digits (0-9), underscores (_) and hashes (#) are allowed.");
		}

		if (Character.isDigit(name.charAt(0)))
		{
			throw new MolgenisDataException("Invalid name: [" + name + "] Names must start with a letter.");
		}
	}

	/**
	 * Recursively traverses attributes and validates the names.
	 */
	private static void validateAttributes(Iterable<AttributeMetaData> amds)
	{
		for (AttributeMetaData amd : amds)
		{
			validateAttribute(amd);
			if (amd.getDataType() == COMPOUND)
			{
				validateAttributes(amd.getAttributeParts());
			}
		}
	}

	protected static void validateAttribute(AttributeMetaData amd)
	{
		validateName(amd.getName());
		if (amd.getDefaultValue() != null)
		{
			if (amd.isUnique())
			{
				throw new MolgenisDataException("Unique attribute " + amd.getName() + " cannot have default value");
			}

			if (amd.getExpression() != null)
			{
				throw new MolgenisDataException("Computed attribute " + amd.getName() + " cannot have default value");
			}

			AttributeType fieldType = amd.getDataType();
			if (fieldType == AttributeType.XREF || fieldType == AttributeType.MREF)
			{
				throw new MolgenisDataException("Attribute " + amd.getName()
						+ " cannot have default value since specifying a default value for XREF and MREF data types is not yet supported.");
			}
		}
	}

	/**
	 * Validates an entity and all of its attributes.
	 *
	 * @param entityMeta entity meta data to validate
	 * @throw MolgenisDataException if entity meta data is not valid
	 */
	public static void validateEntityMetaData(EntityMetaData entityMeta)
	{
		try
		{
			if (!entityMeta.getName().equals(ATTRIBUTE_META_DATA) && !entityMeta.getName().equals(ENTITY_META_DATA)
					&& !entityMeta.getName().equals(PACKAGE))
			{
				validateName(entityMeta.getSimpleName());
				validateAttributes(entityMeta.getAttributes());
			}

			if (entityMeta.getIdAttribute() != null && entityMeta.getIdAttribute().getDefaultValue() != null)
			{
				throw new MolgenisDataException(
						"ID attribute " + entityMeta.getIdAttribute().getName() + " cannot have default value");
			}
		}
		catch (MolgenisDataException e)
		{
			throw new MolgenisDataException(
					"Validation error in entity [" + entityMeta.getName() + "]: " + e.getMessage(), e);
		}
	}

}
