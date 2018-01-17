package org.molgenis.data.postgresql;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.*;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.AttributeUtils;
import org.molgenis.data.support.EntityTypeUtils;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.util.UnexpectedEnumException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.time.ZoneOffset.UTC;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.data.QueryRule.Operator.*;
import static org.molgenis.data.meta.AttributeType.*;
import static org.molgenis.data.postgresql.PostgreSqlNameGenerator.*;
import static org.molgenis.data.postgresql.PostgreSqlQueryGenerator.ColumnMode.INCLUDE_DEFAULT_CONSTRAINT;
import static org.molgenis.data.postgresql.PostgreSqlQueryUtils.*;
import static org.molgenis.data.support.EntityTypeUtils.*;

/**
 * Utility class that generates the SQL used by {@link PostgreSqlRepository} and {@link PostgreSqlRepositoryCollection}
 */
class PostgreSqlQueryGenerator
{
	private static final Logger LOG = LoggerFactory.getLogger(PostgreSqlQueryGenerator.class);

	private static final String UNSPECIFIED_ATTRIBUTE_MSG = "Can't use %s without specifying an attribute";

	static final String ERR_CODE_READONLY_VIOLATION = "23506";

	private PostgreSqlQueryGenerator()
	{

	}

	private static String getSqlConstraintPrimaryKey(EntityType entityType, Attribute attr)
	{
		return "CONSTRAINT " + getPrimaryKeyName(entityType, attr) + " PRIMARY KEY (" + getColumnName(attr) + ')';
	}

	private static String getSqlForeignKey(EntityType entityType, Attribute attr)
	{
		StringBuilder strBuilder = new StringBuilder("CONSTRAINT ").append(getForeignKeyName(entityType, attr))
																   .append(" FOREIGN KEY (")
																   .append(getColumnName(attr))
																   .append(") REFERENCES ")
																   .append(getTableName(attr.getRefEntity()))
																   .append('(')
																   .append(getColumnName(
																		   attr.getRefEntity().getIdAttribute()))
																   .append(')');

		// for self-referencing data or inversed attributes defer checking constraints until the end of the transaction
		if (attr.getRefEntity().getId().equals(entityType.getId()))
		{
			strBuilder.append(" DEFERRABLE INITIALLY DEFERRED");
		}

		return strBuilder.toString();
	}

	private static String getSqlUniqueKey(EntityType entityType, Attribute attr)
	{
		return "CONSTRAINT " + getUniqueKeyName(entityType, attr) + " UNIQUE (" + getColumnName(attr) + ')';
	}

	private static String getSqlCheckConstraint(EntityType entityType, Attribute attr)
	{
		if (attr.getDataType() != ENUM)
		{
			throw new MolgenisDataException(
					format("Check constraint only allowed for attribute type [%s]", ENUM.toString()));
		}

		return "CONSTRAINT " + getCheckConstraintName(entityType, attr) + " CHECK (" + getColumnName(attr) + " IN ("
				+ attr.getEnumOptions().stream().map(enumOption -> '\'' + enumOption + '\'').collect(joining(","))
				+ "))";
	}

	static String getSqlCreateForeignKey(EntityType entityType, Attribute attr)
	{
		return "ALTER TABLE " + getTableName(entityType) + " ADD " + getSqlForeignKey(entityType, attr);
	}

	static String getSqlDropForeignKey(EntityType entityType, Attribute attr)
	{
		return "ALTER TABLE " + getTableName(entityType) + " DROP CONSTRAINT " + getForeignKeyName(entityType, attr);
	}

	static String getSqlCreateUniqueKey(EntityType entityType, Attribute attr)
	{
		return "ALTER TABLE " + getTableName(entityType) + " ADD " + getSqlUniqueKey(entityType, attr);
	}

	static String getSqlDropUniqueKey(EntityType entityType, Attribute attr)
	{
		return "ALTER TABLE " + getTableName(entityType) + " DROP CONSTRAINT " + getUniqueKeyName(entityType, attr);
	}

	static String getSqlCreateCheckConstraint(EntityType entityType, Attribute attr)
	{
		return "ALTER TABLE " + getTableName(entityType) + " ADD " + getSqlCheckConstraint(entityType, attr);
	}

	static String getSqlDropCheckConstraint(EntityType entityType, Attribute attr)
	{
		if (attr.getDataType() != ENUM)
		{
			throw new MolgenisDataException(
					format("Check constraint only allowed for attribute type [%s]", ENUM.toString()));
		}

		return "ALTER TABLE " + getTableName(entityType) + " DROP CONSTRAINT " + getCheckConstraintName(entityType,
				attr);
	}

	static String getSqlSetNotNull(EntityType entityType, Attribute attr)
	{
		return "ALTER TABLE " + getTableName(entityType) + " ALTER COLUMN " + getColumnName(attr) + " SET NOT NULL";
	}

	static String getSqlDropNotNull(EntityType entityType, Attribute attr)
	{
		return "ALTER TABLE " + getTableName(entityType) + " ALTER COLUMN " + getColumnName(attr) + " DROP NOT NULL";
	}

	static String getSqlSetDataType(EntityType entityType, Attribute attr)
	{
		return "ALTER TABLE " + getTableName(entityType) + " ALTER COLUMN " + getColumnName(attr) + " SET DATA TYPE "
				+ getPostgreSqlType(attr) + " USING " + getColumnName(attr) + "::" + getPostgreSqlType(attr);
	}

	/**
	 * Returns SQL string to add a column to an existing table.
	 *
	 * @param entityType entity meta data
	 * @param attr       attribute
	 * @param columnMode column mode
	 * @return SQL string
	 */
	static String getSqlAddColumn(EntityType entityType, Attribute attr, ColumnMode columnMode)
	{
		StringBuilder sql = new StringBuilder("ALTER TABLE ");

		String columnSql = getSqlColumn(entityType, attr, columnMode);
		sql.append(getTableName(entityType)).append(" ADD ").append(columnSql);

		List<String> sqlTableConstraints = getSqlTableConstraints(entityType, attr);
		if (!sqlTableConstraints.isEmpty())
		{
			sqlTableConstraints.forEach(sqlTableConstraint -> sql.append(",ADD ").append(sqlTableConstraint));
		}
		return sql.toString();
	}

	/**
	 * Returns SQL string to remove the default value from an existing column.
	 *
	 * @param entityType entity meta data
	 * @param attr       attribute
	 * @return SQL string
	 */
	static String getSqlDropColumnDefault(EntityType entityType, Attribute attr)
	{
		return "ALTER TABLE " + getTableName(entityType) + " ALTER COLUMN " + getColumnName(attr) + " DROP DEFAULT";
	}

	static String getSqlCreateTable(EntityType entityType)
	{
		List<Attribute> persistedTableAttrs = getTableAttributes(entityType).collect(toList());

		StringBuilder sql = new StringBuilder("CREATE TABLE ").append(getTableName(entityType)).append('(');

		// add columns
		for (Iterator<Attribute> it = persistedTableAttrs.iterator(); it.hasNext(); )
		{
			Attribute attr = it.next();
			sql.append(getSqlColumn(entityType, attr, ColumnMode.EXCLUDE_DEFAULT_CONSTRAINT));

			if (it.hasNext())
			{
				sql.append(',');
			}
		}

		// add table constraints
		for (Attribute persistedTableAttr : persistedTableAttrs)
		{
			List<String> sqlTableConstraints = getSqlTableConstraints(entityType, persistedTableAttr);
			if (!sqlTableConstraints.isEmpty())
			{
				sqlTableConstraints.forEach(sqlTableConstraint -> sql.append(',').append(sqlTableConstraint));
			}
		}

		sql.append(')');

		return sql.toString();
	}

	static String getSqlCreateFunctionValidateUpdate(EntityType entityType, Collection<Attribute> readonlyTableAttrs)
	{
		StringBuilder strBuilder = new StringBuilder(512).append("CREATE FUNCTION ")
														 .append(getFunctionValidateUpdateName(entityType))
														 .append("() RETURNS TRIGGER AS $$\nBEGIN\n");

		String tableName = getTableName(entityType);
		String idColName = getColumnName(entityType.getIdAttribute());
		readonlyTableAttrs.forEach(attr ->
		{
			String colName = getColumnName(attr);

			strBuilder.append("  IF OLD.").append(colName).append(" <> NEW.").append(colName).append(" THEN\n");
			strBuilder.append("    RAISE EXCEPTION 'Updating read-only column ")
					  .append(colName)
					  .append(" of table ")
					  .append(tableName)
					  .append(" with id [%] is not allowed', OLD.")
					  .append(idColName)
					  .append(" USING ERRCODE = '")
					  .append(ERR_CODE_READONLY_VIOLATION)
					  .append("';\n");
			strBuilder.append("  END IF;\n");
		});
		strBuilder.append("  RETURN NEW;\nEND;\n$$ LANGUAGE plpgsql;");

		return strBuilder.toString();
	}

	static String getSqlDropFunctionValidateUpdate(EntityType entityType)
	{
		return "DROP FUNCTION " + getFunctionValidateUpdateName(entityType) + "();";
	}

	static String getSqlCreateUpdateTrigger(EntityType entityType, Collection<Attribute> readonlyTableAttrs)
	{
		StringBuilder strBuilder = new StringBuilder(512).append("CREATE TRIGGER ")
														 .append(getUpdateTriggerName(entityType))
														 .append(" AFTER UPDATE ON ")
														 .append(getTableName(entityType))
														 .append(" FOR EACH ROW WHEN (");
		strBuilder.append(readonlyTableAttrs.stream()
											.map(attr -> "OLD." + getColumnName(attr) + " IS DISTINCT FROM NEW."
													+ getColumnName(attr))
											.collect(joining(" OR ")));
		strBuilder.append(") EXECUTE PROCEDURE ").append(getFunctionValidateUpdateName(entityType)).append("();");
		return strBuilder.toString();
	}

	static String getSqlDropUpdateTrigger(EntityType entityType)
	{
		return "DROP TRIGGER " + getUpdateTriggerName(entityType) + " ON " + getTableName(entityType);
	}

	static String getSqlCreateJunctionTable(EntityType entityType, Attribute attr)
	{
		Attribute idAttr = entityType.getIdAttribute();
		StringBuilder sql = new StringBuilder("CREATE TABLE ").append(getJunctionTableName(entityType, attr))
															  .append(" (")
															  .append(getJunctionTableOrderColumnName())
															  .append(" INT,")
															  .append(getColumnName(idAttr))
															  .append(' ')
															  .append(getPostgreSqlType(idAttr))
															  .append(" NOT NULL, ")
															  .append(getColumnName(attr))
															  .append(' ')
															  .append(getPostgreSqlType(
																	  attr.getRefEntity().getIdAttribute()))
															  .append(" NOT NULL")
															  .append(", FOREIGN KEY (")
															  .append(getColumnName(idAttr))
															  .append(") REFERENCES ")
															  .append(getTableName(entityType))
															  .append('(')
															  .append(getColumnName(idAttr))
															  .append(") ON DELETE CASCADE");

		// for self-referencing data defer checking constraints until the end of the transaction
		if (attr.getRefEntity().getId().equals(entityType.getId()))
		{
			sql.append(" DEFERRABLE INITIALLY DEFERRED");
		}

		if (isPersistedInPostgreSql(attr.getRefEntity()))
		{
			sql.append(", FOREIGN KEY (")
			   .append(getColumnName(attr))
			   .append(") REFERENCES ")
			   .append(getTableName(attr.getRefEntity()))
			   .append('(')
			   .append(getColumnName(attr.getRefEntity().getIdAttribute()))
			   .append(")");

			// for self-referencing data defer checking constraints until the end of the transaction
			if (attr.getRefEntity().getId().equals(entityType.getId()))
			{
				sql.append(" DEFERRABLE INITIALLY DEFERRED");
			}
		}

		AttributeType attrType = attr.getDataType();
		switch (attrType)
		{
			case CATEGORICAL_MREF:
			case MREF:
				sql.append(", UNIQUE (")
				   .append(getColumnName(idAttr))
				   .append(',')
				   .append(getColumnName(attr))
				   .append(')');
				break;
			default:
				throw new RuntimeException(format("Illegal attribute type [%s]", attrType.toString()));
		}
		sql.append(", UNIQUE (")
		   .append(getJunctionTableOrderColumnName())
		   .append(',')
		   .append(getColumnName(idAttr))
		   .append(')');

		sql.append(')');

		return sql.toString();
	}

	static String getSqlCreateJunctionTableIndex(EntityType entityType, Attribute attr)
	{
		Attribute idAttr = entityType.getIdAttribute();
		String junctionTableName = getJunctionTableName(entityType, attr);
		String junctionTableIndexName = getJunctionTableIndexName(entityType, attr, idAttr);
		String idxColumnName = getColumnName(idAttr);
		return "CREATE INDEX " + junctionTableIndexName + " ON " + junctionTableName + " (" + idxColumnName + ')';
	}

	static String getSqlDropJunctionTable(EntityType entityType, Attribute attr)
	{
		return getSqlDropTable(getJunctionTableName(entityType, attr));
	}

	static String getSqlDropTable(EntityType entityType)
	{
		return getSqlDropTable(getTableName(entityType));
	}

	static String getSqlDropColumn(EntityType entityType, Attribute attr)
	{
		return "ALTER TABLE " + getTableName(entityType) + " DROP COLUMN " + getColumnName(attr);
	}

	static String getSqlInsert(EntityType entityType)
	{
		StringBuilder sql = new StringBuilder("INSERT INTO ").append(getTableName(entityType)).append(" (");
		StringBuilder params = new StringBuilder();
		getTableAttributes(entityType).forEach(attr ->
		{
			sql.append(getColumnName(attr)).append(", ");
			params.append("?, ");
		});
		if (sql.charAt(sql.length() - 1) == ' ' && sql.charAt(sql.length() - 2) == ',')
		{
			sql.setLength(sql.length() - 2);
			params.setLength(params.length() - 2);
		}
		sql.append(") VALUES (").append(params).append(')');
		return sql.toString();
	}

	static String getSqlInsertJunction(EntityType entityType, Attribute attr)
	{
		String junctionTableName = getJunctionTableName(entityType, attr);
		return "INSERT INTO " + junctionTableName + " (" + getJunctionTableOrderColumnName() + ',' + getColumnName(
				entityType.getIdAttribute()) + ',' + getColumnName(attr) + ") VALUES (?,?,?)";
	}

	static String getSqlDeleteAll(EntityType entityType)
	{
		return "DELETE FROM " + getTableName(entityType);
	}

	static String getSqlDelete(EntityType entityType)
	{
		return getSqlDelete(getTableName(entityType), entityType.getIdAttribute());
	}

	static String getSqlDelete(String tableName, Attribute attr)
	{
		return "DELETE FROM " + tableName + " WHERE " + getColumnName(attr) + " = ?";
	}

	/**
	 * Returns whether this attribute is stored in the entity table or another table such as a junction table or
	 * referenced entity table.
	 *
	 * @param attr attribute
	 * @return whether this attribute is stored in another table than the entity table
	 */
	private static boolean isPersistedInOtherTable(Attribute attr)
	{
		boolean bidirectionalOneToMany = attr.getDataType() == ONE_TO_MANY && attr.isMappedBy();
		return isMultipleReferenceType(attr) || bidirectionalOneToMany;
	}

	static String getSqlJunctionTableSelect(EntityType entityType, Attribute attr, int numOfIds)
	{
		String idColName = getColumnName(entityType.getIdAttribute());
		String refIdColName = getColumnName(attr);

		return "SELECT " + idColName + "," + getJunctionTableOrderColumnName() + "," + refIdColName + " FROM "
				+ getJunctionTableName(entityType, attr) + " WHERE " + idColName + " in (" + range(0,
				numOfIds).mapToObj(x -> "?").collect(joining(", ")) + ") ORDER BY " + idColName + ","
				+ getJunctionTableOrderColumnName();
	}

	/**
	 * Determines whether a distinct select is required based on a given query.
	 *
	 * @param entityType entity meta data
	 * @param q          query
	 * @param <E>        entity type
	 * @return <code>true</code> if a distinct select is required for SQL queries based on the given query
	 * @throws UnknownAttributeException if query field refers to an attribute that does not exist in entity meta
	 */
	private static <E extends Entity> boolean isDistinctSelectRequired(EntityType entityType, Query<E> q)
	{
		return isDistinctSelectRequiredRec(entityType, q.getRules());
	}

	private static boolean isDistinctSelectRequiredRec(EntityType entityType, List<QueryRule> queryRules)
	{
		if (queryRules.isEmpty())
		{
			return false;
		}
		for (QueryRule queryRule : queryRules)
		{
			if (queryRule.getOperator() == NESTED)
			{
				if (isDistinctSelectRequiredRec(entityType, queryRule.getNestedRules()))
				{
					return true;
				}
			}
			else
			{
				String queryRuleField = queryRule.getField();
				if (queryRuleField != null)
				{
					String attrName = StringUtils.split(queryRuleField, '.')[0];
					Attribute attr = entityType.getAttribute(attrName);
					if (attr == null)
					{
						throw new UnknownAttributeException(entityType, attrName);
					}
					if (isPersistedInOtherTable(attr))
					{
						return true;
					}
				}
			}
		}
		return false;
	}

	static <E extends Entity> String getSqlSelect(EntityType entityType, Query<E> q, List<Object> parameters,
			boolean includeMrefs)
	{
		final StringBuilder select = new StringBuilder("SELECT ");
		if (isDistinctSelectRequired(entityType, q))
		{
			select.append("DISTINCT ");
		}
		final StringBuilder group = new StringBuilder();
		final AtomicInteger count = new AtomicInteger();
		final Attribute idAttribute = entityType.getIdAttribute();
		getPersistedAttributes(entityType).forEach(attr ->
		{
			if (q.getFetch() == null || q.getFetch().hasField(attr.getName()) || (q.getSort() != null && q.getSort()
																										  .hasField(
																												  attr.getName())))
			{
				if (count.get() > 0)
				{
					select.append(", ");
				}

				if (isPersistedInOtherTable(attr))
				{
					if (includeMrefs || (attr.getDataType() == ONE_TO_MANY && attr.isMappedBy()))
					{
						if (attr.getDataType() == ONE_TO_MANY && attr.isMappedBy())
						{
							Attribute refIdAttr = attr.getRefEntity().getIdAttribute();
							String mrefSelect = "(SELECT array_agg(" + getColumnName(refIdAttr);

							Sort orderBy = attr.getOrderBy();
							if (orderBy == null)
							{
								orderBy = new Sort(refIdAttr.getName());
							}

							mrefSelect +=
									' ' + getSqlSort(attr.getRefEntity(), new QueryImpl<>().sort(orderBy)) + ") FROM "
											+ getTableName(attr.getRefEntity()) + " WHERE this." + getColumnName(
											idAttribute) + " = " + getTableName(attr.getRefEntity()) + '.'
											+ getColumnName(attr.getMappedBy()) + ") AS " + getColumnName(attr);
							select.append(mrefSelect);
						}
						else
						{
							// TODO retrieve mref values in separate queries to allow specifying limit and offset after nested MOLGENIS queries are implemented as sub-queries instead of query rules
							String mrefSelect = MessageFormat.format(
									"(SELECT array_agg(DISTINCT ARRAY[{0}.{1}::TEXT,{0}.{0}::TEXT]) "
											+ "FROM {2} AS {0} WHERE this.{3} = {0}.{3}) AS {0}", getColumnName(attr),
									getJunctionTableOrderColumnName(), getJunctionTableName(entityType, attr),
									getColumnName(idAttribute));
							select.append(mrefSelect);
						}
					}
					else
					{
						select.append("NULL AS ").append(getColumnName(attr));
					}
				}
				else
				{
					select.append("this.").append(getColumnName(attr));
					if (group.length() > 0)
					{
						group.append(", this.").append(getColumnName(attr));
					}
					else
					{
						group.append("this.").append(getColumnName(attr));
					}
				}
				count.incrementAndGet();
			}
		});

		// from
		StringBuilder result = new StringBuilder().append(select).append(getSqlFrom(entityType, q));
		// where
		String where = getSqlWhere(entityType, q, parameters, new AtomicInteger());
		if (where.length() > 0)
		{
			result.append(" WHERE ").append(where);
		}
		// order by
		result.append(' ').append(getSqlSort(entityType, q));

		// limit
		if (q.getPageSize() > 0)
		{
			result.append(" LIMIT ").append(q.getPageSize());
		}
		if (q.getOffset() > 0)
		{
			result.append(" OFFSET ").append(q.getOffset());
		}

		return result.toString().trim();
	}

	static String getSqlUpdate(EntityType entityType)
	{
		// use (readonly) identifier
		Attribute idAttribute = entityType.getIdAttribute();

		// create sql
		StringBuilder sql = new StringBuilder("UPDATE ").append(getTableName(entityType)).append(" SET ");
		getTableAttributes(entityType).forEach(attr -> sql.append(getColumnName(attr)).append(" = ?, "));

		if (sql.charAt(sql.length() - 1) == ' ' && sql.charAt(sql.length() - 2) == ',')
		{
			sql.setLength(sql.length() - 2);
		}
		sql.append(" WHERE ").append(getColumnName(idAttribute)).append("= ?");
		return sql.toString();
	}

	/**
	 * Produces SQL to count the number of entities that match the given query. Ignores query offset and pagesize.
	 *
	 * @param q          query
	 * @param parameters prepared statement parameters
	 * @return SQL string
	 */
	static <E extends Entity> String getSqlCount(EntityType entityType, Query<E> q, List<Object> parameters)
	{
		StringBuilder sqlBuilder = new StringBuilder("SELECT COUNT");
		String idAttribute = getColumnName(entityType.getIdAttribute());

		List<QueryRule> queryRules = q.getRules();
		if (queryRules == null || queryRules.isEmpty())
		{
			sqlBuilder.append("(*) FROM ").append(getTableName(entityType));
		}
		else
		{
			boolean distinctSelectRequired = isDistinctSelectRequired(entityType, q);
			if (distinctSelectRequired)
			{
				// distinct count in case query contains one or more rules referring to MREF attributes.
				sqlBuilder.append("(DISTINCT this.").append(idAttribute).append(')');
			}
			else
			{
				sqlBuilder.append("(*)");
			}

			String from = getSqlFrom(entityType, q);
			String where = getSqlWhere(entityType, q, parameters, new AtomicInteger());
			sqlBuilder.append(from).append(" WHERE ").append(where);
		}
		return sqlBuilder.toString();
	}

	private static String getSqlColumn(EntityType entityType, Attribute attr, ColumnMode columnMode)
	{
		StringBuilder sqlBuilder = new StringBuilder(getColumnName(attr)).append(' ');

		AttributeType attrType = attr.getDataType();
		switch (attrType)
		{
			case BOOL:
			case DATE:
			case DATE_TIME:
			case DECIMAL:
			case EMAIL:
			case ENUM:
			case HTML:
			case HYPERLINK:
			case INT:
			case LONG:
			case SCRIPT:
			case STRING:
			case TEXT:
				sqlBuilder.append(getPostgreSqlType(attr));
				break;
			case CATEGORICAL:
			case FILE:
			case XREF:
				sqlBuilder.append(getPostgreSqlType(attr.getRefEntity().getIdAttribute()));
				break;
			case ONE_TO_MANY:
			case COMPOUND:
			case CATEGORICAL_MREF:
			case MREF:
				throw new RuntimeException(format("Illegal attribute type [%s]", attrType.toString()));
			default:
				throw new UnexpectedEnumException(attrType);
		}

		String sqlColumnConstraints = getSqlColumnConstraints(entityType, attr, columnMode);
		if (!sqlColumnConstraints.isEmpty())
		{
			sqlBuilder.append(' ').append(sqlColumnConstraints);
		}
		return sqlBuilder.toString();
	}

	enum ColumnMode
	{
		INCLUDE_DEFAULT_CONSTRAINT, EXCLUDE_DEFAULT_CONSTRAINT
	}

	static boolean generateSqlColumnDefaultConstraint(Attribute attr)
	{
		return attr.getDefaultValue() != null && !EntityTypeUtils.isMultipleReferenceType(attr);
	}

	/**
	 * Generates column constraint SQL, e.g. 'NOT NULL DEFAULT 123'
	 *
	 * @param entityType            entity type
	 * @param attr                  attribute
	 * @param columnConstraintsMode whether or not to add default constraint to generated SQL
	 * @return column constraint SQL
	 */
	private static String getSqlColumnConstraints(EntityType entityType, Attribute attr,
			ColumnMode columnConstraintsMode)
	{
		StringBuilder sqlBuilder = new StringBuilder();
		if (!attr.getName().equals(entityType.getIdAttribute().getName()))
		{
			if (!attr.isNillable())
			{
				sqlBuilder.append("NOT NULL");
			}
		}
		if (columnConstraintsMode == INCLUDE_DEFAULT_CONSTRAINT && generateSqlColumnDefaultConstraint(attr))
		{
			if (sqlBuilder.length() > 0)
			{
				sqlBuilder.append(' ');
			}
			sqlBuilder.append("DEFAULT ").append(getSqlDefaulValue(attr));
		}
		return sqlBuilder.toString();
	}

	private static String getSqlDefaulValue(Attribute attribute)
	{
		return getSqlDefaulValue(attribute, attribute.getDefaultValue());
	}

	private static String getSqlDefaulValue(Attribute attribute, String defaultValueAsString)
	{
		String sqlDefaultValue;

		Object defaultTypedValue = AttributeUtils.getDefaultTypedValue(attribute, defaultValueAsString);

		AttributeType attributeType = attribute.getDataType();
		switch (attributeType)
		{
			case BOOL:
				Boolean booleanDefaultValue = (Boolean) defaultTypedValue;
				sqlDefaultValue = booleanDefaultValue ? "TRUE" : "FALSE";
				break;
			case CATEGORICAL:
			case FILE:
			case XREF:
				Entity refDefaultValue = (Entity) defaultTypedValue;
				sqlDefaultValue = getSqlDefaulValue(attribute.getRefEntity().getIdAttribute(),
						refDefaultValue.getIdValue().toString());
				break;
			case DATE:
				LocalDate dateDefaultValue = (LocalDate) defaultTypedValue;
				sqlDefaultValue = '\'' + dateDefaultValue.toString() + '\'';
				break;
			case DATE_TIME:
				Instant instantDefaultValue = (Instant) defaultTypedValue;
				// As a workaround for #5674, we don't store milliseconds
				sqlDefaultValue =
						'\'' + instantDefaultValue.truncatedTo(ChronoUnit.SECONDS).atOffset(UTC).toString() + '\'';
				break;
			case DECIMAL:
				Double doubleDefaultValue = (Double) defaultTypedValue;
				sqlDefaultValue = doubleDefaultValue.toString();
				break;
			case EMAIL:
			case ENUM:
			case HTML:
			case HYPERLINK:
			case SCRIPT:
			case STRING:
			case TEXT:
				sqlDefaultValue = '\'' + (String) defaultTypedValue + '\'';
				break;
			case INT:
				Integer intDefaultValue = (Integer) defaultTypedValue;
				sqlDefaultValue = intDefaultValue.toString();
				break;
			case LONG:
				Long longDefaultValue = (Long) defaultTypedValue;
				sqlDefaultValue = longDefaultValue.toString();
				break;
			case CATEGORICAL_MREF:
			case COMPOUND:
			case MREF:
			case ONE_TO_MANY:
				throw new RuntimeException(format("Illegal attribute type [%s]", attributeType.toString()));
			default:
				throw new UnexpectedEnumException(attributeType);
		}

		return sqlDefaultValue;
	}

	private static List<String> getSqlTableConstraints(EntityType entityType, Attribute attr)
	{
		List<String> tableConstraints = Lists.newArrayList();

		if (attr.getName().equals(entityType.getIdAttribute().getName()))
		{
			tableConstraints.add(getSqlConstraintPrimaryKey(entityType, attr));
		}
		else
		{
			if (isSingleReferenceType(attr) && isPersistedInPostgreSql(attr.getRefEntity()))
			{
				tableConstraints.add(getSqlForeignKey(entityType, attr));
			}
			if (attr.isUnique())
			{
				tableConstraints.add(getSqlUniqueKey(entityType, attr));
			}
			if (attr.getDataType() == ENUM)
			{
				tableConstraints.add(getSqlCheckConstraint(entityType, attr));
			}
		}

		return tableConstraints;
	}

	private static String getSqlDropTable(String tableName)
	{
		return "DROP TABLE " + tableName;
	}

	static <E extends Entity> String getSqlWhere(EntityType entityType, Query<E> q, List<Object> parameters,
			AtomicInteger mrefFilterIndex)
	{
		StringBuilder result = new StringBuilder();
		for (QueryRule r : q.getRules())
		{
			Attribute attr = null;
			if (r.getField() != null)
			{
				attr = entityType.getAttribute(r.getField());
				if (attr == null)
				{
					throw new MolgenisDataException(format("Unknown attribute [%s]", r.getField()));
				}
				if (isPersistedInOtherTable(attr))
				{
					mrefFilterIndex.incrementAndGet();
				}
			}

			StringBuilder predicate = new StringBuilder();
			Operator operator = r.getOperator();
			switch (operator)
			{
				case AND:
					result.append(" AND ");
					break;
				case NESTED:
					QueryImpl<Entity> nestedQ = new QueryImpl<>(r.getNestedRules());
					result.append('(')
						  .append(getSqlWhere(entityType, nestedQ, parameters, mrefFilterIndex))
						  .append(')');
					break;
				case OR:
					result.append(" OR ");
					break;
				case LIKE:
					requireNonNull(attr, format(UNSPECIFIED_ATTRIBUTE_MSG, LIKE));
					String columnName;
					if (isPersistedInOtherTable(attr))
					{
						columnName = getFilterColumnName(attr, mrefFilterIndex.get());
					}
					else
					{
						columnName = "this." + getColumnName(attr);
					}

					if (isStringType(attr) || isTextType(attr))
					{
						result.append(' ').append(columnName);
					}
					else
					{
						result.append(" CAST(").append(columnName).append(" as TEXT)");
					}

					result.append(" LIKE ?");
					parameters.add("%" + PostgreSqlUtils.getPostgreSqlQueryValue(r.getValue(), attr) + '%');
					break;
				case IN:
				{
					requireNonNull(attr, format(UNSPECIFIED_ATTRIBUTE_MSG, IN));
					Object inValue = r.getValue();
					if (inValue == null)
					{
						throw new MolgenisDataException("Missing value for IN query");
					}
					if (!(inValue instanceof Iterable<?>))
					{
						throw new MolgenisDataException(format("IN value is of type [%s] instead of [Iterable]",
								inValue.getClass().getSimpleName()));
					}

					StringBuilder in = new StringBuilder();
					Attribute inAttr = attr;
					Stream<Object> postgreSqlIds = stream(((Iterable<?>) inValue).spliterator(), false).map(
							idValue -> PostgreSqlUtils.getPostgreSqlQueryValue(idValue, inAttr));
					for (Iterator<Object> it = postgreSqlIds.iterator(); it.hasNext(); )
					{
						Object postgreSqlId = it.next();
						in.append('?');
						if (it.hasNext())
						{
							in.append(',');
						}
						parameters.add(postgreSqlId);
					}

					if (isPersistedInOtherTable(attr))
					{
						result.append(getFilterColumnName(attr, mrefFilterIndex.get()));
					}
					else
					{
						result.append("this");
					}

					Attribute equalsAttr;
					if (attr.isMappedBy())
					{
						equalsAttr = attr.getRefEntity().getIdAttribute();
					}
					else
					{
						equalsAttr = entityType.getAttribute(r.getField());
					}
					result.append('.').append(getColumnName(equalsAttr));
					result.append(" IN (").append(in).append(')');
					break;
				}
				case NOT:
					result.append(" NOT ");
					break;
				case RANGE:
					requireNonNull(attr, format(UNSPECIFIED_ATTRIBUTE_MSG, RANGE));
					Object range = r.getValue();
					if (range == null)
					{
						throw new MolgenisDataException("Missing value for RANGE query");
					}
					if (!(range instanceof Iterable<?>))
					{
						throw new MolgenisDataException(format("RANGE value is of type [%s] instead of [Iterable]",
								range.getClass().getSimpleName()));
					}
					Iterator<?> rangeValues = ((Iterable<?>) range).iterator();
					parameters.add(rangeValues.next()); // from
					parameters.add(rangeValues.next()); // to

					StringBuilder column = new StringBuilder();
					if (isPersistedInOtherTable(attr))
					{
						column.append(getFilterColumnName(attr, mrefFilterIndex.get()));
					}
					else
					{
						column.append("this");
					}
					column.append('.').append(getColumnName(entityType.getAttribute(r.getField())));
					predicate.append(column).append(" >= ? AND ").append(column).append(" <= ?");
					result.append(predicate);
					break;
				case EQUALS:
					if (attr == null)
					{
						throw new MolgenisDataException("Missing attribute field in EQUALS query rule");
					}

					if (isPersistedInOtherTable(attr))
					{
						predicate.append(getFilterColumnName(attr, mrefFilterIndex.get()));
					}
					else
					{
						predicate.append("this");
					}

					Attribute equalsAttr;
					if (attr.isMappedBy())
					{
						equalsAttr = attr.getRefEntity().getIdAttribute();
					}
					else
					{
						equalsAttr = entityType.getAttribute(r.getField());
					}
					predicate.append('.').append(getColumnName(equalsAttr));
					if (r.getValue() == null)
					{
						// expression = null is not valid, use IS NULL
						predicate.append(" IS NULL ");
					}
					else
					{
						Object postgreSqlVal = PostgreSqlUtils.getPostgreSqlQueryValue(r.getValue(), attr);
						//Postgres does not return the rows with an empty value in a boolean field when queried with for example "... NOT abstract = TRUE"
						//It does however return those rows when queried with "... NOT abstract IS TRUE"
						if (attr.getDataType() == BOOL)
						{
							Boolean bool = (Boolean) postgreSqlVal;
							//noinspection ConstantConditions (getPostgreSqlQueryValue() != null if r.getValue() != null)
							if (bool) predicate.append(" IS TRUE");
							else predicate.append(" IS FALSE");
						}
						else
						{
							predicate.append(" =");
							predicate.append(" ? ");

							parameters.add(postgreSqlVal);
						}
					}
					if (result.length() > 0 && !result.toString().endsWith(" OR ") && !result.toString()
																							 .endsWith(" AND ")
							&& !result.toString().endsWith(" NOT "))
					{
						result.append(" AND ");
					}
					result.append(predicate);
					break;
				case GREATER:
				case GREATER_EQUAL:
				case LESS:
				case LESS_EQUAL:
					requireNonNull(attr, format(UNSPECIFIED_ATTRIBUTE_MSG,
							format("%s, %s, %s or %s", GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)));
					if (isPersistedInOtherTable(attr))
					{
						predicate.append(getFilterColumnName(attr, mrefFilterIndex.get()));
					}
					else
					{
						predicate.append("this");
					}

					predicate.append('.').append(getColumnName(entityType.getAttribute(r.getField())));
					switch (operator)
					{
						case GREATER:
							predicate.append(" >");
							break;
						case GREATER_EQUAL:
							predicate.append(" >=");
							break;
						case LESS:
							predicate.append(" <");
							break;
						case LESS_EQUAL:
							predicate.append(" <=");
							break;
						// $CASES-OMITTED$
						default:
							throw new RuntimeException(format("Unexpected query operator [%s]", operator));
					}
					predicate.append(" ? ");

					parameters.add(PostgreSqlUtils.getPostgreSqlQueryValue(r.getValue(), attr));

					if (result.length() > 0 && !result.toString().endsWith(" OR ") && !result.toString()
																							 .endsWith(" AND ")
							&& !result.toString().endsWith(" NOT "))
					{
						result.append(" AND ");
					}
					result.append(predicate);
					break;
				case DIS_MAX:
				case FUZZY_MATCH:
				case FUZZY_MATCH_NGRAM:
				case SEARCH:
				case SHOULD:
					// PostgreSQL does not support semantic searching and sorting matching rows on relevance.
					throw new UnsupportedOperationException(
							format("Query operator [%s] not supported by PostgreSQL repository", operator.toString()));
				default:
					throw new UnexpectedEnumException(operator);
			}
		}

		return result.toString().trim();
	}

	/**
	 * Package-private for testability
	 */
	static <E extends Entity> String getSqlSort(EntityType entityType, Query<E> q)
	{
		StringBuilder sortSql = new StringBuilder();

		// https://www.postgresql.org/docs/9.6/static/queries-limit.html
		// When using LIMIT, it is important to use an ORDER BY clause that constrains the result rows into a unique order.
		// Otherwise you will get an unpredictable subset of the query's rows. You might be asking for the tenth through twentieth rows,
		// but tenth through twentieth in what ordering? The ordering is unknown, unless you specified ORDER BY.
		Sort sort;
		if (q.getSort() != null && !hasUniqueSortAttribute(entityType, q.getSort()))
		{
			LOG.debug("Query with sort without unique attribute detected: {}", q);
			sort = new Sort(q.getSort());
			sort.on(entityType.getIdAttribute().getName());
		}
		else if (q.getSort() == null)
		{
			LOG.debug("Query without sort detected: {}", q);
			sort = new Sort(entityType.getIdAttribute().getName());
		}
		else
		{
			sort = q.getSort();
		}

		for (Sort.Order o : sort)
		{
			Attribute attr = entityType.getAttribute(o.getAttr());
			sortSql.append(", ").append(getColumnName(attr));
			if (o.getDirection().equals(Sort.Direction.DESC))
			{
				sortSql.append(" DESC");
			}
			else
			{
				sortSql.append(" ASC");
			}
		}

		if (sortSql.length() > 0)
		{
			sortSql = new StringBuilder("ORDER BY ").append(sortSql.substring(2));
		}

		return sortSql.toString();
	}

	private static boolean hasUniqueSortAttribute(EntityType entityType, Sort sort)
	{
		for (Sort.Order order : sort)
		{
			String attributeName = order.getAttr();
			Attribute attribute = entityType.getAttribute(attributeName);
			if (attribute.isUnique())
			{
				return true;
			}
		}
		return false;
	}

	private static <E extends Entity> String getSqlFrom(EntityType entityType, Query<E> q)
	{
		List<Attribute> mrefAttrsInQuery = getJoinQueryAttrs(entityType, q);
		StringBuilder from = new StringBuilder(" FROM ").append(getTableName(entityType)).append(" AS this");

		Attribute idAttribute = entityType.getIdAttribute();

		for (int i = 0; i < mrefAttrsInQuery.size(); i++)
		{
			// extra join so we can filter on the mrefs
			Attribute mrefAttr = mrefAttrsInQuery.get(i);

			if (mrefAttr.getDataType() == ONE_TO_MANY && mrefAttr.isMappedBy())
			{
				// query table of referenced entity
				from.append(" LEFT JOIN ")
					.append(getTableName(mrefAttr.getRefEntity()))
					.append(" AS ")
					.append(getFilterColumnName(mrefAttr, i + 1))
					.append(" ON (this.")
					.append(getColumnName(idAttribute))
					.append(" = ")
					.append(getFilterColumnName(mrefAttr, i + 1))
					.append('.')
					.append(getColumnName(mrefAttr.getMappedBy()))
					.append(')');
			}
			else
			{
				// query junction table
				from.append(" LEFT JOIN ")
					.append(getJunctionTableName(entityType, mrefAttr))
					.append(" AS ")
					.append(getFilterColumnName(mrefAttr, i + 1))
					.append(" ON (this.")
					.append(getColumnName(idAttribute))
					.append(" = ")
					.append(getFilterColumnName(mrefAttr, i + 1))
					.append('.')
					.append(getColumnName(idAttribute))
					.append(')');
			}
		}

		return from.toString();
	}

	private static <E extends Entity> List<Attribute> getJoinQueryAttrs(EntityType entityType, Query<E> q)
	{
		List<Attribute> joinAttrs = Lists.newArrayList();
		getJoinQueryAttrsRec(entityType, q.getRules(), joinAttrs);
		return joinAttrs;
	}

	private static void getJoinQueryAttrsRec(EntityType entityType, List<QueryRule> rules, List<Attribute> joinAttrs)
	{
		for (QueryRule rule : rules)
		{
			if (rule.getField() != null)
			{
				Attribute attr = entityType.getAttribute(rule.getField());
				if (attr != null && isPersistedInOtherTable(attr))
				{
					joinAttrs.add(attr);
				}
			}

			if (rule.getNestedRules() != null && !rule.getNestedRules().isEmpty())
			{
				getJoinQueryAttrsRec(entityType, rule.getNestedRules(), joinAttrs);
			}
		}
	}

	private static String getPostgreSqlType(Attribute attr)
	{
		while (true)
		{
			AttributeType attrType = attr.getDataType();
			switch (attrType)
			{
				case BOOL:
					return "boolean";
				case CATEGORICAL:
				case XREF:
				case FILE:
					attr = attr.getRefEntity().getIdAttribute();
					continue;
				case DATE:
					return "date";
				case DATE_TIME:
					return "timestamp with time zone"; // this matters when converting from STRING to DATE_TIME!
				case DECIMAL:
					return "double precision"; // alias: float8
				case EMAIL:
				case ENUM:
				case HYPERLINK:
				case STRING:
					return "character varying(255)"; // alias: varchar(255)
				case HTML:
				case SCRIPT:
				case TEXT:
					return "text";
				case INT:
					return "integer"; // alias: int, int4
				case LONG:
					return "bigint"; // alias: int8
				case CATEGORICAL_MREF:
				case MREF:
				case ONE_TO_MANY:
				case COMPOUND:
					throw new RuntimeException(format("Illegal attribute type [%s]", attrType.toString()));
				default:
					throw new UnexpectedEnumException(attrType);
			}
		}
	}
}

