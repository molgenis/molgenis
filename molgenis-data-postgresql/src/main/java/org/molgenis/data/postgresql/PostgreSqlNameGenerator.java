package org.molgenis.data.postgresql;

import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.postgresql.identifier.Identifiable;

public class PostgreSqlNameGenerator
{
	static final String JUNCTION_TABLE_ORDER_ATTR_NAME = "order";

	/**
	 * Maximum identifier length in bytes.
	 * <p>
	 * https://www.postgresql.org/docs/current/static/sql-syntax-lexical.html#SQL-SYNTAX-IDENTIFIERS
	 */
	private static int MAX_IDENTIFIER_BYTE_LENGTH = 63;

	private PostgreSqlNameGenerator()
	{
	}

	/**
	 * Returns the double-quoted table name based on entity name
	 *
	 * @param entityTypeId entity type ID
	 * @return table name for this entity
	 */
	static String getTableName(String entityTypeId)
	{
		return getTableName(entityTypeId, true);
	}

	/**
	 * Returns the table name based on entity name
	 *
	 * @param entityTypeId entity type ID
	 * @return PostgreSQL table name
	 */
	public static String getTableName(String entityTypeId, boolean quotedIdentifier)
	{
		return getTableName(entityTypeId, quotedIdentifier, MAX_IDENTIFIER_BYTE_LENGTH);
	}

	private static String getTableName(String entityTypeId, boolean quotedIdentifier, int maxLength)
	{
		String identifier = new PostgreSqlIdGenerator(maxLength).generateEntityTypeId(entityTypeId);
		return quotedIdentifier ? getQuotedIdentifier(identifier) : identifier;
	}

	static String getJunctionTableName(EntityType entityType, Attribute attr)
	{
		return getJunctionTableName(entityType.getId(), Identifiable.create(attr), true);
	}

	/**
	 * Returns the junction table name for the given attribute of the given entity
	 *
	 * @param entityTypeId  ID of entitytype that owns the attribute
	 * @param attribute     the attribute
	 * @return PostgreSQL junction table name
	 */
	public static String getJunctionTableName(String entityTypeId, Identifiable attribute, boolean quotedIdentifier)
	{
		int nrAdditionalChars = 1;
		String entityPart = generateEntityTypeId(entityTypeId, (MAX_IDENTIFIER_BYTE_LENGTH - nrAdditionalChars) / 2);
		String attrPart = generateAttributeId(attribute, (MAX_IDENTIFIER_BYTE_LENGTH - nrAdditionalChars) / 2);
		return quotedIdentifier ? getQuotedIdentifier(entityPart + '_' + attrPart) : entityPart + '_' + attrPart;
	}

	/**
	 * Returns the junction table index name for the given indexed attribute in a junction table
	 *
	 * @param entityType entity meta data
	 * @param attr       attribute
	 * @param idxAttr    indexed attribute
	 * @return PostgreSQL junction table index name
	 */
	static String getJunctionTableIndexName(EntityType entityType, Attribute attr, Attribute idxAttr)
	{
		String indexNamePostfix = "_idx";
		int nrAdditionalChars = 1 + indexNamePostfix.length();
		String entityPart = generateEntityTypeId(entityType.getId(),
				(MAX_IDENTIFIER_BYTE_LENGTH - nrAdditionalChars) / 3);
		String attrPart = generateAttributeId(attr, (MAX_IDENTIFIER_BYTE_LENGTH - nrAdditionalChars) / 3);
		String idxAttrPart = generateAttributeId(idxAttr, (MAX_IDENTIFIER_BYTE_LENGTH - nrAdditionalChars) / 3);
		return getQuotedIdentifier(entityPart + '_' + attrPart + '_' + idxAttrPart + indexNamePostfix);
	}

	static String getColumnName(Identifiable attr)
	{
		return getColumnName(attr, true);
	}

	/**
	 * @deprecated
	 */
	public static String getColumnName(Attribute attribute)
	{
		return getColumnName(attribute, true);
	}

	/**
	 * @deprecated
	 */
	public static String getColumnName(Attribute attribute, boolean quotedIdentifier)
	{
		return getColumnName(Identifiable.create(attribute), quotedIdentifier);
	}

	public static String getColumnName(Identifiable attr, boolean quotedIdentifier)
	{
		String identifier = generateAttributeId(attr, MAX_IDENTIFIER_BYTE_LENGTH);
		return quotedIdentifier ? getQuotedIdentifier(identifier) : identifier;
	}

	static String getJunctionTableOrderColumnName()
	{
		return getQuotedIdentifier(JUNCTION_TABLE_ORDER_ATTR_NAME);
	}

	static String getFilterColumnName(Attribute attr, int filterIndex)
	{
		String filterPostfix = "filter" + filterIndex;
		int nrAdditionalChars = 1 + filterPostfix.length();
		String attrId = generateAttributeId(attr, MAX_IDENTIFIER_BYTE_LENGTH - nrAdditionalChars);
		return getQuotedIdentifier(attrId + '_' + filterPostfix);
	}

	static String getPrimaryKeyName(EntityType entityType, Attribute attr)
	{
		return getConstraintName(entityType, attr, "pkey");
	}

	static String getForeignKeyName(EntityType entityType, Attribute attr)
	{
		return getConstraintName(entityType, attr, "fkey");
	}

	static String getUniqueKeyName(EntityType entityType, Attribute attr)
	{
		return getConstraintName(entityType, attr, "key");
	}

	static String getCheckConstraintName(EntityType entityType, Attribute attr)
	{
		return getConstraintName(entityType, attr, "chk");
	}

	private static String getConstraintName(EntityType entityType, Attribute attr, String constraintPostfix)
	{
		int nrAdditionalChars = 2 + constraintPostfix.length();
		String entityPart = generateEntityTypeId(entityType.getId(),
				(MAX_IDENTIFIER_BYTE_LENGTH - nrAdditionalChars) / 2);
		String attrPart = generateAttributeId(attr, (MAX_IDENTIFIER_BYTE_LENGTH - nrAdditionalChars) / 2);
		return getQuotedIdentifier(entityPart + '_' + attrPart + '_' + constraintPostfix);
	}

	static String getFunctionValidateUpdateName(EntityType entityType)
	{
		String prefix = "validate_update";
		int maxLength = MAX_IDENTIFIER_BYTE_LENGTH - prefix.length() - 1;
		return getQuotedIdentifier(prefix + '_' + getTableName(entityType.getId(), false, maxLength));
	}

	static String getUpdateTriggerName(EntityType entityType)
	{
		String prefix = "update_trigger";
		int maxLength = MAX_IDENTIFIER_BYTE_LENGTH - prefix.length() - 1;
		String identifier = prefix + '_' + getTableName(entityType.getId(), false, maxLength);
		return getQuotedIdentifier(identifier);
	}

	private static String getQuotedIdentifier(String identifier)
	{
		return '"' + identifier + '"';
	}

	private static String generateEntityTypeId(String entityTypeId, int maxByteLength)
	{
		return new PostgreSqlIdGenerator(maxByteLength).generateEntityTypeId(entityTypeId);
	}

	private static String generateAttributeId(Identifiable identifiable, int maxByteLength)
	{
		return new PostgreSqlIdGenerator(maxByteLength).generateAttributeId(identifiable);
	}

	private static String generateAttributeId(Attribute attr, int maxByteLength)
	{
		return generateAttributeId(Identifiable.create(attr.getName(), attr.getIdentifier()), maxByteLength);
	}
}
