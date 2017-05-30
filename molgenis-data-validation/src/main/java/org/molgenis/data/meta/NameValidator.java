package org.molgenis.data.meta;

import com.google.common.collect.Sets;
import org.molgenis.data.MolgenisDataException;

import java.util.Set;

/**
 * Validates if metadata is internally consistent and correct.
 */
public class NameValidator
{
	// some words are reserved for the RestAPI and default packages/entities/attributes, etc.
	public static final Set<String> KEYWORDS = Sets.newHashSet("login", "logout", "csv", "base", "exist", "meta");

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

	public static void validatePackageId(String name)
	{
		validateName(name);
		checkForIllegalCharacters(name);
	}

	private static void validateName(String name)
	{
		if (Character.isDigit(name.charAt(0)))
		{
			throw new MolgenisDataException("Invalid name: [" + name + "] Names must start with a letter.");
		}
	}

	private static void checkForIllegalCharacters(String name)
	{
		if (!name.matches("[a-zA-Z0-9_#]+(-[a-z]{2,3})??$")) // FIXME too restrictive
		{
			throw new MolgenisDataException("Invalid characters in: [" + name
					+ "] Only letters (a-z, A-Z), digits (0-9), underscores(_) and hashes (#) are allowed.");
		}
	}
}
