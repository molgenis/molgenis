package org.molgenis.data.meta;

import org.molgenis.AttributeType;
import org.molgenis.MolgenisReservedKeywords;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;

import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static java.lang.String.format;
import static org.molgenis.AttributeType.COMPOUND;
import static org.molgenis.data.meta.model.AttributeMetadata.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.molgenis.data.meta.model.PackageMetadata.PACKAGE;

/**
 * Validates if metadata is internally consistent and correct.
 */
public class MetaValidationUtils
{
	public static final int MAX_ATTRIBUTE_LENGTH = 30;

	public static final Set<String> KEYWORDS = newHashSet();

	static
	{
		// some words are reserved for the RestAPI and default packages/entities/attributes, etc.
		KEYWORDS.addAll(MolgenisReservedKeywords.MOLGENIS_KEYWORDS);
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
	private static void validateAttributes(Iterable<Attribute> amds)
	{
		for (Attribute amd : amds)
		{
			validateAttribute(amd);
			if (amd.getDataType() == COMPOUND)
			{
				validateAttributes(amd.getChildren());
			}
		}
	}

	protected static void validateAttribute(Attribute amd)
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
	 * @param entityType entity meta data to validate
	 * @throws MolgenisDataException if entity meta data is not valid
	 */
	public static void validateEntityType(EntityType entityType)
	{
		try
		{
			if (!entityType.getName().equals(ATTRIBUTE_META_DATA) && !entityType.getName().equals(ENTITY_TYPE_META_DATA)
					&& !entityType.getName().equals(PACKAGE))
			{
				validateName(entityType.getSimpleName());
				validateAttributes(entityType.getAttributes());
			}

			if (entityType.getIdAttribute() != null && entityType.getIdAttribute().getDefaultValue() != null)
			{
				throw new MolgenisDataException(
						"ID attribute " + entityType.getIdAttribute().getName() + " cannot have default value");
			}

			if (!entityType.isAbstract() && entityType.getLabelAttribute() == null)
			{
				throw new MolgenisDataException(
						format("Entity [%s] is missing required label attribute", entityType.getName()));
			}
		}
		catch (MolgenisDataException e)
		{
			throw new MolgenisDataException(
					"Validation error in entity [" + entityType.getName() + "]: " + e.getMessage(), e);
		}
	}

}
