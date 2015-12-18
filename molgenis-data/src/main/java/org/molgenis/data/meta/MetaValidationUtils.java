package org.molgenis.data.meta;

import java.util.Set;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.fieldtypes.CompoundField;
import org.molgenis.model.ReservedKeywords;

import com.google.common.collect.Sets;

/**
 * Validates if metadata is internally consistent and correct.
 */
public class MetaValidationUtils
{
	public static final int MAX_ATTRIBUTE_LENGTH = 30;

	public static final Set<String> KEYWORDS = Sets.newHashSet();
	static
	{
		// we can generate java(script) files with freemarker, so prevent use of reserved words
		KEYWORDS.addAll(ReservedKeywords.JAVA_KEYWORDS);
		KEYWORDS.addAll(ReservedKeywords.JAVASCRIPT_KEYWORDS);

		KEYWORDS.addAll(ReservedKeywords.MYSQL_KEYWORDS);

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
			throw new MolgenisDataException("Attribute name [" + name + "] is too long: maximum length is "
					+ MAX_ATTRIBUTE_LENGTH + " characters.");
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
			if (amd.getDataType() instanceof CompoundField)
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
			if (amd.isIdAtrribute())
			{
				throw new MolgenisDataException("ID attribute " + amd.getName() + " cannot have default value");
			}

			if (amd.isUnique())
			{
				throw new MolgenisDataException("Unique attribute " + amd.getName() + " cannot have default value");
			}

			if (amd.getExpression() != null)
			{
				throw new MolgenisDataException("Computed attribute " + amd.getName() + " cannot have default value");
			}

			FieldTypeEnum fieldType = amd.getDataType().getEnumType();
			if (fieldType == FieldTypeEnum.XREF || fieldType == FieldTypeEnum.MREF)
			{
				throw new MolgenisDataException(
						"Attribute "
								+ amd.getName()
								+ " cannot have default value since specifying a default value for XREF and MREF data types is not yet supported.");
			}
		}
	}

	/**
	 * Validates an entity and all of its attributes.
	 */
	public static void validateEntityMetaData(EntityMetaData emd)
	{
		try
		{
			validateName(emd.getSimpleName());
			validateAttributes(emd.getAttributes());
		}
		catch (MolgenisDataException e)
		{
			throw new MolgenisDataException("Validation error in entity [" + emd.getName() + "]: " + e.getMessage(), e);
		}
	}

}
