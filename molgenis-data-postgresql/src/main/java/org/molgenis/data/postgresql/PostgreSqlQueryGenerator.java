package org.molgenis.data.postgresql;

import com.google.common.collect.Lists;
import org.molgenis.MolgenisFieldTypes.AttributeType;
import org.molgenis.data.*;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.support.QueryImpl;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.MolgenisFieldTypes.AttributeType.BOOL;
import static org.molgenis.MolgenisFieldTypes.AttributeType.ENUM;
import static org.molgenis.data.postgresql.PostgreSqlQueryUtils.*;
import static org.molgenis.data.support.EntityMetaDataUtils.*;

/**
 * Utility class that generates the SQL used by {@link PostgreSqlRepository} and {@link PostgreSqlRepositoryCollection}
 */
class PostgreSqlQueryGenerator
{
	private PostgreSqlQueryGenerator()
	{

	}

	private static String getSqlConstraintPrimaryKey(EntityMetaData entityMeta, Attribute attr)
	{
		return "CONSTRAINT " + getPrimaryKeyName(entityMeta, attr) + " PRIMARY KEY (" + getColumnName(attr) + ')';
	}

	private static String getSqlForeignKey(EntityMetaData entityMeta, Attribute attr)
	{
		StringBuilder strBuilder = new StringBuilder("CONSTRAINT ").append(getForeignKeyName(entityMeta, attr))
				.append(" FOREIGN KEY (").append(getColumnName(attr)).append(") REFERENCES ")
				.append(getTableName(attr.getRefEntity())).append('(')
				.append(getColumnName(attr.getRefEntity().getIdAttribute())).append(')');

		// for self-referencing data defer checking constraints until the end of the transaction
		if (attr.getRefEntity().getName().equals(entityMeta.getName()))
		{
			strBuilder.append(" DEFERRABLE INITIALLY DEFERRED");
		}
		return strBuilder.toString();
	}

	private static String getSqlUniqueKey(EntityMetaData entityMeta, Attribute attr)
	{
		return "CONSTRAINT " + getUniqueKeyName(entityMeta, attr) + " UNIQUE (" + getColumnName(attr) + ')';
	}

	private static String getSqlCheckConstraint(EntityMetaData entityMeta, Attribute attr)
	{
		if (attr.getDataType() != ENUM)
		{
			throw new MolgenisDataException(
					format("Check constraint only allowed for attribute type [%s]", ENUM.toString()));
		}

		return "CONSTRAINT " + getCheckConstraintName(entityMeta, attr) + " CHECK (" + getColumnName(attr) + " IN ("
				+ attr.getEnumOptions().stream().map(enumOption -> '\'' + enumOption + '\'').collect(joining(","))
				+ "))";
	}

	static String getSqlCreateForeignKey(EntityMetaData entityMeta, Attribute attr)
	{
		return "ALTER TABLE " + getTableName(entityMeta) + " ADD " + getSqlForeignKey(entityMeta, attr);
	}

	static String getSqlDropForeignKey(EntityMetaData entityMeta, Attribute attr)
	{
		return "ALTER TABLE " + getTableName(entityMeta) + " DROP CONSTRAINT " + getForeignKeyName(entityMeta, attr);
	}

	static String getSqlCreateUniqueKey(EntityMetaData entityMeta, Attribute attr)
	{
		return "ALTER TABLE " + getTableName(entityMeta) + " ADD " + getSqlUniqueKey(entityMeta, attr);
	}

	static String getSqlDropUniqueKey(EntityMetaData entityMeta, Attribute attr)
	{
		return "ALTER TABLE " + getTableName(entityMeta) + " DROP CONSTRAINT " + getUniqueKeyName(entityMeta, attr);
	}

	static String getSqlCreateCheckConstraint(EntityMetaData entityMeta, Attribute attr)
	{
		return "ALTER TABLE " + getTableName(entityMeta) + " ADD " + getSqlCheckConstraint(entityMeta, attr);
	}

	static String getSqlDropCheckConstraint(EntityMetaData entityMeta, Attribute attr)
	{
		if (attr.getDataType() != ENUM)
		{
			throw new MolgenisDataException(
					format("Check constraint only allowed for attribute type [%s]", ENUM.toString()));
		}

		return "ALTER TABLE " + getTableName(entityMeta) + " DROP CONSTRAINT " + getCheckConstraintName(entityMeta,
				attr);
	}

	static String getSqlSetNotNull(EntityMetaData entityMeta, Attribute attr)
	{
		return "ALTER TABLE " + getTableName(entityMeta) + " ALTER COLUMN " + getColumnName(attr) + " SET NOT NULL";
	}

	static String getSqlDropNotNull(EntityMetaData entityMeta, Attribute attr)
	{
		return "ALTER TABLE " + getTableName(entityMeta) + " ALTER COLUMN " + getColumnName(attr) + " DROP NOT NULL";
	}

	static String getSqlSetDataType(EntityMetaData entityMeta, Attribute attr)
	{
		return "ALTER TABLE " + getTableName(entityMeta) + " ALTER COLUMN " + getColumnName(attr) + " SET DATA TYPE "
				+ getPostgreSqlType(attr) + " USING " + getColumnName(attr) + "::" + getPostgreSqlType(attr);
	}

	static String getSqlAddColumn(EntityMetaData entityMeta, Attribute attr)
	{
		StringBuilder sql = new StringBuilder("ALTER TABLE ").append(getTableName(entityMeta)).append(" ADD ");
		sql.append(getSqlColumn(entityMeta, attr));
		List<String> sqlTableConstraints = getSqlTableConstraints(entityMeta, attr);
		if (!sqlTableConstraints.isEmpty())
		{
			sqlTableConstraints.forEach(sqlTableConstraint -> sql.append(",ADD ").append(sqlTableConstraint));
		}
		return sql.toString();
	}

	static String getSqlCreateTable(EntityMetaData entityMeta)
	{
		List<Attribute> persistedNonMrefAttrs = getPersistedAttributesNonMref(entityMeta).collect(toList());

		StringBuilder sql = new StringBuilder("CREATE TABLE ").append(getTableName(entityMeta)).append('(');

		// add columns
		for (Iterator<Attribute> it = persistedNonMrefAttrs.iterator(); it.hasNext(); )
		{
			sql.append(getSqlColumn(entityMeta, it.next()));
			if (it.hasNext())
			{
				sql.append(',');
			}
		}

		// add table constraints
		for (Attribute persistedNonMrefAttr : persistedNonMrefAttrs)
		{
			List<String> sqlTableConstraints = getSqlTableConstraints(entityMeta, persistedNonMrefAttr);
			if (!sqlTableConstraints.isEmpty())
			{
				sqlTableConstraints.forEach(sqlTableConstraint -> sql.append(',').append(sqlTableConstraint));
			}
		}

		sql.append(')');

		return sql.toString();
	}

	static String getSqlCreateJunctionTable(EntityMetaData entityMeta, Attribute attr)
	{
		Attribute idAttr = entityMeta.getIdAttribute();
		StringBuilder sql = new StringBuilder("CREATE TABLE ").append(getJunctionTableName(entityMeta, attr))
				.append(" (").append(getColumnName(JUNCTION_TABLE_ORDER_ATTR_NAME)).append(" INT,")
				.append(getColumnName(idAttr)).append(' ').append(getPostgreSqlType(idAttr)).append(" NOT NULL, ")
				.append(getColumnName(attr)).append(' ').append(getPostgreSqlType(attr.getRefEntity().getIdAttribute()))
				.append(" NOT NULL, FOREIGN KEY (").append(getColumnName(idAttr)).append(") REFERENCES ")
				.append(getTableName(entityMeta)).append('(').append(getColumnName(idAttr))
				.append(") ON DELETE CASCADE");

		// for self-referencing data defer checking constraints until the end of the transaction
		if (attr.getRefEntity().getName().equals(entityMeta.getName()))
		{
			sql.append(" DEFERRABLE INITIALLY DEFERRED");
		}

		if (isPersistedInPostgreSql(attr.getRefEntity()))
		{
			sql.append(", FOREIGN KEY (").append(getColumnName(attr)).append(") REFERENCES ")
					.append(getTableName(attr.getRefEntity())).append('(')
					.append(getColumnName(attr.getRefEntity().getIdAttribute())).append(") ON DELETE CASCADE");

			// for self-referencing data defer checking constraints until the end of the transaction
			if (attr.getRefEntity().getName().equals(entityMeta.getName()))
			{
				sql.append(" DEFERRABLE INITIALLY DEFERRED");
			}
		}

		sql.append(", UNIQUE (").append(getColumnName(idAttr)).append(',').append(getColumnName(attr)).append(')');
		sql.append(", UNIQUE (").append(getColumnName(JUNCTION_TABLE_ORDER_ATTR_NAME)).append(',')
				.append(getColumnName(idAttr)).append(')');

		sql.append(')');

		return sql.toString();
	}

	static String getSqlCreateJunctionTableIndex(EntityMetaData entityMeta, Attribute attr)
	{
		Attribute idxAttr = entityMeta.getIdAttribute();
		String junctionTableName = getJunctionTableName(entityMeta, attr);
		String junctionTableIndexName = getJunctionTableIndexName(entityMeta, attr, idxAttr);
		String idxColumnName = getColumnName(idxAttr);
		return "CREATE INDEX " + junctionTableIndexName + " ON " + junctionTableName + " (" + idxColumnName + ')';
	}

	static String getSqlDropJunctionTable(EntityMetaData entityMeta, Attribute attr)
	{
		return getSqlDropTable(getJunctionTableName(entityMeta, attr));
	}

	static String getSqlDropTable(EntityMetaData entityMeta)
	{
		return getSqlDropTable(getTableName(entityMeta));
	}

	static String getSqlDropColumn(EntityMetaData entityMeta, Attribute attr)
	{
		return "ALTER TABLE " + getTableName(entityMeta) + " DROP COLUMN " + getColumnName(attr);
	}

	static String getSqlInsert(EntityMetaData entityMeta)
	{
		StringBuilder sql = new StringBuilder("INSERT INTO ").append(getTableName(entityMeta)).append(" (");
		StringBuilder params = new StringBuilder();
		getPersistedAttributesNonMref(entityMeta).forEach(attr ->
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

	static String getSqlInsertMref(EntityMetaData entityMeta, Attribute attr, Attribute idAttr)
	{
		return "INSERT INTO " + getJunctionTableName(entityMeta, attr) + " (" + getColumnName(
				JUNCTION_TABLE_ORDER_ATTR_NAME) + ',' + getColumnName(idAttr) + ',' + getColumnName(attr)
				+ ") VALUES (?,?,?)";
	}

	static String getSqlDeleteAll(EntityMetaData entityMeta)
	{
		return "DELETE FROM " + getTableName(entityMeta);
	}

	static String getSqlDelete(EntityMetaData entityMeta)
	{
		return getSqlDelete(getTableName(entityMeta), entityMeta.getIdAttribute());
	}

	static String getSqlDelete(String tableName, Attribute attr)
	{
		return "DELETE FROM " + tableName + " WHERE " + getColumnName(attr) + " = ?";
	}

	static String getJunctionTableSelect(EntityMetaData entityMeta, Attribute attr, int numOfIds)
	{
		return "SELECT " + getColumnName(entityMeta.getIdAttribute()) + ", \"" + JUNCTION_TABLE_ORDER_ATTR_NAME + "\","
				+ getColumnName(attr) + " FROM " + getJunctionTableName(entityMeta, attr) + " WHERE " + getColumnName(
				entityMeta.getIdAttribute()) + " in (" + range(0, numOfIds).mapToObj((x) -> "?").collect(joining(", "))
				+ ") ORDER BY " + getColumnName(entityMeta.getIdAttribute()) + ", \"" + JUNCTION_TABLE_ORDER_ATTR_NAME
				+ '"';
	}

	static <E extends Entity> String getSqlSelect(EntityMetaData entityMeta, Query<E> q, List<Object> parameters,
			boolean includeMrefs)
	{
		final StringBuilder select = new StringBuilder("SELECT ");
		final StringBuilder group = new StringBuilder();
		final AtomicInteger count = new AtomicInteger();
		final Attribute idAttribute = entityMeta.getIdAttribute();
		getPersistedAttributes(entityMeta).forEach(attr ->
		{
			if (q.getFetch() == null || q.getFetch().hasField(attr.getName()) || (q.getSort() != null && q.getSort()
					.hasField(attr.getName())))
			{
				if (count.get() > 0)
				{
					select.append(", ");
				}

				if (isMultipleReferenceType(attr))
				{
					if (includeMrefs)
					{
						// TODO retrieve mref values in seperate queries to allow specifying limit and offset after nested
						// MOLGENIS queries are implemented as sub-queries instead of query rules
						String mrefSelect = MessageFormat
								.format("(SELECT array_agg(DISTINCT ARRAY[{0}.{1}::TEXT,{0}.{0}::TEXT]) "
												+ "FROM {2} AS {0} WHERE this.{3} = {0}.{3}) AS {0}", getColumnName(attr),
										getColumnName(JUNCTION_TABLE_ORDER_ATTR_NAME),
										getJunctionTableName(entityMeta, attr), getColumnName(idAttribute));
						select.append(mrefSelect);
					}
					else
					{
						select.append("NULL AS " + getColumnName(attr));
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
		StringBuilder result = new StringBuilder().append(select).append(getSqlFrom(entityMeta, q));
		// where
		String where = getSqlWhere(entityMeta, q, parameters, 0);
		if (where.length() > 0)
		{
			result.append(" WHERE ").append(where);
		}
		// order by
		result.append(' ').append(getSqlSort(entityMeta, q));
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

	static String getSqlUpdate(EntityMetaData entityMeta)
	{
		// use (readonly) identifier
		Attribute idAttribute = entityMeta.getIdAttribute();

		// create sql
		StringBuilder sql = new StringBuilder("UPDATE ").append(getTableName(entityMeta)).append(" SET ");
		getPersistedAttributesNonMref(entityMeta).forEach(attr -> sql.append(getColumnName(attr)).append(" = ?, "));
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
	static <E extends Entity> String getSqlCount(EntityMetaData entityMeta, Query<E> q, List<Object> parameters)
	{
		StringBuilder sqlBuilder = new StringBuilder("SELECT COUNT");
		String idAttribute = getColumnName(entityMeta.getIdAttribute());

		List<QueryRule> queryRules = q.getRules();
		if (queryRules == null || queryRules.isEmpty())
		{
			sqlBuilder.append("(*) FROM ").append(getTableName(entityMeta));
		}
		else
		{
			List<Attribute> mrefAttrsInQuery = getMrefQueryAttrs(entityMeta, q);
			if (!mrefAttrsInQuery.isEmpty())
			{
				// distinct count in case query contains one or more rules refering to MREF attributes.
				sqlBuilder.append("(DISTINCT this.").append(idAttribute).append(')');
			}
			else
			{
				sqlBuilder.append("(*)");
			}

			String from = getSqlFromForCount(entityMeta, mrefAttrsInQuery);
			String where = getSqlWhere(entityMeta, q, parameters, 0);
			sqlBuilder.append(from).append(" WHERE ").append(where);
		}
		return sqlBuilder.toString();
	}

	private static String getSqlColumn(EntityMetaData entityMeta, Attribute attr)
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
			case COMPOUND:
			case CATEGORICAL_MREF:
			case MREF:
				throw new RuntimeException(format("Illegal attribute type [%s]", attrType.toString()));
			default:
				throw new RuntimeException(format("Unknown attribute type [%s]", attrType.toString()));
		}

		String sqlColumnConstraints = getSqlColumnConstraints(entityMeta, attr);
		if (!sqlColumnConstraints.isEmpty())
		{
			sqlBuilder.append(' ').append(sqlColumnConstraints);
		}
		return sqlBuilder.toString();
	}

	private static String getSqlColumnConstraints(EntityMetaData entityMeta, Attribute attr)
	{
		StringBuilder sqlBuilder = new StringBuilder();
		if (!attr.getName().equals(entityMeta.getIdAttribute().getName()))
		{
			if (!attr.isNillable())
			{
				sqlBuilder.append("NOT NULL");
			}
		}
		return sqlBuilder.toString();
	}

	private static List<String> getSqlTableConstraints(EntityMetaData entityMeta, Attribute attr)
	{
		List<String> tableConstraints = Lists.newArrayList();

		if (attr.getName().equals(entityMeta.getIdAttribute().getName()))
		{
			tableConstraints.add(getSqlConstraintPrimaryKey(entityMeta, attr));
		}
		else
		{
			if (isSingleReferenceType(attr) && isPersistedInPostgreSql(attr.getRefEntity()))
			{
				tableConstraints.add(getSqlForeignKey(entityMeta, attr));
			}
			if (attr.isUnique())
			{
				tableConstraints.add(getSqlUniqueKey(entityMeta, attr));
			}
			if (attr.getDataType() == ENUM)
			{
				tableConstraints.add(getSqlCheckConstraint(entityMeta, attr));
			}
		}

		return tableConstraints;
	}

	private static String getSqlDropTable(String tableName)
	{
		return "DROP TABLE " + tableName;
	}

	private static <E extends Entity> String getSqlWhere(EntityMetaData entityMeta, Query<E> q, List<Object> parameters,
			int mrefFilterIndex)
	{
		StringBuilder result = new StringBuilder();
		for (QueryRule r : q.getRules())
		{
			Attribute attr = null;
			if (r.getField() != null)
			{
				attr = entityMeta.getAttribute(r.getField());
				if (attr == null)
				{
					throw new MolgenisDataException(format("Unknown attribute [%s]", r.getField()));
				}
				if (isMultipleReferenceType(attr))
				{
					mrefFilterIndex++;
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
					result.append('(').append(getSqlWhere(entityMeta, nestedQ, parameters, mrefFilterIndex))
							.append(')');
					break;
				case OR:
					result.append(" OR ");
					break;
				case LIKE:

					String columnName;
					if (isMultipleReferenceType(attr))
					{
						columnName = getFilterColumnName(attr, mrefFilterIndex);
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
					Stream<Object> postgreSqlIds = stream(((Iterable<?>) inValue).spliterator(), false)
							.map(idValue -> PostgreSqlUtils.getPostgreSqlQueryValue(idValue, inAttr));
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

					if (isMultipleReferenceType(attr))
					{
						result.append(getFilterColumnName(attr, mrefFilterIndex));
					}
					else
					{
						result.append("this");
					}

					result.append('.').append(getColumnName(r.getField())).append(" IN (").append(in).append(')');
					break;
				case NOT:
					result.append(" NOT ");
					break;
				case RANGE:
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
					if (isMultipleReferenceType(attr))
					{
						column.append(getFilterColumnName(attr, mrefFilterIndex));
					}
					else
					{
						column.append("this");
					}
					column.append('.').append(getColumnName(r.getField()));
					predicate.append(column).append(" >= ? AND ").append(column).append(" <= ?");
					result.append(predicate);
					break;
				case EQUALS:
					if (isMultipleReferenceType(attr))
					{
						predicate.append(getFilterColumnName(attr, mrefFilterIndex));
					}
					else
					{
						predicate.append("this");
					}

					predicate.append('.').append(getColumnName(r.getField()));
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
							.endsWith(" AND ") && !result.toString().endsWith(" NOT "))
					{
						result.append(" AND ");
					}
					result.append(predicate);
					break;
				case GREATER:
				case GREATER_EQUAL:
				case LESS:
				case LESS_EQUAL:
					if (isMultipleReferenceType(attr))
					{
						predicate.append(getFilterColumnName(attr, mrefFilterIndex));
					}
					else
					{
						predicate.append("this");
					}

					predicate.append('.').append(getColumnName(r.getField()));
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
							.endsWith(" AND ") && !result.toString().endsWith(" NOT "))
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
					throw new RuntimeException(format("Unknown query operator [%s]", operator.toString()));
			}
		}

		return result.toString().trim();
	}

	private static <E extends Entity> String getSqlSort(EntityMetaData entityMeta, Query<E> q)
	{
		StringBuilder sortSql = new StringBuilder();
		if (q.getSort() != null)
		{
			for (Sort.Order o : q.getSort())
			{
				Attribute attr = entityMeta.getAttribute(o.getAttr());
				if (isMultipleReferenceType(attr))
				{
					sortSql.append(", ").append(getColumnName(attr));
				}
				else
				{
					sortSql.append(", ").append(getColumnName(attr));
				}
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
		}
		return sortSql.toString();
	}

	private static <E extends Entity> String getSqlFrom(EntityMetaData entityMeta, Query<E> q)
	{
		StringBuilder from = new StringBuilder(" FROM ").append(getTableName(entityMeta)).append(" AS this");

		Attribute idAttribute = entityMeta.getIdAttribute();

		List<Attribute> mrefAttrsInQuery = getMrefQueryAttrs(entityMeta, q);
		for (int i = 0; i < mrefAttrsInQuery.size(); i++)
		{
			// extra join so we can filter on the mrefs
			Attribute mrefAttr = mrefAttrsInQuery.get(i);

			from.append(" LEFT JOIN ").append(getJunctionTableName(entityMeta, mrefAttr)).append(" AS ")
					.append(getFilterColumnName(mrefAttr, i + 1)).append(" ON (this.")
					.append(getColumnName(idAttribute)).append(" = ").append(getFilterColumnName(mrefAttr, i + 1))
					.append('.').append(getColumnName(idAttribute)).append(')');
		}

		return from.toString();
	}

	private static <E extends Entity> String getSqlFromForCount(EntityMetaData entityMeta,
			List<Attribute> mrefAttrsInQuery)
	{
		StringBuilder from = new StringBuilder(" FROM ").append(getTableName(entityMeta)).append(" AS this");

		Attribute idAttribute = entityMeta.getIdAttribute();

		for (int i = 0; i < mrefAttrsInQuery.size(); i++)
		{
			// extra join so we can filter on the mrefs
			Attribute mrefAttr = mrefAttrsInQuery.get(i);

			from.append(" LEFT JOIN ").append(getJunctionTableName(entityMeta, mrefAttr)).append(" AS ")
					.append(getFilterColumnName(mrefAttr, i + 1)).append(" ON (this.")
					.append(getColumnName(idAttribute)).append(" = ").append(getFilterColumnName(mrefAttr, i + 1))
					.append('.').append(getColumnName(idAttribute)).append(')');
		}

		return from.toString();
	}

	private static String getColumnName(Attribute attr)
	{
		return getColumnName(attr.getName());
	}

	private static String getColumnName(String attrName)
	{
		return '"' + attrName + '"';
	}

	private static String getFilterColumnName(Attribute attr, int filterIndex)
	{
		return '"' + attr.getName() + "_filter" + filterIndex + '"';
	}

	private static String getPrimaryKeyName(EntityMetaData entityMeta, Attribute attr)
	{
		return getConstraintName(entityMeta, attr, "pkey");
	}

	private static String getForeignKeyName(EntityMetaData entityMeta, Attribute attr)
	{
		return getConstraintName(entityMeta, attr, "fkey");
	}

	private static String getUniqueKeyName(EntityMetaData entityMeta, Attribute attr)
	{
		return getConstraintName(entityMeta, attr, "key");
	}

	private static String getCheckConstraintName(EntityMetaData entityMeta, Attribute attr)
	{
		return getConstraintName(entityMeta, attr, "chk");
	}

	private static String getConstraintName(EntityMetaData entityMeta, Attribute attr, String constraintPostfix)
	{
		return '"' + entityMeta.getName() + '_' + attr.getName() + '_' + constraintPostfix + '"';
	}

	private static <E extends Entity> List<Attribute> getMrefQueryAttrs(EntityMetaData entityMeta, Query<E> q)
	{
		List<Attribute> mrefAttrsInQuery = Lists.newArrayList();
		getMrefQueryFieldsRec(entityMeta, q.getRules(), mrefAttrsInQuery);
		return mrefAttrsInQuery;
	}

	private static void getMrefQueryFieldsRec(EntityMetaData entityMeta, List<QueryRule> rules,
			List<Attribute> mrefAttrsInQuery)
	{
		for (QueryRule rule : rules)
		{
			if (rule.getField() != null)
			{
				Attribute attr = entityMeta.getAttribute(rule.getField());
				if (attr != null && isMultipleReferenceType(attr))
				{
					mrefAttrsInQuery.add(attr);
				}
			}

			if (rule.getNestedRules() != null && !rule.getNestedRules().isEmpty())
			{
				getMrefQueryFieldsRec(entityMeta, rule.getNestedRules(), mrefAttrsInQuery);
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
					return "timestamp";
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
				case COMPOUND:
					throw new RuntimeException(format("Illegal attribute type [%s]", attrType.toString()));
				default:
					throw new RuntimeException(format("Unknown attribute type [%s]", attrType.toString()));
			}
		}
	}
}

