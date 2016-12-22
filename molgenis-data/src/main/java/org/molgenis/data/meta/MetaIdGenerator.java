package org.molgenis.data.meta;

import com.google.common.hash.Hashing;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;

import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Id generator for meta entity types that generates readable ids with a maximum length for use as table names or
 * document types.
 */
public class MetaIdGenerator
{
	private static final Pattern PATTERN_WORD = Pattern.compile("\\w*");
	private static final Pattern PATTERN_WORD_REPLACE = Pattern.compile("\\W");

	private static final char ID_NAME_SEPARATOR = '#';

	private MetaIdGenerator()
	{
	}

	/**
	 * Generates an human readable identifier for an entity type with a given maximum length
	 *
	 * @param entityType    entity type
	 * @param maxByteLength must be >= 10
	 * @return human readable identifier
	 */
	public static String generateId(EntityType entityType, int maxByteLength)
	{
		validateMaxByteLength(maxByteLength);
		return generateId(entityType.getId(), entityType.getSimpleName(), maxByteLength);
	}

	/**
	 * Generates an human readable identifier for an attribute with a given maximum length
	 *
	 * @param attribute attribute
	 * @param maxByteLength must be >= 10
	 * @return human readable identifier
	 */
	public static String generateId(Attribute attribute, int maxByteLength)
	{
		validateMaxByteLength(maxByteLength);
		String attributeName = attribute.getName();
		if (PATTERN_WORD.matcher(attributeName).matches() && attributeName.length() <= maxByteLength)
		{
			return attributeName;
		}
		else
		{
			return generateId(attribute.getIdentifier(), attributeName, maxByteLength);
		}
	}

	private static String generateId(String entityId, String entityName, int maxByteLength)
	{
		String idPart = generateIdentifierIdPart(entityId);
		int maxNamePartLength = maxByteLength - 1 - idPart.length(); // minus one for separator
		String namePart = generateIdentifierNamePart(entityName, maxNamePartLength);
		return namePart + ID_NAME_SEPARATOR + idPart;
	}

	private static String generateIdentifierIdPart(String id)
	{
		return Hashing.crc32().hashString(id, UTF_8).toString();
	}

	private static String generateIdentifierNamePart(String name, int maxByteLength)
	{
		// guarantee that one character is stored in one byte
		String identifierName = PATTERN_WORD_REPLACE.matcher(name).replaceAll("");
		return identifierName.length() > maxByteLength ? identifierName.substring(0, maxByteLength) : identifierName;
	}

	private static void validateMaxByteLength(int maxByteLength)
	{
		if (maxByteLength < 10)
		{
			throw new IllegalArgumentException("Max byte length must be >= 10");
		}
	}
}
