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

	public static String generateId(EntityType entityType, int maxByteLength)
	{
		return generateId(entityType.getId(), entityType.getSimpleName(), maxByteLength);
	}

	public static String generateId(Attribute attribute, int maxByteLength)
	{
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
}
