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
	private static final String PREFIX = "catalog_";

	public static String catalogIdToOmxIdentifier(String id)
	{
		return PREFIX + id;
	}

	public static final String omxIdentifierToCatalogId(String identifier)
	{
		if (!identifier.startsWith(PREFIX))
		{
			throw new IllegalArgumentException("Identifier [" + identifier
					+ "] is not from a catalogue. It should start with " + PREFIX);
		}

		return identifier.substring(PREFIX.length());
	}
}
