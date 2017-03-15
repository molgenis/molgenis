package org.molgenis.data.meta;

import org.molgenis.MolgenisReservedKeywords;
import org.molgenis.data.MolgenisDataException;

import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

/**
 * Validates if metadata is internally consistent and correct.
 */
public class NameValidator
{
	private static final int MAX_ATTRIBUTE_LENGTH = 30;

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
	public static void validateAttributeName(String name)
	{
		checkForKeyword(name);

		validateName(name);

		if (!name.matches("[a-zA-Z0-9_#]+(-[a-z]{2,3})??$"))
		{
			throw new MolgenisDataException("Invalid characters in: [" + name
					+ "] Only letters (a-z, A-Z), digits (0-9), underscores (_) and hashes (#) are allowed.");
		}

	}

	public static void validateEntityName(String name)
	{
		checkForKeyword(name);
		validateName(name);
		checkForIllegalCharacters(name);
	}

	public static void validatePackageName(String name)
	{
		validateName(name);
		checkForIllegalCharacters(name);
	}

	private static void validateName(String name)
	{
		if (name.length() > MAX_ATTRIBUTE_LENGTH)
		{
			throw new MolgenisDataException(
					"Name [" + name + "] is too long: maximum length is " + MAX_ATTRIBUTE_LENGTH + " characters.");
		}

		if (Character.isDigit(name.charAt(0)))
		{
			throw new MolgenisDataException("Invalid name: [" + name + "] Names must start with a letter.");
		}
	}

	private static void checkForIllegalCharacters(String name)
	{
		if (!name.matches("[a-zA-Z0-9#]+(-[a-z]{2,3})??$"))
		{
			throw new MolgenisDataException("Invalid characters in: [" + name
					+ "] Only letters (a-z, A-Z), digits (0-9) and hashes (#) are allowed.");
		}
	}
}
