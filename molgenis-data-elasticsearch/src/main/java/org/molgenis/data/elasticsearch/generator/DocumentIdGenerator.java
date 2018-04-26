package org.molgenis.data.elasticsearch.generator;

import org.molgenis.data.meta.AbstractMetadataIdGenerator;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Generator for Elasticsearch document type names and document field names.
 */
@Component
public class DocumentIdGenerator extends AbstractMetadataIdGenerator
{
	/**
	 * Unknown whether Elasticsearch document type names or field names have a length limit. Limit length to be on the
	 * safe side.
	 */
	private static final int MAX_IDENTIFIER_LENGTH = 32;

	/**
	 * Document types cannot start with '_' and cannot contain '#', ',' and '.'. Stick to alphanumeric characters to
	 * be on the safe side.
	 */
	private static final Pattern PATTERN_ALPHANUMERIC_REPLACE = Pattern.compile("[^a-zA-Z0-9]");

	/**
	 * Separator between identifier and name part in generated identifiers to improve readability.
	 */
	private static final char SEPARATOR = '_';

	/**
	 * Generates a document type name for the given entity type
	 *
	 * @param entityType entity type
	 * @return human readable document type name (unique within the system)
	 */
	@Override
	public String generateId(EntityType entityType)
	{
		String idHash = generateHashcode(entityType.getId());
		String truncatedId = truncateName(cleanName(entityType.getId())).toLowerCase();
		return truncatedId + SEPARATOR + idHash;
	}

	/**
	 * Generates a field name for the given entity type
	 *
	 * @param attribute attribute
	 * @return human readable field name (unique within the system)
	 */
	@Override
	public String generateId(Attribute attribute)
	{
		String idPart = generateHashcode(attribute.getEntity().getId() + attribute.getIdentifier());
		String namePart = truncateName(cleanName(attribute.getName()));
		return namePart + SEPARATOR + idPart;
	}

	private String cleanName(String name)
	{
		return PATTERN_ALPHANUMERIC_REPLACE.matcher(name).replaceAll("");
	}

	private String truncateName(String name)
	{
		int maxNameLength = MAX_IDENTIFIER_LENGTH - 8 - 1; // -8 for identifier part, -1 for separator
		return name.length() < maxNameLength ? name : name.substring(0, maxNameLength);
	}
}
