package org.molgenis.data.postgresql;

import org.molgenis.data.meta.AbstractMetadataIdGenerator;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;

import java.util.regex.Pattern;

/**
 * Generator for PostgreSQL table, column, key, trigger etc. identifiers.
 */
public class PostgreSqlIdGenerator extends AbstractMetadataIdGenerator
{
	private static final Pattern PATTERN_WORD = Pattern.compile("\\w*");
	private static final Pattern PATTERN_WORD_REPLACE = Pattern.compile("\\W");

	/**
	 * Separator between identifier and name part in generated identifiers to improve readability.
	 */
	private static final char SEPARATOR = '#';

	private final int maxIdentifierByteLength;

	PostgreSqlIdGenerator(int maxIdentifierByteLength)
	{
		if (maxIdentifierByteLength < 10)
		{
			throw new IllegalArgumentException("Max identifier byte length must be >= 10");
		}
		this.maxIdentifierByteLength = maxIdentifierByteLength;
	}

	@Override
	public String generateId(EntityType entityType)
	{
		String idHash = generateHashcode(entityType.getId());
		String truncatedId = truncateName(cleanName(entityType.getId()));
		return truncatedId + SEPARATOR + idHash;
	}

	@Override
	public String generateId(Attribute attribute)
	{
		String attrName = attribute.getName();
		if (PATTERN_WORD.matcher(attrName).matches() && attrName.length() <= maxIdentifierByteLength)
		{
			return attrName;
		}
		else
		{
			String idPart = generateHashcode(attribute.getIdentifier());
			String namePart = truncateName(cleanName(attribute.getName()));
			return namePart + SEPARATOR + idPart;
		}
	}

	private String cleanName(String name)
	{
		return PATTERN_WORD_REPLACE.matcher(name).replaceAll("");
	}

	private String truncateName(String name)
	{
		int maxNameLength = maxIdentifierByteLength - 8 - 1; // -8 for identifier part, -1 for separator
		return name.length() < maxNameLength ? name : name.substring(0, maxNameLength);
	}
}
