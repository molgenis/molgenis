package org.molgenis.lifelines.catalogue;

/**
 * Helper class to convert a omx catalog identifier to a LL catalogue id and vice versa. Prefix is used to prevent
 * clashes with other identifiers (identifiers need to be unique)
 * 
 * @author erwin
 * 
 */
public class CatalogIdConverter
{
	private static final String PREFIX_CATALOG = "catalog_";
	private static final String PREFIX_STUDYDEFINITION = "studydefinition_";

	public static String catalogIdToOmxIdentifier(String id)
	{
		return PREFIX_CATALOG + id;
	}

	public static final String omxIdentifierToCatalogId(String identifier)
	{
		if (!identifier.startsWith(PREFIX_CATALOG))
		{
			throw new IllegalArgumentException("Identifier [" + identifier
					+ "] is not from a catalogue. It should start with " + PREFIX_CATALOG);
		}

		return identifier.substring(PREFIX_CATALOG.length());
	}

	public static String catalogOfStudyDefinitionIdToOmxIdentifier(String id)
	{
		return PREFIX_STUDYDEFINITION + id;
	}

	public static final String omxIdentifierToCatalogOfStudyDefinitionId(String identifier)
	{
		if (!identifier.startsWith(PREFIX_STUDYDEFINITION))
		{
			throw new IllegalArgumentException("Identifier [" + identifier
					+ "] is not from a catalogue of a study definition. It should start with " + PREFIX_STUDYDEFINITION);
		}

		return identifier.substring(PREFIX_STUDYDEFINITION.length());
	}
}
