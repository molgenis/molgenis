package org.molgenis.data.postgresql;

import com.google.common.hash.Hashing;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;

import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;

class PostgreSqlIdentifierGenerator
{
	private static Pattern NO_LETTER_NO_DIGIT_PATTERN = Pattern.compile("[^A-Za-z0-9]");

	/**
	 * Maximum identifier length in bytes.
	 * <p>
	 * https://www.postgresql.org/docs/current/static/sql-syntax-lexical.html#SQL-SYNTAX-IDENTIFIERS
	 */
	private static int MAX_IDENTIFIER_LENGTH = 63;

	private PostgreSqlIdentifierGenerator()
	{
	}

	static String generateTableIdentifier(EntityType entityType)
	{
		return generateTableIdentifier(entityType, MAX_IDENTIFIER_LENGTH);
	}

	static String generateJunctionTableIdentifier(EntityType entityType, Attribute attr)
	{
		String entityIdNamePart = generateTableIdentifier(entityType, (MAX_IDENTIFIER_LENGTH - 1) / 2);

		String attrIdPart = generateIdentifierId(attr.getIdentifier());
		int maxAttrNamePartLength = ((MAX_IDENTIFIER_LENGTH - 1) / 2) - attrIdPart.length();
		String attrNamePart = generateIdentifierName(attr.getName(), maxAttrNamePartLength);

		return entityIdNamePart + '_' + attrNamePart + '#' + attrIdPart;
	}

	private static String generateTableIdentifier(EntityType entityType, int maxLength)
	{
		String idPart = generateIdentifierId(entityType.getId());
		int maxNamePartLength = maxLength - 1 - idPart.length(); // minus one for separator
		String namePart = generateIdentifierName(entityType.getName(), maxNamePartLength);
		return namePart + '#' + idPart;
	}

	private static String generateIdentifierId(String id)
	{
		return Hashing.crc32().hashString(id, UTF_8).toString();
	}

	private static String generateIdentifierName(String name, int maxLength)
	{
		// guarantee that one character is stored in one byte
		String identifierName = NO_LETTER_NO_DIGIT_PATTERN.matcher(name).replaceAll("");
		return identifierName.length() > maxLength ? identifierName.substring(0, maxLength) : identifierName;
	}
}
