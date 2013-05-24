package org.molgenis.lifelines.studydefinition;

/**
 * Helper class to convert a omx studydefinition identifier to a LL studydefinition id and vice versa. Prefix is used to
 * prevent clashes with other identifiers (identifiers need to be unique)
 * 
 * @author erwin
 * 
 */
public class StudyDefinitionIdConverter
{
	private static final String PREFIX = "studydefinition_";

	public static String studyDefinitionIdToOmxIdentifier(String id)
	{
		return PREFIX + id;
	}

	public static final String omxIdentifierToStudyDefinitionId(String identifier)
	{
		if (!identifier.startsWith(PREFIX))
		{
			throw new IllegalArgumentException("Identifier [" + identifier
					+ "] is not from a studydefinition. It should start with " + PREFIX);
		}

		return identifier.substring(PREFIX.length());
	}

}
