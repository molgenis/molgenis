package org.molgenis.data.postgresql;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.MolgenisFieldTypes.AttributeType;
import org.molgenis.data.*;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.fieldtypes.FieldType;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static java.util.stream.IntStream.range;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.MolgenisFieldTypes.AttributeType.*;
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

	static String getSqlCreateForeignKey(EntityMetaData entityMeta, AttributeMetaData attr)
	{
		return new StringBuilder().append("ALTER TABLE ").append(getTableName(entityMeta)).append(" ADD FOREIGN KEY (")
				.append(getColumnName(attr)).append(") REFERENCES ").append(getTableName(attr.getRefEntity()))
				.append('(').append(getColumnName(attr.getRefEntity().getIdAttribute())).append(")").toString();
	}

	static String getSqlCreateUniqueKey(EntityMetaData entityMeta, AttributeMetaData attr)
	{
		// PostgreSQL name convention
		return new StringBuilder().append("ALTER TABLE ").append(getTableName(entityMeta)).append(" ADD CONSTRAINT ")
				.append(getUniqueKeyName(entityMeta, attr)).append(" UNIQUE (").append(getColumnName(attr)).append(")")
				.toString();
	}

	static String getSqlDropUniqueKey(EntityMetaData entityMeta, AttributeMetaData attr)
	{
		// PostgreSQL name convention
		return new StringBuilder().append("ALTER TABLE ").append(getTableName(entityMeta)).append(" DROP CONSTRAINT ")
				.append(getUniqueKeyName(entityMeta, attr)).toString();
	}

	static String getSqlSetNotNull(EntityMetaData entityMeta, AttributeMetaData attr)
	{
		return new StringBuilder().append("ALTER TABLE ").append(getTableName(entityMeta)).append(" ALTER COLUMN ")
				.append(getColumnName(attr)).append(" SET NOT NULL").toString();
	}

	static String getSqlDropNotNull(EntityMetaData entityMeta, AttributeMetaData attr)
	{
		return new StringBuilder().append("ALTER TABLE ").append(getTableName(entityMeta)).append(" ALTER COLUMN ")
				.append(getColumnName(attr)).append(" DROP NOT NULL").toString();
	}

	static String getSqlSetDataType(EntityMetaData entityMeta, AttributeMetaData attr)
	{
		return new StringBuilder().append("ALTER TABLE ").append(getTableName(entityMeta)).append(" ALTER COLUMN ")
				.append(getColumnName(attr)).append(" SET DATA TYPE ").append(getPostgreSqlType(attr))
				.append(" USING ").append(getColumnName(attr)).append("::").append(getPostgreSqlType(attr)).toString();
	}

	/**
	 * @param entityMeta
	 * @param attr
	 * @return
	 */
	static String getSqlAddColumn(EntityMetaData entityMeta, AttributeMetaData attr)
	{
		StringBuilder sql = new StringBuilder();
		sql.append("ALTER TABLE ").append(getTableName(entityMeta)).append(" ADD ");
		getSqlAttribute(sql, attr);
		return sql.toString();
	}

	static String getSqlCreateTable(EntityMetaData entityMeta)
	{
		StringBuilder sql = new StringBuilder();
		sql.append("CREATE TABLE ").append(getTableName(entityMeta)).append('(');

		getPersistedAttributesNonMref(entityMeta).forEach(attr ->
		{
			getSqlAttribute(sql, attr);
			sql.append(", ");
		});

		// primary key is first attribute unless otherwise indicated
		AttributeMetaData idAttribute = entityMeta.getIdAttribute();

		if (idAttribute == null)
		{
			throw new MolgenisDataException(format("Missing idAttribute for entity [%s]", entityMeta.getName()));
		}

		if (isReferenceType(idAttribute))
		{
			throw new RuntimeException(format("primary key(%s.%s) cannot be XREF or MREF", getTableName(entityMeta),
					getColumnName(idAttribute)));
		}

		if (idAttribute.isNillable() == true)
		{
			throw new RuntimeException(format("idAttribute (%s.%s) should not be nillable", getTableName(entityMeta),
					getColumnName(idAttribute)));
		}

		sql.append("PRIMARY KEY (").append(getColumnName(entityMeta.getIdAttribute())).append(')');
		sql.append(')');

		return sql.toString();
	}

	static String getSqlCreateJunctionTable(EntityMetaData entityMeta, AttributeMetaData attr)
	{
		AttributeMetaData idAttr = entityMeta.getIdAttribute();
		StringBuilder sql = new StringBuilder();

		sql.append(" CREATE TABLE IF NOT EXISTS ").append(getJunctionTableName(entityMeta, attr)).append(" (")
				.append(getColumnName(JUNCTION_TABLE_ORDER_ATTR_NAME)).append(" INT,").append(getColumnName(idAttr))
				.append(' ').append(getPostgreSqlType(idAttr)).append(" NOT NULL, ")
				.append(getColumnName(attr)).append(' ').append(getPostgreSqlType(attr.getRefEntity().getIdAttribute()))
				.append(" NOT NULL, FOREIGN KEY (").append(getColumnName(idAttr)).append(") REFERENCES ")
				.append(getTableName(entityMeta)).append('(').append(getColumnName(idAttr))
				.append(") ON DELETE CASCADE");

		if (isPersistedInPostgreSql(attr.getRefEntity()))
		{
			sql.append(", FOREIGN KEY (").append(getColumnName(attr)).append(") REFERENCES ")
					.append(getTableName(attr.getRefEntity())).append('(')
					.append(getColumnName(attr.getRefEntity().getIdAttribute())).append(") ON DELETE CASCADE");
		}

		sql.append(", UNIQUE (").append(getColumnName(attr)).append(',').append(getColumnName(idAttr)).append(')');
		sql.append(", UNIQUE (").append(getColumnName(JUNCTION_TABLE_ORDER_ATTR_NAME)).append(',')
				.append(getColumnName(idAttr)).append(')');

		sql.append(')');

		return sql.toString();
	}

	static String getSqlCreateJunctionTableIndex(EntityMetaData entityMeta, AttributeMetaData attr)
	{
		String junctionTableName = getJunctionTableName(entityMeta, attr);
		return "CREATE INDEX " + junctionTableName.substring(0, junctionTableName.length() - 1) + "_" + entityMeta
				.getIdAttribute().getName() + "_index\" ON " + junctionTableName + " (" + getColumnName(
				entityMeta.getIdAttribute()) + ")";
	}

	static String getSqlDropJunctionTable(EntityMetaData entityMeta, AttributeMetaData attr)
	{
		return getSqlDropTable(getJunctionTableName(entityMeta, attr));
	}

	static String getSqlDropTable(EntityMetaData entityMeta)
	{
		return getSqlDropTable(getTableName(entityMeta));
	}

	static String getSqlDropColumn(EntityMetaData entityMeta, String attrName)
	{
		return new StringBuilder().append("ALTER TABLE ").append(getTableName(entityMeta)).append(" DROP COLUMN ")
				.append(getColumnName(attrName)).toString();
	}

	static String getSqlInsert(EntityMetaData entityMeta)
	{
		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO ").append(getTableName(entityMeta)).append(" (");
		StringBuilder params = new StringBuilder();
		getPersistedAttributesNonMref(entityMeta).forEach(attr -> {
			sql.append(getColumnName(attr)).append(", ");
			params.append("?, ");
		});
		if (sql.charAt(sql.length() - 1) == ' ' && sql.charAt(sql.length() - 2) == ',')
		{
			sql.setLength(sql.length() - 2);
			params.setLength(params.length() - 2);
		}
		sql.append(") VALUES (").append(params).append(")");
		return sql.toString();
	}

	static String getSqlInsertMref(EntityMetaData entityMeta, AttributeMetaData attr, AttributeMetaData idAttr)
	{
		return new StringBuilder().append("INSERT INTO ").append(getJunctionTableName(entityMeta, attr)).append(" (")
				.append(getColumnName(JUNCTION_TABLE_ORDER_ATTR_NAME)).append(',').append(getColumnName(idAttr))
				.append(',').append(getColumnName(attr)).append(") VALUES (?,?,?)").toString();
	}

	static String getSqlDeleteAll(EntityMetaData entityMeta)
	{
		return new StringBuilder().append("DELETE FROM ").append(getTableName(entityMeta)).toString();
	}

	static String getSqlDelete(EntityMetaData entityMeta)
	{
		return getSqlDelete(getTableName(entityMeta), entityMeta.getIdAttribute());
	}

	static String getSqlDelete(String tableName, AttributeMetaData attr)
	{
		return new StringBuilder().append("DELETE FROM ").append(tableName).append(" WHERE ")
				.append(getColumnName(attr)).append(" = ?").toString();
	}

	static String getJunctionTableSelect(EntityMetaData entityMeta, AttributeMetaData attr, int numOfIds)
	{
		return "SELECT " + getColumnName(entityMeta.getIdAttribute()) + ", \"" + JUNCTION_TABLE_ORDER_ATTR_NAME + "\","
				+ getColumnName(attr) + " FROM " + getJunctionTableName(entityMeta, attr) + " WHERE " + getColumnName(
				entityMeta.getIdAttribute()) + " in (" + range(0, numOfIds).mapToObj((x) -> "?").collect(joining(", "))
				+ ") ORDER BY " + getColumnName(entityMeta.getIdAttribute()) + ", \"" + JUNCTION_TABLE_ORDER_ATTR_NAME
				+ "\"";
	}

	static <E extends Entity> String getSqlSelect(EntityMetaData entityMeta, Query<E> q, List<Object> parameters,
			boolean includeMrefs)
	{
		final StringBuilder select = new StringBuilder("SELECT ");
		final StringBuilder group = new StringBuilder();
		final AtomicInteger count = new AtomicInteger();
		final AttributeMetaData idAttribute = entityMeta.getIdAttribute();
		getPersistedAttributes(entityMeta).forEach(attr -> {
			if (q.getFetch() == null || q.getFetch().hasField(attr.getName()))
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
		AttributeMetaData idAttribute = entityMeta.getIdAttribute();

		// create sql
		StringBuilder sql = new StringBuilder("UPDATE ").append(getTableName(entityMeta)).append(" SET ");
		getPersistedAttributesNonMref(entityMeta).forEach(attr -> {
			sql.append(getColumnName(attr)).append(" = ?, ");
		});
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
	 * @param q
	 * @param parameters
	 * @return
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
			List<AttributeMetaData> mrefAttrsInQuery = getMrefQueryAttrs(entityMeta, q);
			if (!mrefAttrsInQuery.isEmpty())
			{
				// distinct count in case query contains one or more rules refering to MREF attributes.
				sqlBuilder.append("(DISTINCT this.").append(idAttribute).append(')');
			}
			else
			{
				sqlBuilder.append("(*)");
			}

			String from = getSqlFromForCount(entityMeta, q, mrefAttrsInQuery);
			String where = getSqlWhere(entityMeta, q, parameters, 0);
			sqlBuilder.append(from).append(" WHERE ").append(where);
		}
		return sqlBuilder.toString();
	}

	private static void getSqlAttribute(StringBuilder sql, AttributeMetaData attr)
	{
		sql.append(getColumnName(attr)).append(' ');

		AttributeType attrType = attr.getDataType();
		switch (attrType) {
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
				sql.append(getPostgreSqlType(attr));
				break;
			case CATEGORICAL:
			case FILE:
			case XREF:
				sql.append(getPostgreSqlType(attr.getRefEntity().getIdAttribute()));
				break;
			case COMPOUND:
			case CATEGORICAL_MREF:
			case MREF:
				throw new RuntimeException(format("Illegal attribute type [%s]", attrType.toString()));
			default:
				throw new RuntimeException(format("Unknown attribute type [%s]", attrType.toString()));
		}

		if (!attr.isNillable())
		{
			sql.append(" NOT NULL");
		}

		if (attr.getDataType() == ENUM)
		{
			sql.append(" CHECK (").append(getColumnName(attr)).append(" IN (")
					.append(attr.getEnumOptions().stream().map(enumOption -> '\'' + enumOption + '\'')
							.collect(joining(","))).append("))");
		}
	}

	private static String getSqlDropTable(String tableName)
	{
		return new StringBuilder("DROP TABLE ").append(tableName).toString();
	}

	private static <E extends Entity> String getSqlWhere(EntityMetaData entityMeta, Query<E> q, List<Object> parameters,
			int mrefFilterIndex)
	{
		StringBuilder result = new StringBuilder();
		for (QueryRule r : q.getRules())
		{
			AttributeMetaData attr = null;
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
						result.append(" ").append(columnName);
					}
					else
					{
						result.append(" CAST(").append(columnName).append(" as TEXT)");
					}

					result.append(" LIKE ?");
					parameters.add("%" + PostgreSqlUtils.getPostgreSqlQueryValue(r.getValue(), attr) + "%");
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
					AttributeMetaData inAttr = attr;
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
					Iterator<Object> rangeValues = ((Iterable) range).iterator();
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
						case EQUALS:
							predicate.append(" =");
							break;
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
				AttributeMetaData attr = entityMeta.getAttribute(o.getAttr());
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
		StringBuilder from = new StringBuilder().append(" FROM ").append(getTableName(entityMeta)).append(" AS this");

		AttributeMetaData idAttribute = entityMeta.getIdAttribute();

		List<AttributeMetaData> mrefAttrsInQuery = getMrefQueryAttrs(entityMeta, q);
		for (int i = 0; i < mrefAttrsInQuery.size(); i++)
		{
			// extra join so we can filter on the mrefs
			AttributeMetaData mrefAttr = mrefAttrsInQuery.get(i);

			from.append(" LEFT JOIN ").append(getJunctionTableName(entityMeta, mrefAttr)).append(" AS ")
					.append(getFilterColumnName(mrefAttr, i + 1)).append(" ON (this.")
					.append(getColumnName(idAttribute)).append(" = ").append(getFilterColumnName(mrefAttr, i + 1))
					.append('.').append(getColumnName(idAttribute)).append(')');
		}

		return from.toString();
	}

	private static <E extends Entity> String getSqlFromForCount(EntityMetaData entityMeta, Query<E> q,
			List<AttributeMetaData> mrefAttrsInQuery)
	{
		StringBuilder from = new StringBuilder().append(" FROM ").append(getTableName(entityMeta)).append(" AS this");

		AttributeMetaData idAttribute = entityMeta.getIdAttribute();

		for (int i = 0; i < mrefAttrsInQuery.size(); i++)
		{
			// extra join so we can filter on the mrefs
			AttributeMetaData mrefAttr = mrefAttrsInQuery.get(i);

			from.append(" LEFT JOIN ").append(getJunctionTableName(entityMeta, mrefAttr)).append(" AS ")
					.append(getFilterColumnName(mrefAttr, i + 1)).append(" ON (this.")
					.append(getColumnName(idAttribute)).append(" = ").append(getFilterColumnName(mrefAttr, i + 1))
					.append('.').append(getColumnName(idAttribute)).append(')');
		}

		return from.toString();
	}

	static String getColumnName(AttributeMetaData attr)
	{
		return getColumnName(attr.getName());
	}

	static String getColumnName(String attrName)
	{
		return new StringBuilder().append("\"").append(attrName).append("\"").toString();
	}

	private static String getFilterColumnName(AttributeMetaData attr, int filterIndex)
	{
		return new StringBuilder().append("\"").append(attr.getName()).append("_filter").append(filterIndex)
				.append("\"").toString();
	}

	private static String getUniqueKeyName(EntityMetaData emd, AttributeMetaData attr)
	{
		return new StringBuilder().append("\"").append(emd.getName()).append('_').append(attr.getName()).append("_key")
				.append("\"").toString();
	}

	private static <E extends Entity> List<AttributeMetaData> getMrefQueryAttrs(EntityMetaData entityMeta, Query<E> q)
	{
		List<AttributeMetaData> mrefAttrsInQuery = new ArrayList<>();
		getMrefQueryFieldsRec(entityMeta, q.getRules(), mrefAttrsInQuery);
		return mrefAttrsInQuery;
	}

	private static void getMrefQueryFieldsRec(EntityMetaData entityMeta, List<QueryRule> rules,
			List<AttributeMetaData> mrefAttrsInQuery)
	{
		for (QueryRule rule : rules)
		{
			if (rule.getField() != null)
			{
				AttributeMetaData attr = entityMeta.getAttribute(rule.getField());
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

	private static String getPostgreSqlType(AttributeMetaData attr)
	{
		FieldType fieldType = MolgenisFieldTypes.getType(getValueString(attr.getDataType()));
		return fieldType.getPostgreSqlType();
	}
}

