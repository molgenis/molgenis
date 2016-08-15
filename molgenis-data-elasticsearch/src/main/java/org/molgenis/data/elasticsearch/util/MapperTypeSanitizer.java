package org.molgenis.data.elasticsearch.util;

import java.util.regex.Pattern;

/**
 * Removes invalid characters from a mapper type.
 * <p>
 * Mapper type cannot start with '_' and cannot contain '#', ',' and '.'
 *
 * @author erwin
 */
public class MapperTypeSanitizer
{
	private static final String INVALID_CHARS = "^[_]|[#,\\.]";
	private static final Pattern PATTERN = Pattern.compile(INVALID_CHARS);
	private static final String REPLACEMENT_STRING = "";

	public static String sanitizeMapperType(String documentTypeName)
	{
		return documentTypeName != null ? PATTERN.matcher(documentTypeName).replaceAll(REPLACEMENT_STRING) : null;
	}
}
